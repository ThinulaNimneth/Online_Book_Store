package lk.ijse.bookstore.service;

import lk.ijse.bookstore.dto.request.OrderRequestDTO;
import lk.ijse.bookstore.dto.response.OrderResponseDTO;

import java.util.List;

public interface OrderService {
    OrderResponseDTO placeOrder(String email, OrderRequestDTO dto);
    List<OrderResponseDTO> findMyOrders(String email);
    List<OrderResponseDTO> findAll();
    OrderResponseDTO updateStatus(Long orderId, String status);
}
