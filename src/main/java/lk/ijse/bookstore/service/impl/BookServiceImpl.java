package lk.ijse.bookstore.service.impl;

import lk.ijse.bookstore.dto.request.BookRequestDTO;
import lk.ijse.bookstore.dto.response.BookResponseDTO;
import lk.ijse.bookstore.entity.*;
import lk.ijse.bookstore.exception.CustomerException;
import lk.ijse.bookstore.exception.ResponseCode;
import lk.ijse.bookstore.exception.ResponseMessage;
import lk.ijse.bookstore.repository.*;
import lk.ijse.bookstore.service.BookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final PublisherRepository publisherRepository;
    private final CategoryRepository categoryRepository;
    private final AuthorRepository authorRepository;
    private final InventoryRepository inventoryRepository;
    private final BookImageRepository bookImageRepository;
    private final ReviewRepository reviewRepository;
    private final OrderItemRepository orderItemRepository;


    @Override
    public BookResponseDTO save(BookRequestDTO dto){
        try {
            Book book = new Book();
            applyRequest(book, dto);
            book = bookRepository.save(book);

            if (dto.getQuantityAvailable() != null) {
                inventoryRepository.save(Inventory.builder()
                        .book(book).quantityAvailable(dto.getQuantityAvailable()).build());
            }
            if (dto.getImageUrl() != null && !dto.getImageUrl().isBlank()) {
                bookImageRepository.save(BookImage.builder()
                        .book(book).imageUrl(dto.getImageUrl()).isPrimary(true).build());
            }

            log.info("Book created: {}", book.getTitle());
            return toDto(bookRepository.findById(book.getBookId()).orElse(book));

        }catch (CustomerException ce){
            throw ce;
        }catch (Exception ex){
            log.error("save fail", ex);
            throw new CustomerException(ResponseCode.INTERNAL_ERROR, ResponseMessage.UNEXPECTED_ERROR);
        }
    }



    @Override
    public BookResponseDTO update(Long id, BookRequestDTO dto) {
        Book book = findEntity(id);
        applyRequest(book, dto);
        final Book savedBook = bookRepository.save(book);

        if (dto.getQuantityAvailable() != null) {
            Inventory inventory = inventoryRepository.findByBook_BookId(id)
                    .orElseGet(() -> Inventory.builder().book(savedBook).build());
            inventory.setQuantityAvailable(dto.getQuantityAvailable());
            inventoryRepository.save(inventory);
        }

        return toDto(savedBook);
    }


    @Override
    public void delete(Long id) {
        bookRepository.delete(findEntity(id));
    }


    @Override
    public  BookResponseDTO findById(Long id){
        return toDto(findEntity(id));
    }

    @Override
    public List<BookResponseDTO> findAll(){
        return bookRepository.findAll()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }


    @Override
    public List<BookResponseDTO> search(String keyword, String category){
        List<Book> books;
        if (keyword != null && !keyword.isBlank()){
            books = bookRepository.searchByKeyword(keyword);
        } else if (category != null && !category.isBlank()){
            books = bookRepository.findByCategoryName(category);
        }else {
            books = bookRepository.findAll();
        }
        return books.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }


    @Override
    public  List<BookResponseDTO> newArrivals(){
        LocalDateTime cutoff = LocalDateTime.now().minusDays(30);
        List<Book> books = bookRepository.findTop10ByCreatedAtAfterOrderByCreatedAtDesc(cutoff);
        if (books.isEmpty()){
            books = bookRepository.findTop10ByOrderByCreatedAtDesc();
        }
        return books.stream().map(this::toDto).collect(Collectors.toList());
    }


    @Override
    public List<BookResponseDTO> bestsellers(String range){
        Map<Long, Integer> soldByBook = new LinkedHashMap<>();
        for (OrderItem item : orderItemRepository.findAll()) {
            soldByBook.merge(item.getBook().getBookId(), item.getQuantity(), Integer::sum);
        }

        return soldByBook.entrySet().stream()
                .sorted((a, b) -> b.getValue() - a.getValue())
                .limit(10)
                .map(e -> toDto(findEntity(e.getKey())))
                .collect(Collectors.toList());
    }

    @Override
    public List<BookResponseDTO> trending() {
        List<Book> books = bookRepository.findAll();
        return books.stream()
                .filter(b -> !b.getReviews().isEmpty())
                .sorted((a, b) -> Double.compare(avgRating(b), avgRating(a)))
                .limit(10)
                .map(this::toDto)
                .collect(Collectors.toList());
    }


    private void applyRequest(Book book, BookRequestDTO dto){
        book.setTitle(dto.getTitle());
        book.setIsbn(dto.getIsbn());
        book.setDescription(dto.getDescription());
        book.setPrice(dto.getPrice());
        book.setSpecialPrice(dto.getSpecialPrice());
        book.setLanguage(dto.getLanguage());
        book.setPages(dto.getPages());

        if (dto.getPublisherId() != null) {
            Publisher publisher = publisherRepository.findById(dto.getPublisherId())
                    .orElseThrow(() -> new CustomerException(ResponseCode.NOT_FOUND, ResponseMessage.PUBLISHER_NOT_FOUND));
            book.setPublisher(publisher);
        }

        if (dto.getCategoryIds() != null) {
            Set<Category> categories = dto.getCategoryIds().stream()
                    .map(cid -> categoryRepository.findById(cid)
                            .orElseThrow(() -> new CustomerException(ResponseCode.NOT_FOUND, ResponseMessage.CATEGORY_NOT_FOUND)))
                    .collect(Collectors.toSet());
            book.setCategories(categories);
        }

        if (dto.getAuthorIds() != null) {
            Set<Author> authors = dto.getAuthorIds().stream()
                    .map(aid -> authorRepository.findById(aid)
                            .orElseThrow(() -> new CustomerException(ResponseCode.NOT_FOUND, ResponseMessage.AUTHOR_NOT_FOUND)))
                    .collect(Collectors.toSet());
            book.setAuthors(authors);
        }
    }


    private Book findEntity(Long id){
        return bookRepository.findById(id)
                .orElseThrow(() -> new CustomerException(ResponseCode.NOT_FOUND, ResponseMessage.BOOK_NOT_FOUND));
    }


    private double avgRating(Book book){
        return book.getReviews()
                .stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);
    }



    private BookResponseDTO toDto(Book book){
        int qty = inventoryRepository.findByBook_BookId(book.getBookId())
                .map(Inventory::getQuantityAvailable)
                .orElse(0);


        List<String> authorNames = book.getAuthors().stream().map(Author::getName).collect(Collectors.toList());
        List<String> categoryNames = book.getCategories().stream().map(Category::getName).collect(Collectors.toList());
        List<String> imageUrls = book.getImages().stream().map(BookImage::getImageUrl).collect(Collectors.toList());


        double avgRating = avgRating(book);
        boolean isNew = book.getCreatedAt() != null && book.getCreatedAt().isAfter(LocalDateTime.now().minusDays(30));


        return BookResponseDTO.builder()
                .id(book.getBookId())
                .title(book.getTitle())
                .isbn(book.getIsbn())
                .description(book.getDescription())
                .price(book.getPrice())
                .specialPrice(book.getSpecialPrice())
                .language(book.getLanguage())
                .pages(book.getPages())
                .author(authorNames.isEmpty() ? "unknown" : authorNames.get(0))
                .category(categoryNames.isEmpty() ? "" : categoryNames.get(0))
                .publisher(book.getPublisher() != null ? book.getPublisher().getName() : null)
                .authors(authorNames)
                .categories(categoryNames)
                .images(imageUrls)
                .rating(Math.round(avgRating * 10.0) / 10.0)
                .reviewCount(book.getReviews().size())
                .inStock(qty > 0)
                .quantityAvailable(qty)
                .isNew(isNew)
                .build();

    }






}
