package lk.ijse.bookstore.controller;

import jakarta.validation.Valid;
import lk.ijse.bookstore.dto.request.InventoryRequestDTO;
import lk.ijse.bookstore.dto.response.CommonResponse;
import lk.ijse.bookstore.exception.ResponseCode;
import lk.ijse.bookstore.exception.ResponseMessage;
import lk.ijse.bookstore.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/v1/inventory")
@CrossOrigin
@RequiredArgsConstructor
public class InventoryController {
    private final InventoryService inventoryService;


    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public CommonResponse getAll() {
        return new CommonResponse(ResponseCode.SUCCESS, inventoryService.findAll(), ResponseMessage.SUCCESS);
    }

    @GetMapping(value = "/book/{bookId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public CommonResponse getByBook(@PathVariable Long bookId) {
        return new CommonResponse(ResponseCode.SUCCESS, inventoryService.findByBookId(bookId), ResponseMessage.SUCCESS);
    }


    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public CommonResponse upsert(@Valid @RequestBody InventoryRequestDTO dto) {
        return new CommonResponse(ResponseCode.SUCCESS, inventoryService.upsert(dto), ResponseMessage.SUCCESS);
    }


    @PutMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public CommonResponse update(@Valid @RequestBody InventoryRequestDTO dto) {
        return new CommonResponse(ResponseCode.SUCCESS, inventoryService.upsert(dto), ResponseMessage.SUCCESS);
    }
}
