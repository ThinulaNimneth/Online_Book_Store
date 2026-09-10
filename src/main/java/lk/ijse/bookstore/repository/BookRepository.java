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

//    Keyword search
    @Query("select distinct b from Book b " +
            "left join b.authors a " +
            "left join b.categories c " +
            "where lower(b.title) like lower(concat('%', :keyword, '%')) " +
            "or lower(b.isbn) like lower(concat('%', :keyword, '%')) " +
            "or lower(b.description) like lower(concat('%', :keyword, '%')) " +
            "or lower(a.name) like lower(concat('%', :keyword, '%')) " +
            "or lower(c.name) like lower(concat('%', :keyword, '%'))")
    List<Book> searchByKeyword(@Param("keyword") String keyword);


    //category dropdown
    @Query("select distinct b from Book b join b.categories c where lower(trim(c.name)) = lower(trim(:category))")
    List<Book> findByCategoryName(@Param("category") String category);


    //character name
    @Query("select distinct b from Book b join b.categories c where lower(c.name) like lower(concat('%', :category, '%'))")
    List<Book> findByCategoryNameContaining(@Param("category") String category);



    @Query("select distinct b from Book b " +
            "left join b.authors a " +
            "join b.categories c " +
            "where (lower(b.title) like lower(concat('%', :keyword, '%')) " +
            "   or lower(b.isbn) like lower(concat('%', :keyword, '%')) " +
            "   or lower(b.description) like lower(concat('%', :keyword, '%')) " +
            "   or lower(a.name) like lower(concat('%', :keyword, '%'))) " +
            "and lower(trim(c.name)) = lower(trim(:category))")
    List<Book> searchByKeywordAndCategory(@Param("keyword") String keyword, @Param("category") String category);

    List<Book>findTop10ByCreatedAtAfterOrderByCreatedAtDesc(LocalDateTime after);

    List<Book>findTop10ByOrderByCreatedAtDesc();
}
