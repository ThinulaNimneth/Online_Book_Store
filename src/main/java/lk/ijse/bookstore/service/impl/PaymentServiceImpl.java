package lk.ijse.bookstore.service.impl;

import lk.ijse.bookstore.dto.request.PaymentRequestDTO;
import lk.ijse.bookstore.dto.response.PaymentResponseDTO;
import lk.ijse.bookstore.entity.Order;
import lk.ijse.bookstore.entity.Payment;
import lk.ijse.bookstore.entity.User;
import lk.ijse.bookstore.enumiration.PaymentMethod;
import lk.ijse.bookstore.enumiration.PaymentStatus;
import lk.ijse.bookstore.exception.CustomerException;
import lk.ijse.bookstore.exception.ResponseCode;
import lk.ijse.bookstore.exception.ResponseMessage;
import lk.ijse.bookstore.repository.OrderRepository;
import lk.ijse.bookstore.repository.PaymentRepository;
import lk.ijse.bookstore.repository.UserRepository;
import lk.ijse.bookstore.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;


    @Override
    public PaymentResponseDTO simulate(String email, PaymentRequestDTO dto){
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomerException(ResponseCode.NOT_FOUND, ResponseMessage.USER_NOT_FOUND));

        Order order = orderRepository.findById(dto.getOrderId())
                .orElseThrow(() -> new CustomerException(ResponseCode.NOT_FOUND, ResponseMessage.ORDER_NOT_FOUND));


        if (!order.getUser().getUserId().equals(user.getUserId())) {
            throw new CustomerException(ResponseCode.FORBIDDEN, ResponseMessage.ORDER_NOT_FOUND);
        }

        Payment payment = paymentRepository.findByOrder_OrderId(order.getOrderId())
                .orElseThrow(() -> new CustomerException(ResponseCode.NOT_FOUND, ResponseMessage.PAYMENT_NOT_FOUND));


        try {
            payment.setPaymentMethod(PaymentMethod.valueOf(dto.getPaymentMethod().toUpperCase()));
        } catch (IllegalArgumentException ex) {
            throw new CustomerException(ResponseCode.VALIDATION_FAILED, "Invalid payment method");
        }


        payment.setPaymentStatus(PaymentStatus.SUCCESS);
        payment.setPaidAt(LocalDateTime.now());
        payment =  paymentRepository.save(payment);

        log.info("Payment {} for order {} process as SUCCESS", payment.getPaymentId(), order.getOrderId());
        return toDto(payment);

    }





    private PaymentResponseDTO toDto(Payment p){
        return PaymentResponseDTO.builder()
                .paymentId(p.getPaymentId())
                .orderId(p.getOrder().getOrderId())
                .paymentMethod(p.getPaymentMethod().name())
                .paymentStatus(p.getPaymentStatus().name())
                .amount(p.getAmount())
                .paidAt(p.getPaidAt())
                .transactionRef(p.getTransactionRef())
                .build();
    }

}
