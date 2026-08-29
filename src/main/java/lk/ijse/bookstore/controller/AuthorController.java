package lk.ijse.bookstore.controller;

import jakarta.validation.Valid;
import lk.ijse.bookstore.dto.request.AuthorRequestDTO;
import lk.ijse.bookstore.dto.response.CommonResponse;
import lk.ijse.bookstore.exception.ResponseCode;
import lk.ijse.bookstore.exception.ResponseMessage;
import lk.ijse.bookstore.service.AuthorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/v1/authors")
@CrossOrigin
@RequiredArgsConstructor
public class AuthorController {

    private final AuthorService authorService;


    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public CommonResponse getAll() {
        return new CommonResponse(ResponseCode.SUCCESS, authorService.findAll(), ResponseMessage.SUCCESS);
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public CommonResponse getById(@PathVariable Long id) {
        return new CommonResponse(ResponseCode.SUCCESS, authorService.findById(id), ResponseMessage.SUCCESS);
    }


    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public CommonResponse create(@Valid @RequestBody AuthorRequestDTO dto) {
        return new CommonResponse(ResponseCode.SUCCESS, authorService.save(dto), ResponseMessage.SUCCESS);
    }


    @PutMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public CommonResponse update(@PathVariable Long id, @Valid @RequestBody AuthorRequestDTO dto) {
        return new CommonResponse(ResponseCode.SUCCESS, authorService.update(id, dto), ResponseMessage.SUCCESS);
    }

    @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public CommonResponse delete(@PathVariable Long id) {
        authorService.delete(id);
        return new CommonResponse(ResponseCode.SUCCESS, ResponseMessage.SUCCESS);
    }
}
