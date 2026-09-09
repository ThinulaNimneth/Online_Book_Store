package lk.ijse.bookstore.controller;

import jakarta.validation.Valid;
import lk.ijse.bookstore.dto.request.AuthRequestDTO;
import lk.ijse.bookstore.dto.request.RegisterRequestDTO;
import lk.ijse.bookstore.dto.response.CommonResponse;
import lk.ijse.bookstore.exception.ResponseCode;
import lk.ijse.bookstore.exception.ResponseMessage;
import lk.ijse.bookstore.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/api/v1/auth")
@CrossOrigin
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;


    @PostMapping(value = "/register", produces = MediaType.APPLICATION_JSON_VALUE)
    public CommonResponse register(@Valid @RequestBody RegisterRequestDTO dto) {
        return new CommonResponse(ResponseCode.SUCCESS, authService.register(dto), ResponseMessage.SUCCESS);
    }


    @PostMapping(value = "/login", produces = MediaType.APPLICATION_JSON_VALUE)
    public CommonResponse login(@Valid @RequestBody AuthRequestDTO dto) {
        return new CommonResponse(ResponseCode.SUCCESS, authService.login(dto), ResponseMessage.SUCCESS);
    }




    @GetMapping(value = "/whoami", produces = MediaType.APPLICATION_JSON_VALUE)
    public CommonResponse whoAmI(Authentication authentication) {
        List<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        Map<String, Object> body = new HashMap<>();
        body.put("authenticated", authentication.isAuthenticated() && !roles.contains("ROLE_GUEST"));
        body.put("username", "anonymousUser".equals(authentication.getName()) ? null : authentication.getName());
        body.put("roles", roles);

        return new CommonResponse(ResponseCode.SUCCESS, body, ResponseMessage.SUCCESS);
    }
}