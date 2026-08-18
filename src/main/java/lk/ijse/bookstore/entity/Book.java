package lk.ijse.bookstore.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "books")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long bookId;

    @Column(nullable = false,length = 300)
    private String title;

    @Column(unique = true, length = 50)
    private String isbn;

    @Column(length = 500)
    private String description;

    @Column(nullable = false,precision = 10 , scale = 2)
    private BigDecimal price;

    @Column(precision = 10 ,scale = 2)
    private BigDecimal specialPrice;

    @Column(length = 50)
    private String language;

    private Integer pages;

    @Column(updatable = false)
    private LocalDateTime createdAt;

}
