package lk.ijse.bookstore.service;

import lk.ijse.bookstore.dto.request.ReviewRequestDTO;
import lk.ijse.bookstore.dto.response.ReviewResponseDTO;

import java.util.List;

public interface ReviewService {
    ReviewResponseDTO save(String email, ReviewRequestDTO dto);
    List<ReviewResponseDTO> findByBookId(Long bookId);
}
