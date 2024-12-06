package com.example.application.service.implementation;

import com.example.application.entity.Publisher;
import com.example.application.repository.PublisherRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PublisherServiceImpl {

    private PublisherRepository publisherRepository;

    public PublisherServiceImpl(PublisherRepository publisherRepository) {
        this.publisherRepository = publisherRepository;
    }

    public List<Publisher> findAll() {
        return publisherRepository.findAll();
    }

    public Publisher findFirstByName(String name) {
        return publisherRepository.findFirstByName(name);
    }

    public int save(Publisher publisher) {

        Publisher existingPublisher = publisherRepository.findFirstByName(publisher.getName());

        if (existingPublisher != null) {
            return 0;
        }

        publisherRepository.save(publisher);

        return 1;

    }

    public Publisher findById(Long id) {
        return publisherRepository.findById(id).orElse(null);
    }
}
