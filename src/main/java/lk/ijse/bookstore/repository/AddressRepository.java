package lk.ijse.bookstore.repository;

import lk.ijse.bookstore.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AddressRepository  extends JpaRepository<Address, Long> {
    List<Address> findByUser_UserId(Long userId);
}
