package lk.ijse.bookstore.service.impl;

import lk.ijse.bookstore.dto.request.AuthorRequestDTO;
import lk.ijse.bookstore.dto.response.AuthorResponseDTO;
import lk.ijse.bookstore.entity.Author;
import lk.ijse.bookstore.exception.CustomerException;
import lk.ijse.bookstore.exception.ResponseCode;
import lk.ijse.bookstore.exception.ResponseMessage;
import lk.ijse.bookstore.repository.AuthorRepository;
import lk.ijse.bookstore.service.AuthorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthorServiceImpl  implements AuthorService {
    private final AuthorRepository authorRepository;


    @Override
    public AuthorResponseDTO save(AuthorRequestDTO dto) {
        Author author = Author.builder().name(dto.getName()).bio(dto.getBio()).build();
        return toDto(authorRepository.save(author));
    }


    @Override
    public AuthorResponseDTO update(Long id, AuthorRequestDTO dto) {
        Author author = find(id);
        author.setName(dto.getName());
        author.setBio(dto.getBio());
        return toDto(authorRepository.save(author));
    }

    @Override
    public void delete(Long id) {
        authorRepository.delete(find(id));
    }


    @Override
    public AuthorResponseDTO findById(Long id) {
        return toDto(find(id));
    }

    @Override
    public List<AuthorResponseDTO> findAll() {
        return authorRepository.findAll()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }


    private Author find(Long id) {
        return authorRepository.findById(id)
                .orElseThrow(() -> new CustomerException(ResponseCode.NOT_FOUND, ResponseMessage.AUTHOR_NOT_FOUND));
    }


    private AuthorResponseDTO toDto(Author a) {
        return AuthorResponseDTO.builder()
                .authorId(a.getAuthorId())
                .name(a.getName())
                .bio(a.getBio())
                .build();
    }

}
