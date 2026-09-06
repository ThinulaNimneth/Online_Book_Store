package lk.ijse.bookstore.controller;

import jakarta.validation.Valid;
import lk.ijse.bookstore.dto.request.ReviewRequestDTO;
import lk.ijse.bookstore.dto.response.CommonResponse;
import lk.ijse.bookstore.exception.ResponseCode;
import lk.ijse.bookstore.exception.ResponseMessage;
import lk.ijse.bookstore.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/v1/reviews")
@CrossOrigin
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public CommonResponse getByBook(@RequestParam Long bookId) {
        return new CommonResponse(ResponseCode.SUCCESS, reviewService.findByBookId(bookId), ResponseMessage.SUCCESS);
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public CommonResponse create(Authentication authentication, @Valid @RequestBody ReviewRequestDTO dto) {
        return new CommonResponse(ResponseCode.SUCCESS, reviewService.save(authentication.getName(), dto), ResponseMessage.SUCCESS);
    }
}
