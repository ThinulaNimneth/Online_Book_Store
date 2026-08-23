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
public class PublisherRequestDTO {

    @NotBlank(message = "publisher name required")
    private String name;
    private String contactEmail;
}
