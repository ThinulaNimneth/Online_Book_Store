package lk.ijse.bookstore.controller;

import jakarta.validation.Valid;
import lk.ijse.bookstore.dto.request.PaymentRequestDTO;
import lk.ijse.bookstore.dto.response.CommonResponse;
import lk.ijse.bookstore.exception.ResponseCode;
import lk.ijse.bookstore.exception.ResponseMessage;
import lk.ijse.bookstore.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/v1/payments")
@CrossOrigin
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public CommonResponse simulate(Authentication authentication, @Valid @RequestBody PaymentRequestDTO dto) {
        return new CommonResponse(ResponseCode.SUCCESS, paymentService.simulate(authentication.getName(), dto), ResponseMessage.SUCCESS);
    }
}
