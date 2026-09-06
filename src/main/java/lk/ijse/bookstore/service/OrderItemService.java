package lk.ijse.bookstore.service;

import lk.ijse.bookstore.dto.response.OrderItemResponseDTO;

import java.util.List;

public interface OrderItemService {
    List<OrderItemResponseDTO> findByOrderId(Long orderId);
}
