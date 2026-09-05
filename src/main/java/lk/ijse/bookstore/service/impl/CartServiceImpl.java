package lk.ijse.bookstore.service.impl;

import lk.ijse.bookstore.dto.request.CartItemRequestDTO;
import lk.ijse.bookstore.dto.response.CartItemResponseDTO;
import lk.ijse.bookstore.dto.response.CartResponseDTO;
import lk.ijse.bookstore.entity.*;
import lk.ijse.bookstore.exception.CustomerException;
import lk.ijse.bookstore.exception.ResponseCode;
import lk.ijse.bookstore.exception.ResponseMessage;
import lk.ijse.bookstore.repository.*;
import lk.ijse.bookstore.service.CartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final InventoryRepository inventoryRepository;
    private final WishlistItemRepository wishlistItemRepository;

    @Override
    public CartResponseDTO getMyCart(String email) {
        Cart cart = getOrCreateCart(email);
        return toDto(cart);
    }



    @Override
    public CartResponseDTO addItem(String email, CartItemRequestDTO dto) {
        Cart cart = getOrCreateCart(email);

        Long bookId = dto.getBookId();
        Integer quantity = dto.getQuantity() == null ? 1 : dto.getQuantity();


        if (bookId == null && dto.getWishlistItemId() != null) {
            WishlistItem wishlistItem = wishlistItemRepository.findById(dto.getWishlistItemId())
                    .orElseThrow(() -> new CustomerException(ResponseCode.NOT_FOUND, ResponseMessage.WISHLIST_ITEM_NOT_FOUND));
            bookId = wishlistItem.getBook().getBookId();
            wishlistItemRepository.delete(wishlistItem);
        }

        if (bookId == null) {
            throw new CustomerException(ResponseCode.VALIDATION_FAILED, "book_id needed");
        }

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new CustomerException(ResponseCode.NOT_FOUND, ResponseMessage.BOOK_NOT_FOUND));

        int available = inventoryRepository.findByBook_BookId(bookId)
                .map(Inventory::getQuantityAvailable).orElse(0);

        if (available <= 0) {
            throw new CustomerException(ResponseCode.CONFLICT, ResponseMessage.OUT_OF_STOCK);
        }

        BigDecimal unitPrice = (book.getSpecialPrice() != null && book.getSpecialPrice().compareTo(book.getPrice()) < 0)
                ? book.getSpecialPrice() : book.getPrice();

        CartItem item = cartItemRepository.findByCart_CartIdAndBook_BookId(cart.getCartId(), bookId)
                .orElseGet(() -> CartItem.builder()
                        .cart(cart)
                        .book(book)
                        .quantity(0)
                        .unitPrice(unitPrice)
                        .build());

        item.setQuantity(item.getQuantity() + quantity);
        item.setUnitPrice(unitPrice);
        cartItemRepository.save(item);

        log.info("added {}  book {} to_cart {}", quantity, bookId, email);
        return toDto(cartRepository.findById(cart.getCartId()).orElse(cart));
    }



    private Cart getOrCreateCart(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomerException(ResponseCode.NOT_FOUND, ResponseMessage.USER_NOT_FOUND));
        return cartRepository.findByUser_UserId(user.getUserId())
                .orElseGet(() -> cartRepository.save(Cart.builder().user(user).build()));
    }



    private CartResponseDTO toDto(Cart cart) {
        List<CartItemResponseDTO> items = cart.getItems()
                .stream().map(this::toItemDto)
                .collect(Collectors.toList());

        BigDecimal subtotal = items.stream()
                .map(i -> i.getUnitPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return CartResponseDTO.builder()
                .cartId(cart.getCartId())
                .items(items)
                .subtotal(subtotal)
                .build();
    }

    private CartItemResponseDTO toItemDto(CartItem item) {
        String authorName = item.getBook().getAuthors().stream().findFirst().map(Author::getName).orElse("Unknown");
        return CartItemResponseDTO.builder()
                .cartItemId(item.getCartItemId())
                .bookId(item.getBook().getBookId())
                .title(item.getBook().getTitle())
                .author(authorName)
                .unitPrice(item.getUnitPrice())
                .quantity(item.getQuantity())
                .build();
    }
}
