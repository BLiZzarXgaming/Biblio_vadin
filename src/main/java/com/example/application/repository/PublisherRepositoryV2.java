package com.example.application.repository;

import com.example.application.entity.Publisher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PublisherRepositoryV2 extends JpaRepository<Publisher, Long> {
    // Méthodes personnalisées
    Optional<Publisher> findByName(String name);
    Optional<Publisher> findByContactInfo(String contactInfo);
}
