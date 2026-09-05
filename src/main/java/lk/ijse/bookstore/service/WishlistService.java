package lk.ijse.bookstore.service;

import lk.ijse.bookstore.dto.request.WishlistItemRequestDTO;
import lk.ijse.bookstore.dto.response.WishlistResponseDTO;

public interface WishlistService {
    WishlistResponseDTO getMyWishlist(String email);
    WishlistResponseDTO addItem(String email, WishlistItemRequestDTO dto);
}
