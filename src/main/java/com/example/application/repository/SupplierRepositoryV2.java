package com.example.application.repository;

import com.example.application.entity.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SupplierRepositoryV2 extends JpaRepository<Supplier, Long> {
    // Méthodes personnalisées
    Optional<Supplier> findByName(String name);
    Optional<Supplier> findByContactInfo(String contactInfo);
}
