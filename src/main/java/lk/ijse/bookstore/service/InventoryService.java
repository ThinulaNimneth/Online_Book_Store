package lk.ijse.bookstore.service;

import lk.ijse.bookstore.dto.request.InventoryRequestDTO;
import lk.ijse.bookstore.dto.response.InventoryResponseDTO;

import java.util.List;

public interface InventoryService {
    InventoryResponseDTO upsert(InventoryRequestDTO dto);
    List<InventoryResponseDTO> findAll();
    InventoryResponseDTO findByBookId(Long bookId);
}
