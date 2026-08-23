package lk.ijse.bookstore.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterRequestDTO {

    @NotBlank(message = "need the full name")
    private String fullName;

    @NotBlank(message = "email is need")
    @Email(message = "valid email need")
    private String email;

    @NotBlank(message = "password required")
    private String password;

    private String phone;
}
