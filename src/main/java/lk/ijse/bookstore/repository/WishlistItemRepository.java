package lk.ijse.bookstore.repository;

import lk.ijse.bookstore.entity.WishlistItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WishlistItemRepository  extends JpaRepository<WishlistItem, Long> {
    Optional<WishlistItem> findByWishlist_WishlistIdAndBook_BookId(Long wishlistId, Long bookId);
}
