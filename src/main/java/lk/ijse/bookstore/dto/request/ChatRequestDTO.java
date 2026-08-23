package lk.ijse.bookstore.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRequestDTO {
    @NotBlank(message = "message  required")
    private String message;

    private List<ChatTurnDTO> history;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ChatTurnDTO {
        private String role;
        private String content;
    }
}
