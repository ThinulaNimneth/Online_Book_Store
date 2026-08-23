package lk.ijse.bookstore.dto.request;

import jakarta.validation.constraints.DecimalMin;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookRequestDTO {

    private String title;
    private String isbn;
    private String description;

    @DecimalMin(value = "0.0", inclusive = false, message = "price need greater than 0")
    private BigDecimal price;

    private BigDecimal specialPrice;
    private String language;
    private Integer pages;
    private Long publisherId;
    private List<Long> categoryIds;

    private List<Long> authorIds;

    private Integer quantityAvailable;

    private String imageUrl;
}
