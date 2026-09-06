package lk.ijse.bookstore.controller;

import lk.ijse.bookstore.dto.response.CommonResponse;
import lk.ijse.bookstore.enumiration.OrderStatus;
import lk.ijse.bookstore.exception.ResponseCode;
import lk.ijse.bookstore.exception.ResponseMessage;
import lk.ijse.bookstore.repository.BookRepository;
import lk.ijse.bookstore.repository.InventoryRepository;
import lk.ijse.bookstore.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping(value = "/api/v1/admin")
@CrossOrigin
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")

public class AdminController {
    private final BookRepository bookRepository;
    private final OrderRepository orderRepository;
    private final InventoryRepository inventoryRepository;


    @GetMapping(value = "/stats", produces = MediaType.APPLICATION_JSON_VALUE)
    public CommonResponse stats(){
        LocalDateTime monthStart = LocalDateTime.now().with(TemporalAdjusters.firstDayOfMonth())
                .toLocalDate().atStartOfDay();

        long totalBooks = bookRepository.count();

        var ordersThisMonth = orderRepository.findAll().stream()
                .filter(o -> o.getOrderDate() != null && !o.getOrderDate().isBefore(monthStart))
                .toList();

        BigDecimal revenueThisMonth = ordersThisMonth.stream()
                .filter(o -> o.getOrderStatus() != OrderStatus.CANCELLED)
                .map(o -> o.getTotalAmount() == null ? BigDecimal.ZERO : o.getTotalAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);


        long lowStockItems = inventoryRepository.findAll().stream()
                .filter(i -> i.getQuantityAvailable() != null && i.getReorderLevel() != null
                        && i.getQuantityAvailable() <= i.getReorderLevel())
                .count();


        Map<String, Object> body = new HashMap<>();
        body.put("totalBooks", totalBooks);
        body.put("ordersThisMonth", ordersThisMonth.size());
        body.put("revenueThisMonth", revenueThisMonth);
        body.put("lowStockItems", lowStockItems);


        return  new CommonResponse(ResponseCode.SUCCESS, body, ResponseMessage.SUCCESS);
    }
}
