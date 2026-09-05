package lk.ijse.bookstore.service;

import lk.ijse.bookstore.dto.request.CartItemRequestDTO;
import lk.ijse.bookstore.dto.response.CartResponseDTO;

public interface CartService {
    CartResponseDTO getMyCart(String email);
    CartResponseDTO addItem(String email, CartItemRequestDTO dto);
}
