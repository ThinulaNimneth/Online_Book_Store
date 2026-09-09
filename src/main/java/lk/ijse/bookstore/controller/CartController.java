package lk.ijse.bookstore.controller;

import jakarta.validation.Valid;
import lk.ijse.bookstore.dto.request.CartItemRequestDTO;
import lk.ijse.bookstore.dto.response.CommonResponse;
import lk.ijse.bookstore.exception.ResponseCode;
import lk.ijse.bookstore.exception.ResponseMessage;
import lk.ijse.bookstore.service.CartItemService;
import lk.ijse.bookstore.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/v1/carts")
@CrossOrigin
@RequiredArgsConstructor
public class CartController {
    private final CartService cartService;
    private final CartItemService cartItemService;


    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public CommonResponse getMyCart(Authentication authentication) {
        return new CommonResponse(ResponseCode.SUCCESS, cartService.getMyCart(authentication.getName()), ResponseMessage.SUCCESS);
    }



    @PostMapping(value = "/items", produces = MediaType.APPLICATION_JSON_VALUE)
    public CommonResponse addItem(Authentication authentication, @Valid @RequestBody CartItemRequestDTO dto){
        return new CommonResponse(ResponseCode.SUCCESS,cartService.addItem(authentication.getName(), dto), ResponseMessage.SUCCESS);
    }


    @PutMapping(value = "/items/{cartItemId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public CommonResponse updateItem(Authentication authentication, @PathVariable Long cartItemId,
                                     @Valid @RequestBody CartItemRequestDTO dto) {
        return new CommonResponse(ResponseCode.SUCCESS,
                cartItemService.updateQuantity(authentication.getName(), cartItemId, dto.getQuantity()), ResponseMessage.SUCCESS);
    }



    @DeleteMapping(value = "/items/{cartItemId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public CommonResponse removeItem(Authentication authentication, @PathVariable Long cartItemId) {
        cartItemService.remove(authentication.getName(), cartItemId);
        return new CommonResponse(ResponseCode.SUCCESS, ResponseMessage.SUCCESS);
    }

}