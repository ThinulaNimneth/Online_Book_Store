package lk.ijse.bookstore.service.impl;

import lk.ijse.bookstore.dto.response.CartItemResponseDTO;
import lk.ijse.bookstore.dto.response.CartResponseDTO;
import lk.ijse.bookstore.entity.Author;
import lk.ijse.bookstore.entity.Cart;
import lk.ijse.bookstore.entity.CartItem;
import lk.ijse.bookstore.entity.User;
import lk.ijse.bookstore.exception.CustomerException;
import lk.ijse.bookstore.exception.ResponseCode;
import lk.ijse.bookstore.exception.ResponseMessage;
import lk.ijse.bookstore.repository.CartItemRepository;
import lk.ijse.bookstore.repository.CartRepository;
import lk.ijse.bookstore.repository.UserRepository;
import lk.ijse.bookstore.service.CartItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartItemServiceImpl implements CartItemService {
    private final CartItemRepository cartItemRepository;
    private final CartRepository cartRepository;
    private final UserRepository userRepository;


    @Override
    public CartResponseDTO updateQuantity(String email, Long cartItemId, Integer quantity){
        CartItem item =  ownedItem(email, cartItemId);
        item.setQuantity(Math.max(1, quantity));
        cartItemRepository.save(item);
        return toDto(item.getCart());
    }


    @Override
    public void remove(String email, Long cartItemId) {
        CartItem item = ownedItem(email, cartItemId);
        cartItemRepository.delete(item);
    }



    private CartItem ownedItem(String email, Long cartItemId){
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomerException(ResponseCode.NOT_FOUND, ResponseMessage.USER_NOT_FOUND));
        CartItem item =  cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new CustomerException(ResponseCode.NOT_FOUND, ResponseMessage.CART_ITEM_NOT_FOUND));


        if (!item.getCart().getUser().getUserId().equals(user.getUserId())) {
            throw new CustomerException(ResponseCode.FORBIDDEN, ResponseMessage.CART_ITEM_NOT_FOUND);
        }

        return item;
    }



    private CartResponseDTO toDto(Cart cart) {
        List<CartItemResponseDTO> items = cart.getItems().stream().map(this::toItemDto).collect(Collectors.toList());
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
        String authorName = item.getBook().getAuthors().stream().findFirst().map(Author::getName).orElse("dont_know");
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
