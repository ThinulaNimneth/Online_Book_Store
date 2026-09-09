package lk.ijse.bookstore.service.impl;

import lk.ijse.bookstore.dto.request.ReviewRequestDTO;
import lk.ijse.bookstore.dto.response.ReviewResponseDTO;
import lk.ijse.bookstore.entity.Book;
import lk.ijse.bookstore.entity.Review;
import lk.ijse.bookstore.entity.User;
import lk.ijse.bookstore.exception.CustomerException;
import lk.ijse.bookstore.exception.ResponseCode;
import lk.ijse.bookstore.exception.ResponseMessage;
import lk.ijse.bookstore.repository.BookRepository;
import lk.ijse.bookstore.repository.ReviewRepository;
import lk.ijse.bookstore.repository.UserRepository;
import lk.ijse.bookstore.service.ReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewServiceImpl implements ReviewService {
    private final ReviewRepository reviewRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;


    @Override
    public ReviewResponseDTO save(String email, ReviewRequestDTO dto){

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomerException(ResponseCode.NOT_FOUND, ResponseMessage.USER_NOT_FOUND));
        Book book = bookRepository.findById(dto.getBookId())
                .orElseThrow(() -> new CustomerException(ResponseCode.NOT_FOUND, ResponseMessage.BOOK_NOT_FOUND));

        Review review = Review.builder()
                .user(user)
                .book(book)
                .rating(dto.getRating())
                .comment(dto.getComment())
                .build();


        review = reviewRepository.save(review);
        log.info("Review posted by {} for book {}", email, book.getBookId());
        return toDto(review);
    }




    @Override
    public List<ReviewResponseDTO> findByBookId(Long bookId) {
        return reviewRepository.findByBook_BookIdOrderByCreatedAtDesc(bookId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }


    private ReviewResponseDTO toDto(Review r) {
        return ReviewResponseDTO.builder()
                .reviewId(r.getReviewId())
                .name(r.getUser().getFullName())
                .rating(r.getRating())
                .comment(r.getComment())
                .createdAt(r.getCreatedAt())
                .build();
    }

}

