package lk.ijse.bookstore.repository;

import lk.ijse.bookstore.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    @Query("select b from Book b where lower(b.title) like lower(concat('%', :keyword, '%')) " +
            "or lower(b.isbn) like lower(concat('%', :keyword, '%'))")
    List<Book> searchByKeyword(@Param("keyword") String keyword);

    @Query("select b from Book b join b.categories c where lower(c.name) = lower(:category)")
    List<Book> findByCategoryName(@Param("category") String category);

    List<Book>findTop10ByCreatedAtAfterOrderByCreatedAtDesc(LocalDateTime after);

    List<Book>findTop10ByOrderByCreatedAtDesc();
}
