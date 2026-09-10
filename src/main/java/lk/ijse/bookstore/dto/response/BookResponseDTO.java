package lk.ijse.bookstore.dto.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookResponseDTO {
    private Long id;

    private Long wishlistItemId;
    private String title;
    private String isbn;
    private String description;
    private BigDecimal price;
    private BigDecimal specialPrice;
    private String language;
    private Integer pages;

    private String author;
    private String category;
    private String publisher;
    private List<String> authors;
    private List<String> categories;
    private List<String> images;


    private Long publisherId;
    private List<Long> categoryIds;
    private List<Long> authorIds;

    private double rating;
    private int reviewCount;
    private boolean inStock;
    private int quantityAvailable;
    private boolean isNew;


}