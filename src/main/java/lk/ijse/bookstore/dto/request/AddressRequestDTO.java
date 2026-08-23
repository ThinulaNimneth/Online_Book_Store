package lk.ijse.bookstore.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddressRequestDTO {

    @NotBlank(message = "address line01 required")
    private String line1;

    private String line2;

    @NotBlank(message = "city required to enter")
    private String city;

    private String district;
    private String postalCode;
    private String country;
    private Boolean isDefault;
}
