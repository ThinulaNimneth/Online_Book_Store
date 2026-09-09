package lk.ijse.bookstore.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderRequestDTO {


    private Long addressId;

    @Valid
    private AddressRequestDTO newAddress;

    @NotBlank(message = "paymentMethod is required")
    private String paymentMethod;
}