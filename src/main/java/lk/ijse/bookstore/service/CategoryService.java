package lk.ijse.bookstore.service;

import lk.ijse.bookstore.dto.request.CategoryRequestDTO;
import lk.ijse.bookstore.dto.response.CategoryResponseDTO;

import java.util.List;

public interface CategoryService {
    CategoryResponseDTO save(CategoryRequestDTO dto);
    CategoryResponseDTO update(Long id, CategoryRequestDTO dto);
    void delete(Long id);
    CategoryResponseDTO findById(Long id);
    List<CategoryResponseDTO> findAll();
}
