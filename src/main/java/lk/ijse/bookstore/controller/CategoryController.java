package lk.ijse.bookstore.controller;

import jakarta.validation.Valid;
import lk.ijse.bookstore.dto.request.CategoryRequestDTO;
import lk.ijse.bookstore.dto.response.CommonResponse;
import lk.ijse.bookstore.exception.ResponseCode;
import lk.ijse.bookstore.exception.ResponseMessage;
import lk.ijse.bookstore.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/v1/categories")
@CrossOrigin
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public CommonResponse getAll() {
        return new CommonResponse(ResponseCode.SUCCESS, categoryService.findAll(), ResponseMessage.SUCCESS);
    }


    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public CommonResponse getById(@PathVariable Long id) {
        return new CommonResponse(ResponseCode.SUCCESS, categoryService.findById(id), ResponseMessage.SUCCESS);
    }


    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public CommonResponse create(@Valid @RequestBody CategoryRequestDTO dto) {
        return new CommonResponse(ResponseCode.SUCCESS, categoryService.save(dto), ResponseMessage.SUCCESS);
    }



    @PutMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public CommonResponse update(@PathVariable Long id, @Valid @RequestBody CategoryRequestDTO dto) {
        return new CommonResponse(ResponseCode.SUCCESS, categoryService.update(id, dto), ResponseMessage.SUCCESS);
    }



    @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public CommonResponse delete(@PathVariable Long id) {
        categoryService.delete(id);
        return new CommonResponse(ResponseCode.SUCCESS, ResponseMessage.SUCCESS);
    }
}
