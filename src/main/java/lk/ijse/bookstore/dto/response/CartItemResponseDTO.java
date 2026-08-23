package lk.ijse.bookstore.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItemResponseDTO {
    private Long cartItemId;
    private Long bookId;
    private String title;
    private String author;
    private BigDecimal unitPrice;
    private Integer quantity;
}
