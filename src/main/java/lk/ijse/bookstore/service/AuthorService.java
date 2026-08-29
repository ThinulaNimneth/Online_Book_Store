package lk.ijse.bookstore.service;

import lk.ijse.bookstore.dto.request.AuthorRequestDTO;
import lk.ijse.bookstore.dto.response.AuthorResponseDTO;

import java.util.List;

public interface AuthorService {
    AuthorResponseDTO save(AuthorRequestDTO dto);
    AuthorResponseDTO update(Long id, AuthorRequestDTO dto);
    void delete(Long id);
    AuthorResponseDTO findById(Long id);
    List<AuthorResponseDTO> findAll();
}
