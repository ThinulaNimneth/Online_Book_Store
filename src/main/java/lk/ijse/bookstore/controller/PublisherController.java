package lk.ijse.bookstore.controller;

import jakarta.validation.Valid;
import lk.ijse.bookstore.dto.request.PublisherRequestDTO;
import lk.ijse.bookstore.dto.response.CommonResponse;
import lk.ijse.bookstore.exception.ResponseCode;
import lk.ijse.bookstore.exception.ResponseMessage;
import lk.ijse.bookstore.service.PublisherService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/v1/publishers")
@CrossOrigin
@RequiredArgsConstructor
public class PublisherController {
    private final PublisherService publisherService;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public CommonResponse getAll() {
        return new CommonResponse(ResponseCode.SUCCESS, publisherService.findAll(), ResponseMessage.SUCCESS);
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public CommonResponse getById(@PathVariable Long id) {
        return new CommonResponse(ResponseCode.SUCCESS, publisherService.findById(id), ResponseMessage.SUCCESS);
    }


    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public CommonResponse create(@Valid @RequestBody PublisherRequestDTO dto) {
        return new CommonResponse(ResponseCode.SUCCESS, publisherService.save(dto), ResponseMessage.SUCCESS);
    }



    @PutMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public CommonResponse update(@PathVariable Long id, @Valid @RequestBody PublisherRequestDTO dto) {
        return new CommonResponse(ResponseCode.SUCCESS, publisherService.update(id, dto), ResponseMessage.SUCCESS);
    }

    @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public CommonResponse delete(@PathVariable Long id) {
        publisherService.delete(id);
        return new CommonResponse(ResponseCode.SUCCESS, ResponseMessage.SUCCESS);
    }
}
