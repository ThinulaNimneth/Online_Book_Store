package lk.ijse.bookstore.service.impl;

import lk.ijse.bookstore.dto.response.OrderItemResponseDTO;
import lk.ijse.bookstore.repository.OrderRepository;
import lk.ijse.bookstore.exception.CustomerException;
import lk.ijse.bookstore.exception.ResponseCode;
import lk.ijse.bookstore.exception.ResponseMessage;
import lk.ijse.bookstore.service.OrderItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderItemServiceImpl implements OrderItemService {

    private final OrderRepository orderRepository;



    @Override
    public List<OrderItemResponseDTO> findByOrderId(Long orderId) {

        var order = orderRepository.findById(orderId)
                .orElseThrow(() -> new CustomerException(ResponseCode.NOT_FOUND, ResponseMessage.ORDER_NOT_FOUND));


        return order.getItems().stream().map(oi -> OrderItemResponseDTO.builder()
                .orderItemId(oi.getOrderItemId())
                .bookId(oi.getBook().getBookId())
                .title(oi.getBook().getTitle())
                .quantity(oi.getQuantity())
                .unitPrice(oi.getUnitPrice())
                .subtotal(oi.getSubtotal())
                .build()).collect(Collectors.toList());
    }
}
