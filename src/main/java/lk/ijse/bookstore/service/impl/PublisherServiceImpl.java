package lk.ijse.bookstore.service.impl;

import lk.ijse.bookstore.dto.request.PublisherRequestDTO;
import lk.ijse.bookstore.dto.response.PublisherResponseDTO;
import lk.ijse.bookstore.entity.Publisher;
import lk.ijse.bookstore.exception.CustomerException;
import lk.ijse.bookstore.exception.ResponseCode;
import lk.ijse.bookstore.exception.ResponseMessage;
import lk.ijse.bookstore.repository.PublisherRepository;
import lk.ijse.bookstore.service.PublisherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PublisherServiceImpl implements PublisherService {
    private final PublisherRepository publisherRepository;

    @Override
    public PublisherResponseDTO save(PublisherRequestDTO dto) {
        Publisher publisher = Publisher.builder()
                .name(dto.getName())
                .contactEmail(dto.getContactEmail()).build();
        return toDto(publisherRepository.save(publisher));
    }


    @Override
    public PublisherResponseDTO update(Long id, PublisherRequestDTO dto) {
        Publisher publisher = find(id);
        publisher.setName(dto.getName());
        publisher.setContactEmail(dto.getContactEmail());
        return toDto(publisherRepository.save(publisher));
    }


    @Override
    public void delete(Long id) {
        publisherRepository.delete(find(id));
    }


    @Override
    public PublisherResponseDTO findById(Long id) {
        return toDto(find(id));
    }

    @Override
    public List<PublisherResponseDTO> findAll() {
        return publisherRepository.findAll()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    private Publisher find(Long id) {
        return publisherRepository.findById(id)
                .orElseThrow(() -> new CustomerException(ResponseCode.NOT_FOUND, ResponseMessage.PUBLISHER_NOT_FOUND));
    }

    private PublisherResponseDTO toDto(Publisher p) {
        return PublisherResponseDTO.builder()
                .publisherId(p.getPublisherId())
                .name(p.getName())
                .contactEmail(p.getContactEmail()).build();
    }

}
