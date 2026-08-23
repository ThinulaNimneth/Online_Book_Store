package lk.ijse.bookstore.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewRequestDTO {

    @NotNull(message = "bookID required")
    private Long bookId;

    @Min(value = 1, message = " between 1 and 5")
    @Max(value = 5, message = " between 1 and 5")
    private Integer rating;

    private String comment;
}
