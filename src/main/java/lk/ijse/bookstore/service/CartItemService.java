package lk.ijse.bookstore.service;

import lk.ijse.bookstore.dto.response.CartResponseDTO;

public interface CartItemService {
    CartResponseDTO updateQuantity(String email, Long cartItemId, Integer quantity);
    void remove(String email, Long cartItemId);
}
