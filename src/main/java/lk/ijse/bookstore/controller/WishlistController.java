package lk.ijse.bookstore.controller;

import lk.ijse.bookstore.dto.request.WishlistItemRequestDTO;
import lk.ijse.bookstore.dto.response.CommonResponse;
import lk.ijse.bookstore.exception.ResponseCode;
import lk.ijse.bookstore.exception.ResponseMessage;
import lk.ijse.bookstore.service.WishlistItemService;
import lk.ijse.bookstore.service.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/v1/wishlists")
@CrossOrigin
@RequiredArgsConstructor
public class WishlistController {
    private final WishlistService wishlistService;
    private final WishlistItemService wishlistItemService;


    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public CommonResponse getMyWishlist(Authentication authentication) {
        return new CommonResponse(ResponseCode.SUCCESS, wishlistService.getMyWishlist(authentication.getName()), ResponseMessage.SUCCESS);
    }


    @PostMapping(value = "/items", produces = MediaType.APPLICATION_JSON_VALUE)
    public CommonResponse addItem(Authentication authentication, @RequestBody WishlistItemRequestDTO dto) {
        return new CommonResponse(ResponseCode.SUCCESS, wishlistService.addItem(authentication.getName(), dto), ResponseMessage.SUCCESS);
    }


    @DeleteMapping(value = "/items/{wishlistItemId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public CommonResponse removeItem(Authentication authentication, @PathVariable Long wishlistItemId) {
        wishlistItemService.remove(authentication.getName(), wishlistItemId);
        return new CommonResponse(ResponseCode.SUCCESS, ResponseMessage.SUCCESS);
    }
}
