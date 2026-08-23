package lk.ijse.bookstore.repository;

import lk.ijse.bookstore.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    Optional<CartItem>findByCart_CartIdAndBook_BookId(Long cartId, Long bookId);
}
