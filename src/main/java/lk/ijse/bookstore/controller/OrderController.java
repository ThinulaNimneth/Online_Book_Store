package lk.ijse.bookstore.controller;

import jakarta.validation.Valid;
import lk.ijse.bookstore.dto.request.OrderRequestDTO;
import lk.ijse.bookstore.dto.response.CommonResponse;
import lk.ijse.bookstore.exception.ResponseCode;
import lk.ijse.bookstore.exception.ResponseMessage;
import lk.ijse.bookstore.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/v1/orders")
@CrossOrigin
@RequiredArgsConstructor
public class OrderController {

    private final OrderService  orderService;


    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public CommonResponse placeOrder(Authentication authentication, @Valid @RequestBody OrderRequestDTO dto) {
        return new CommonResponse(ResponseCode.SUCCESS, orderService.placeOrder(authentication.getName(), dto), ResponseMessage.SUCCESS);
    }


    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public CommonResponse myOrders(Authentication authentication) {
        return new CommonResponse(ResponseCode.SUCCESS, orderService.findMyOrders(authentication.getName()), ResponseMessage.SUCCESS);
    }


    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(value = "/all", produces = MediaType.APPLICATION_JSON_VALUE)
    public CommonResponse allOrders() {
        return new CommonResponse(ResponseCode.SUCCESS, orderService.findAll(), ResponseMessage.SUCCESS);
    }


    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping(value = "/{orderId}/status", produces = MediaType.APPLICATION_JSON_VALUE)
    public CommonResponse updateStatus(@PathVariable Long orderId, @RequestParam String status) {
        return new CommonResponse(ResponseCode.SUCCESS, orderService.updateStatus(orderId, status), ResponseMessage.SUCCESS);
    }

}