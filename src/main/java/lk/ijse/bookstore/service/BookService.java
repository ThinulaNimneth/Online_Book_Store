package lk.ijse.bookstore.service;

import lk.ijse.bookstore.dto.request.BookRequestDTO;
import lk.ijse.bookstore.dto.response.BookResponseDTO;

import java.util.List;

public interface BookService {
    BookResponseDTO save(BookRequestDTO dto);
    BookResponseDTO update(Long id, BookRequestDTO dto);
    void delete(Long id);
    BookResponseDTO findById(Long id);
    List<BookResponseDTO> findAll();
    List<BookResponseDTO> search(String keyword, String category);
    List<BookResponseDTO> newArrivals();
    List<BookResponseDTO> bestsellers(String range);
    List<BookResponseDTO> trending();
}
