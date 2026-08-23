package lk.ijse.bookstore.repository;

import lk.ijse.bookstore.entity.BookImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookImageRepository extends JpaRepository<BookImage, Long> {
    List<BookImage> findByBook_BookId(Long bookId);
}
