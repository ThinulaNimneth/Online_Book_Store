package lk.ijse.bookstore.repository;

import lk.ijse.bookstore.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    Optional<Inventory>findByBook_BookId(Long bookId);
}
