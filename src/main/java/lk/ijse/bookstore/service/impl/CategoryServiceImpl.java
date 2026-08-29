package lk.ijse.bookstore.service.impl;

import lk.ijse.bookstore.dto.request.CategoryRequestDTO;
import lk.ijse.bookstore.dto.response.CategoryResponseDTO;
import lk.ijse.bookstore.entity.Category;
import lk.ijse.bookstore.exception.CustomerException;
import lk.ijse.bookstore.exception.ResponseCode;
import lk.ijse.bookstore.exception.ResponseMessage;
import lk.ijse.bookstore.repository.CategoryRepository;
import lk.ijse.bookstore.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static java.nio.file.Files.find;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryServiceImpl implements CategoryService {

    private  final  CategoryRepository categoryRepository;

    @Override
    public CategoryResponseDTO save(CategoryRequestDTO dto){
        Category category = Category.builder().name(dto.getName()).description(dto.getDescription()).build();
        category = categoryRepository.save(category);
        log.info("Category created: {}", category.getName());
        return toDto(category);
    }

    @Override
    public CategoryResponseDTO update(Long id, CategoryRequestDTO dto){
        Category category = find(id);
        category.setName(dto.getName());
        category.setDescription(dto.getDescription());
        return toDto(categoryRepository.save(category));
    }

    @Override
    public void delete(Long id){
        Category category = find(id);
        categoryRepository.delete(category);
    }

    @Override
    public CategoryResponseDTO findById(Long id) {
        return toDto(find(id));
    }

    @Override
    public List<CategoryResponseDTO> findAll() {
        return categoryRepository.findAll()
                .stream().map(this::toDto)
                .collect(Collectors.toList());
    }


    private Category find(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new CustomerException(ResponseCode.NOT_FOUND, ResponseMessage.CATEGORY_NOT_FOUND));
    }


    private CategoryResponseDTO toDto(Category c) {
        return CategoryResponseDTO.builder()
                .categoryId(c.getCategoryId())
                .name(c.getName())
                .description(c.getDescription()).build();
    }

}
