package lk.ijse.bookstore.repository;

import lk.ijse.bookstore.entity.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WishlistRepository extends JpaRepository<Wishlist, Long> {
    Optional<Wishlist> findByUser_UserId(Long userId);
}
