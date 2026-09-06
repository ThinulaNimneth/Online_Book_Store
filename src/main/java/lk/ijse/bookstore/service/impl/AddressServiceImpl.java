package lk.ijse.bookstore.service.impl;

import lk.ijse.bookstore.dto.request.AddressRequestDTO;
import lk.ijse.bookstore.dto.response.AddressResponseDTO;
import lk.ijse.bookstore.entity.Address;
import lk.ijse.bookstore.entity.User;
import lk.ijse.bookstore.exception.CustomerException;
import lk.ijse.bookstore.exception.ResponseCode;
import lk.ijse.bookstore.exception.ResponseMessage;
import lk.ijse.bookstore.repository.AddressRepository;
import lk.ijse.bookstore.repository.UserRepository;
import lk.ijse.bookstore.service.AddressService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AddressServiceImpl implements AddressService {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    @Override
    public AddressResponseDTO save(String email, AddressRequestDTO dto){
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomerException(ResponseCode.NOT_FOUND, ResponseMessage.USER_NOT_FOUND));


        Address address = Address.builder()
                .user(user)
                .line1(dto.getLine1())
                .line2(dto.getLine2())
                .city(dto.getCity())
                .district(dto.getDistrict())
                .postalCode(dto.getPostalCode())
                .country(dto.getCountry() == null || dto.getCountry().isBlank() ? "sri_Lanka" : dto.getCountry())
                .isDefault(Boolean.TRUE.equals(dto.getIsDefault()))
                .build();

        address = addressRepository.save(address);
        log.info("Address saved for {}", email);
        return  toDto(address);
    }


    @Override
    public List<AddressResponseDTO> findMine(String email){
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomerException(ResponseCode.NOT_FOUND, ResponseMessage.USER_NOT_FOUND));
        return addressRepository.findByUser_UserId(user.getUserId())
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }


    private AddressResponseDTO toDto(Address a){
        return AddressResponseDTO.builder()
                .addressId(a.getAddressId())
                .line1(a.getLine1())
                .line2(a.getLine2())
                .city(a.getCity())
                .district(a.getDistrict())
                .postalCode(a.getPostalCode())
                .country(a.getCountry())
                .isDefault(a.isDefault())
                .build();
    }


}
