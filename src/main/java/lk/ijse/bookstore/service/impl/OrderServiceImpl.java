package lk.ijse.bookstore.service.impl;

import lk.ijse.bookstore.dto.request.AddressRequestDTO;
import lk.ijse.bookstore.dto.request.OrderRequestDTO;
import lk.ijse.bookstore.dto.response.OrderItemResponseDTO;
import lk.ijse.bookstore.dto.response.OrderResponseDTO;
import lk.ijse.bookstore.entity.*;
import lk.ijse.bookstore.enumiration.OrderStatus;
import lk.ijse.bookstore.enumiration.PaymentMethod;
import lk.ijse.bookstore.enumiration.PaymentStatus;
import lk.ijse.bookstore.exception.CustomerException;
import lk.ijse.bookstore.exception.ResponseCode;
import lk.ijse.bookstore.exception.ResponseMessage;
import lk.ijse.bookstore.repository.*;
import lk.ijse.bookstore.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl  implements OrderService {
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final AddressRepository addressRepository;
    private final InventoryRepository inventoryRepository;
    private final PaymentRepository paymentRepository;




    public OrderResponseDTO placeOrder(String email, OrderRequestDTO dto){

        try {
            User user = userRepository.findByEmail(email)
                    .orElseThrow(()->new CustomerException(ResponseCode.NOT_FOUND, ResponseMessage.USER_NOT_FOUND));


            Cart cart = cartRepository.findByUser_UserId(user.getUserId())
                    .orElseThrow(()->new CustomerException(ResponseCode.NOT_FOUND, ResponseMessage.CART_NOT_FOUND));

            if (cart.getItems().isEmpty()){
                throw new CustomerException(ResponseCode.VALIDATION_FAILED, ResponseMessage.CART_EMPTY);
            }


            Address address = resolveAddress(user,dto);

            Order order = Order.builder()
                    .user(user)
                    .address(address)
                    .orderStatus(OrderStatus.PENDING)
                    .totalAmount(BigDecimal.ZERO)
                    .build();    //remove this builderpattern

            order = orderRepository.save(order);


            BigDecimal total = BigDecimal.ZERO;
            for (CartItem cartItem : cart.getItems()){
                Inventory inventory = inventoryRepository.findByBook_BookId(cartItem.getBook().getBookId())
                        .orElseThrow(()->new CustomerException(ResponseCode.CONFLICT, ResponseMessage.OUT_OF_STOCK));

                if (inventory.getQuantityAvailable() < cartItem.getQuantity()){
                    throw new CustomerException(ResponseCode.CONFLICT, ResponseMessage.OUT_OF_STOCK);
                }
                inventory.setQuantityAvailable(inventory.getQuantityAvailable() - cartItem.getQuantity());
                inventoryRepository.save(inventory);


                BigDecimal subtotal = cartItem.getUnitPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()));
                OrderItem orderItem = OrderItem.builder()
                        .order(order)
                        .book(cartItem.getBook())
                        .quantity(cartItem.getQuantity())
                        .unitPrice(cartItem.getUnitPrice())
                        .subtotal(subtotal)
                        .build();
                order.getItems().add(orderItem);
                total = total.add(subtotal);
            }


            order.setTotalAmount(total);
            order.setOrderStatus(OrderStatus.CONFIRMED);
            order = orderRepository.save(order);



            PaymentMethod method = parsePaymentMethod(dto.getPaymentMethod());
            Payment payment = Payment.builder()
                    .order(order)
                    .paymentMethod(method)
                    .paymentStatus(method == PaymentMethod.COD ? PaymentStatus.PENDING : PaymentStatus.SUCCESS)
                    .amount(total)
                    .transactionRef("TXN-" + order.getOrderId() + "-" + System.currentTimeMillis())
                    .build();
            paymentRepository.save(payment);

            cartItemRepository.deleteAll(cart.getItems());
            cart.getItems().clear();
            cartRepository.save(cart);


            log.info("Order {} placed by {} for {}", order.getOrderId(), email, total);
            return toDto(order);


        }catch (CustomerException ce){
            throw ce;
        }catch (Exception ex){
            log.error("placeOrder() failed", ex);
            throw new CustomerException(ResponseCode.INTERNAL_ERROR, ResponseMessage.UNEXPECTED_ERROR);
        }


    }



    @Override
    public List<OrderResponseDTO> findMyOrders(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomerException(ResponseCode.NOT_FOUND, ResponseMessage.USER_NOT_FOUND));

        return orderRepository.findByUser_UserIdOrderByOrderDateDesc(user.getUserId())
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }



    @Override
    public List<OrderResponseDTO> findAll() {
        return orderRepository.findAll()
                .stream()  //stream api
                .map(this::toDto)
                .collect(Collectors.toList());
    }



    @Override
    public OrderResponseDTO updateStatus(Long orderId, String status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new CustomerException(ResponseCode.NOT_FOUND, ResponseMessage.ORDER_NOT_FOUND));

        try {
            order.setOrderStatus(OrderStatus.valueOf(status.toUpperCase()));
        } catch (IllegalArgumentException ex) {
            throw new CustomerException(ResponseCode.VALIDATION_FAILED, "invalid order status: " + status);
        }
        return toDto(orderRepository.save(order));
    }






    private Address resolveAddress(User user, OrderRequestDTO dto){
        if (dto.getAddressId() != null) {
            return addressRepository.findById(dto.getAddressId())
                    .orElseThrow(() -> new CustomerException(ResponseCode.NOT_FOUND, ResponseMessage.ADDRESS_NOT_FOUND));
        }

        AddressRequestDTO na = dto.getNewAddress();

        if (na == null || na.getLine1() == null || na.getLine1().isBlank()) {
            throw new CustomerException(ResponseCode.VALIDATION_FAILED, "shipping address needed");
        }
        Address address = Address.builder()
                .user(user)
                .line1(na.getLine1())
                .line2(na.getLine2())
                .city(na.getCity())
                .district(na.getDistrict())
                .postalCode(na.getPostalCode())
                .country(na.getCountry() == null || na.getCountry().isBlank() ? "Sri Lanka" : na.getCountry())
                .build();
        return addressRepository.save(address);

    }


    private PaymentMethod parsePaymentMethod(String raw){
        if (raw == null) return PaymentMethod.COD;
        try {
            return PaymentMethod.valueOf(raw.toUpperCase());
        } catch (IllegalArgumentException ex) {
            return PaymentMethod.COD;
        }

    }


    private OrderResponseDTO  toDto(Order order){
        List<OrderItemResponseDTO> items = order.getItems().stream().map(oi -> OrderItemResponseDTO.builder()
                .orderItemId(oi.getOrderItemId())
                .bookId(oi.getBook().getBookId())
                .title(oi.getBook().getTitle())
                .quantity(oi.getQuantity())
                .unitPrice(oi.getUnitPrice())
                .subtotal(oi.getSubtotal())
                .build()).collect(Collectors.toList());

        return OrderResponseDTO.builder()
                .orderId(order.getOrderId())
                .customerName(order.getUser() != null ? order.getUser().getFullName() : null)
                .orderStatus(order.getOrderStatus().name())
                .totalAmount(order.getTotalAmount())
                .orderDate(order.getOrderDate())
                .items(items)
                .build();
    }



}


