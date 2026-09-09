package lk.ijse.bookstore.service.impl;

import lk.ijse.bookstore.dto.request.InventoryRequestDTO;
import lk.ijse.bookstore.dto.response.InventoryResponseDTO;
import lk.ijse.bookstore.entity.Book;
import lk.ijse.bookstore.entity.Inventory;
import lk.ijse.bookstore.exception.CustomerException;
import lk.ijse.bookstore.exception.ResponseCode;
import lk.ijse.bookstore.exception.ResponseMessage;
import lk.ijse.bookstore.repository.BookRepository;
import lk.ijse.bookstore.repository.InventoryRepository;
import lk.ijse.bookstore.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;
    private final BookRepository bookRepository;


    @Override
    public InventoryResponseDTO upsert(InventoryRequestDTO dto) {
        Book book = bookRepository.findById(dto.getBookId())
                .orElseThrow(() -> new CustomerException(ResponseCode.NOT_FOUND, ResponseMessage.BOOK_NOT_FOUND));

        Inventory inventory = inventoryRepository.findByBook_BookId(book.getBookId())
                .orElseGet(() -> Inventory.builder().book(book).build());

        inventory.setQuantityAvailable(dto.getQuantityAvailable());
        if (dto.getReorderLevel() != null) {
            inventory.setReorderLevel(dto.getReorderLevel());
        }

        inventory = inventoryRepository.save(inventory);
        log.info("Stock for book {} set to {}", book.getBookId(), inventory.getQuantityAvailable());
        return toDto(inventory);
    }



    @Override
    public List<InventoryResponseDTO> findAll() {
        return inventoryRepository.findAll()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }



    @Override
    public InventoryResponseDTO findByBookId(Long bookId) {
        Inventory inventory = inventoryRepository.findByBook_BookId(bookId)
                .orElseThrow(() -> new CustomerException(ResponseCode.NOT_FOUND, ResponseMessage.INVENTORY_NOT_FOUND));
        return toDto(inventory);
    }



    private InventoryResponseDTO toDto(Inventory i) {
        return InventoryResponseDTO.builder()
                .inventoryId(i.getInventoryId())
                .bookId(i.getBook().getBookId())
                .bookTitle(i.getBook().getTitle())
                .quantityAvailable(i.getQuantityAvailable())
                .reorderLevel(i.getReorderLevel())
                .build();
    }
}





