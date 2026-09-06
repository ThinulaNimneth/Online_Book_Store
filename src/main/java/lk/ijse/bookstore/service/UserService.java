package lk.ijse.bookstore.service;

import lk.ijse.bookstore.dto.response.UserResponseDTO;

import java.util.List;

public interface UserService {
    List<UserResponseDTO> getAllUsers();
    UserResponseDTO getById(Long userId);
}
