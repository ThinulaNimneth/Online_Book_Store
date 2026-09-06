package lk.ijse.bookstore.controller;

import jakarta.validation.Valid;
import lk.ijse.bookstore.dto.request.AddressRequestDTO;
import lk.ijse.bookstore.dto.response.CommonResponse;
import lk.ijse.bookstore.exception.ResponseCode;
import lk.ijse.bookstore.exception.ResponseMessage;
import lk.ijse.bookstore.service.AddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/v1/addresses")
@CrossOrigin
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;


    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public CommonResponse getMine(Authentication authentication) {
        return new CommonResponse(ResponseCode.SUCCESS, addressService.findMine(authentication.getName()), ResponseMessage.SUCCESS);
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public CommonResponse create(Authentication authentication, @Valid @RequestBody AddressRequestDTO dto) {
        return new CommonResponse(ResponseCode.SUCCESS, addressService.save(authentication.getName(), dto), ResponseMessage.SUCCESS);
    }

}
