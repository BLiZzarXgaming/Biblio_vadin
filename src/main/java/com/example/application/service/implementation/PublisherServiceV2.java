package com.example.application.service.implementation;

import com.example.application.entity.DTO.PublisherDto;
import com.example.application.entity.Publisher;

import java.util.List;
import java.util.Optional;

public interface PublisherServiceV2 {
    List<PublisherDto> findAll();
    Optional<PublisherDto> findById(Long id);
    Optional<PublisherDto> findByName(String name);
    Optional<PublisherDto> findByContactInfo(String contactInfo);
    PublisherDto save(PublisherDto publisher);
    void deleteById(Long id);
}
