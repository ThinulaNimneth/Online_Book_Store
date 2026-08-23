package lk.ijse.bookstore.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryRequestDTO {

    @NotNull(message = "bookID required")
    private Long bookId;

    @NotNull(message = "quantity required")
    private Integer quantityAvailable;

    private Integer reorderLevel;
}
