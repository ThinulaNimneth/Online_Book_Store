package lk.ijse.bookstore.service;

import lk.ijse.bookstore.dto.request.PublisherRequestDTO;
import lk.ijse.bookstore.dto.response.PublisherResponseDTO;

import java.util.List;

public interface PublisherService {
    PublisherResponseDTO save(PublisherRequestDTO dto);
    PublisherResponseDTO update(Long id, PublisherRequestDTO dto);
    void delete(Long id);
    PublisherResponseDTO findById(Long id);
    List<PublisherResponseDTO> findAll();
}
