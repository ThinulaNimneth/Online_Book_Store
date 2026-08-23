package lk.ijse.bookstore.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryResponseDTO {
    private Long inventoryId;
    private Long bookId;
    private String bookTitle;
    private Integer quantityAvailable;
    private Integer reorderLevel;
}
