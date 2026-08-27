package lk.ijse.bookstore.service;

import lk.ijse.bookstore.dto.request.AuthRequestDTO;
import lk.ijse.bookstore.dto.request.RegisterRequestDTO;
import lk.ijse.bookstore.dto.response.JwtResponseDTO;

public interface AuthService {
    JwtResponseDTO register(RegisterRequestDTO dto);
    JwtResponseDTO login(AuthRequestDTO dto);
}
