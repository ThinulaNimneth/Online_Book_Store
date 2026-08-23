package lk.ijse.bookstore.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddressResponseDTO {
    private Long addressId;
    private String line1;
    private String line2;
    private String city;
    private String district;
    private String postalCode;
    private String country;
    private boolean isDefault;
}
