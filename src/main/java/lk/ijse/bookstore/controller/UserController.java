package lk.ijse.bookstore.controller;

import lk.ijse.bookstore.dto.response.CommonResponse;
import lk.ijse.bookstore.exception.ResponseCode;
import lk.ijse.bookstore.exception.ResponseMessage;
import lk.ijse.bookstore.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/v1/admin/users")
@CrossOrigin
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;


    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public CommonResponse getAll() {
        return new CommonResponse(ResponseCode.SUCCESS, userService.getAllUsers(), ResponseMessage.SUCCESS);
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public CommonResponse getById(@PathVariable Long id) {
        return new CommonResponse(ResponseCode.SUCCESS, userService.getById(id), ResponseMessage.SUCCESS);
    }
}
