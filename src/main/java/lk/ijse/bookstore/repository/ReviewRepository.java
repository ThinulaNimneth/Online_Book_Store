package lk.ijse.bookstore.repository;

import lk.ijse.bookstore.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByBook_BookIdOrderByCreatedAtDesc(Long bookId);
}
