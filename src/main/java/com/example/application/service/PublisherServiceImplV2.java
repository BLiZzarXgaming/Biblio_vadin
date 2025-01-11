package com.example.application.service;

import com.example.application.entity.DTO.PublisherDto;
import com.example.application.entity.Mapper.PublisherMapper;
import com.example.application.entity.Publisher;
import com.example.application.repository.PublisherRepositoryV2;
import com.example.application.service.implementation.PublisherServiceV2;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PublisherServiceImplV2 implements PublisherServiceV2 {
    private final PublisherRepositoryV2 publisherRepository;
    private final PublisherMapper publisherMapper;

    public PublisherServiceImplV2(PublisherRepositoryV2 publisherRepository, PublisherMapper publisherMapper) {
        this.publisherRepository = publisherRepository;
        this.publisherMapper = publisherMapper;
    }

    @Override
    public List<PublisherDto> findAll() {
        return publisherRepository.findAll().stream().map(publisherMapper::toDto).collect(Collectors.toList());
    }

    @Override
    public Optional<PublisherDto> findById(Long id) {
        return publisherRepository.findById(id).map(publisherMapper::toDto);
    }

    @Override
    public Optional<PublisherDto> findByName(String name) {
        return publisherRepository.findByName(name).map(publisherMapper::toDto);
    }

    @Override
    public Optional<PublisherDto> findByContactInfo(String contactInfo) {
        return publisherRepository.findByContactInfo(contactInfo).map(publisherMapper::toDto);
    }

    @Override
    public PublisherDto save(PublisherDto publisher) {
        // Vérification d'unicité pour le nom ou les informations de contact
        Optional<Publisher> existingPublisherByName = publisherRepository.findByName(publisher.getName());
        if (existingPublisherByName.isPresent()) {
            throw new IllegalArgumentException("A publisher with the same name already exists.");
        }

        return publisherMapper.toDto(publisherRepository.save(publisherMapper.toEntity(publisher)));
    }

    @Override
    public void deleteById(Long id) {
        publisherRepository.deleteById(id);
    }
}
