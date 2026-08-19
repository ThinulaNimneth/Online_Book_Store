package lk.ijse.bookstore.repository;

import lk.ijse.bookstore.entity.Role;
import lk.ijse.bookstore.enumiration.RoleType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository  extends JpaRepository<Role, Long> {
    Optional<Role> findByRoleName(RoleType roleName);
}
