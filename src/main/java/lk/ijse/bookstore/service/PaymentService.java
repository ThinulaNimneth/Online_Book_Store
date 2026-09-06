package lk.ijse.bookstore.service;

import lk.ijse.bookstore.dto.request.PaymentRequestDTO;
import lk.ijse.bookstore.dto.response.PaymentResponseDTO;

public interface PaymentService {
    PaymentResponseDTO simulate(String email, PaymentRequestDTO dto);
}
