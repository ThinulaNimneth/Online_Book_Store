package lk.ijse.bookstore.controller;

import jakarta.validation.Valid;
import lk.ijse.bookstore.dto.request.BookRequestDTO;
import lk.ijse.bookstore.dto.response.CommonResponse;
import lk.ijse.bookstore.exception.ResponseCode;
import lk.ijse.bookstore.exception.ResponseMessage;
import lk.ijse.bookstore.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/v1/books")
@CrossOrigin
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public CommonResponse getAll(@RequestParam(required = false) String keyword,
                                 @RequestParam(required = false) String category) {
        if (keyword != null || category != null) {
            return new CommonResponse(ResponseCode.SUCCESS, bookService.search(keyword, category), ResponseMessage.SUCCESS);
        }
        return new CommonResponse(ResponseCode.SUCCESS, bookService.findAll(), ResponseMessage.SUCCESS);
    }


    @GetMapping(value = "/new-arrivals", produces = MediaType.APPLICATION_JSON_VALUE)
    public CommonResponse newArrivals() {
        return new CommonResponse(ResponseCode.SUCCESS, bookService.newArrivals(), ResponseMessage.SUCCESS);
    }


    @GetMapping(value = "/bestsellers", produces = MediaType.APPLICATION_JSON_VALUE)
    public CommonResponse bestsellers(@RequestParam(required = false, defaultValue = "all") String range) {
        return new CommonResponse(ResponseCode.SUCCESS, bookService.bestsellers(range), ResponseMessage.SUCCESS);
    }


    @GetMapping(value = "/trending", produces = MediaType.APPLICATION_JSON_VALUE)
    public CommonResponse trending() {
        return new CommonResponse(ResponseCode.SUCCESS, bookService.trending(), ResponseMessage.SUCCESS);
    }


    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public CommonResponse getById(@PathVariable Long id) {
        return new CommonResponse(ResponseCode.SUCCESS, bookService.findById(id), ResponseMessage.SUCCESS);
    }


    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public CommonResponse create(@Valid @RequestBody BookRequestDTO dto) {
        return new CommonResponse(ResponseCode.SUCCESS, bookService.save(dto), ResponseMessage.SUCCESS);
    }


    @PutMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public CommonResponse update(@PathVariable Long id, @Valid @RequestBody BookRequestDTO dto) {
        return new CommonResponse(ResponseCode.SUCCESS, bookService.update(id, dto), ResponseMessage.SUCCESS);
    }

    @PatchMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public CommonResponse patch(@PathVariable Long id, @RequestBody BookRequestDTO dto) {
        return new CommonResponse(ResponseCode.SUCCESS, bookService.update(id, dto), ResponseMessage.SUCCESS);
    }

    @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public CommonResponse delete(@PathVariable Long id) {
        bookService.delete(id);
        return new CommonResponse(ResponseCode.SUCCESS, ResponseMessage.SUCCESS);
    }

}
