package lk.ijse.bookstore.service;

import lk.ijse.bookstore.dto.request.AddressRequestDTO;
import lk.ijse.bookstore.dto.response.AddressResponseDTO;

import java.util.List;

public interface AddressService {
    AddressResponseDTO save(String email, AddressRequestDTO dto);
    List<AddressResponseDTO> findMine(String email);
}
