package lk.ijse.bookstore.service.impl;

import lk.ijse.bookstore.dto.request.WishlistItemRequestDTO;
import lk.ijse.bookstore.dto.response.BookResponseDTO;
import lk.ijse.bookstore.dto.response.WishlistResponseDTO;
import lk.ijse.bookstore.entity.*;
import lk.ijse.bookstore.exception.CustomerException;
import lk.ijse.bookstore.exception.ResponseCode;
import lk.ijse.bookstore.exception.ResponseMessage;
import lk.ijse.bookstore.repository.*;
import lk.ijse.bookstore.service.WishlistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class WishlistServiceImpl implements WishlistService {
    private final WishlistRepository wishlistRepository;
    private final WishlistItemRepository wishlistItemRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final InventoryRepository inventoryRepository;


    @Override
    public WishlistResponseDTO getMyWishlist(String email) {
        return toDto(getOrCreate(email));
    }


    @Override
    public WishlistResponseDTO addItem(String email, WishlistItemRequestDTO dto){
        Wishlist wishlist = getOrCreate(email);
        Book book = bookRepository.findById(dto.getBookId())
                .orElseThrow(() -> new CustomerException(ResponseCode.NOT_FOUND, ResponseMessage.BOOK_NOT_FOUND));

        wishlistItemRepository.findByWishlist_WishlistIdAndBook_BookId(wishlist.getWishlistId(), book.getBookId())
                .orElseGet(() -> wishlistItemRepository.save(
                        WishlistItem
                                .builder()
                                .wishlist(wishlist)
                                .book(book)
                                .build()));

        log.info("Book {} saved_to_wishlist {}", book.getBookId(), email);
        return toDto(wishlistRepository.findById(wishlist.getWishlistId()).orElse(wishlist));
    }



    private Wishlist getOrCreate(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomerException(ResponseCode.NOT_FOUND, ResponseMessage.USER_NOT_FOUND));
        return wishlistRepository.findByUser_UserId(user.getUserId())
                .orElseGet(() -> wishlistRepository.save(Wishlist.builder().user(user).build()));
    }


    private WishlistResponseDTO toDto(Wishlist wishlist) {
        List<BookResponseDTO> items = wishlist.getItems().stream().map(wi -> toBookDto(wi)).collect(Collectors.toList());
        return WishlistResponseDTO.builder()
                .wishlistId(wishlist.getWishlistId())
                .items(items)
                .build();
    }




    private BookResponseDTO toBookDto(WishlistItem wi) {
        Book book = wi.getBook();
        int qty = inventoryRepository.findByBook_BookId(book.getBookId()).map(Inventory::getQuantityAvailable).orElse(0);
        List<String> authorNames = book.getAuthors()
                .stream()
                .map(Author::getName)
                .collect(Collectors.toList());
        List<String> categoryNames = book.getCategories()
                .stream()
                .map(Category::getName)
                .collect(Collectors.toList());

        double avgRating = book.getReviews()
                .stream().mapToInt(Review::getRating)
                .average().orElse(0.0);



        return BookResponseDTO.builder()
                .id(book.getBookId())
                .wishlistItemId(wi.getWishlistItemId())
                .title(book.getTitle())
                .price(book.getPrice())
                .specialPrice(book.getSpecialPrice())
                .author(authorNames.isEmpty() ? "Unknown" : authorNames.get(0))
                .category(categoryNames.isEmpty() ? "" : categoryNames.get(0))
                .authors(authorNames)
                .categories(categoryNames)
                .rating(Math.round(avgRating * 10.0) / 10.0)
                .reviewCount(book.getReviews().size())
                .inStock(qty > 0)
                .quantityAvailable(qty)
                .build();
    }



}
