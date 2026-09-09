package lk.ijse.bookstore.service.impl;

import lk.ijse.bookstore.dto.request.AuthRequestDTO;
import lk.ijse.bookstore.dto.request.RegisterRequestDTO;
import lk.ijse.bookstore.dto.response.JwtResponseDTO;
import lk.ijse.bookstore.entity.Cart;
import lk.ijse.bookstore.entity.Role;
import lk.ijse.bookstore.entity.User;
import lk.ijse.bookstore.entity.Wishlist;
import lk.ijse.bookstore.enumiration.RoleType;
import lk.ijse.bookstore.exception.CustomerException;
import lk.ijse.bookstore.exception.ResponseCode;
import lk.ijse.bookstore.exception.ResponseMessage;
import lk.ijse.bookstore.repository.CartRepository;
import lk.ijse.bookstore.repository.UserRepository;
import lk.ijse.bookstore.repository.WishlistRepository;
import lk.ijse.bookstore.security.JwtUtil;
import lk.ijse.bookstore.service.AuthService;
import lk.ijse.bookstore.service.RoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final WishlistRepository wishlistRepository;
    private final RoleService roleService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;


    @Override
    public JwtResponseDTO register(RegisterRequestDTO dto){
        try {
            if (userRepository.existsByEmail(dto.getEmail())) {
                throw new CustomerException(ResponseCode.CONFLICT, ResponseMessage.EMAIL_ALREADY_REGISTERED);
            }

            Role userRole = roleService.getOrCreate(RoleType.USER);
            Set<Role> roles = new HashSet<>();
            roles.add(userRole);

            User user = User.builder()
                    .fullName(dto.getFullName())
                    .email(dto.getEmail())
                    .password(passwordEncoder.encode(dto.getPassword()))
                    .phone(dto.getPhone())
                    .enabled(true)
                    .roles(roles)
                    .build();

            user = userRepository.save(user);

            cartRepository.save(Cart.builder().user(user).build());
            wishlistRepository.save(Wishlist.builder().user(user).build());

            log.info("Registered new user {}", user.getEmail());
            return buildTokenResponse(user);

        } catch (CustomerException ce) {
            throw ce;
        } catch (Exception ex) {
            log.error("register() failed", ex);
            throw new CustomerException(ResponseCode.INTERNAL_ERROR, ResponseMessage.UNEXPECTED_ERROR);
        }
    }


    @Override
    public JwtResponseDTO login(AuthRequestDTO dto) {
        try {
            User user = userRepository.findByEmail(dto.getEmail())
                    .orElseThrow(() -> new CustomerException(ResponseCode.UNAUTHORIZED, ResponseMessage.INVALID_CREDENTIALS));

            if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
                throw new CustomerException(ResponseCode.UNAUTHORIZED, ResponseMessage.INVALID_CREDENTIALS);
            }

            log.info("User {} logged in", user.getEmail());
            return buildTokenResponse(user);

        } catch (CustomerException ce) {
            throw ce;
        } catch (Exception ex) {
            log.error("login() failed", ex);
            throw new CustomerException(ResponseCode.INTERNAL_ERROR, ResponseMessage.UNEXPECTED_ERROR);
        }
    }



    private JwtResponseDTO buildTokenResponse(User user) {
        List<String> roleNames = user.getRoles().stream()
                .map(r -> r.getRoleName().name())
                .collect(Collectors.toList());

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getUserId());
        claims.put("roles", roleNames);

        String token = jwtUtil.generateToken(user.getEmail(), claims);

        return JwtResponseDTO.builder()
                .token(token)
                .userId(user.getUserId())
                .fullName(user.getFullName())
                .roles(roleNames)
                .build();
    }


}

