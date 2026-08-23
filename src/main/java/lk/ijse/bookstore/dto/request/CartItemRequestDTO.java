package lk.ijse.bookstore.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItemRequestDTO {

    private Long bookId;
    @Builder.Default
    private Integer quantity = 1;
    private Long wishlistItemId;
}
