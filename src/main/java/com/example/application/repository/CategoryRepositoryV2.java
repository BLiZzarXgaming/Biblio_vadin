package com.example.application.repository;

import com.example.application.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CategoryRepositoryV2 extends JpaRepository<Category, Long> {
    Optional<Category> findByName(String name);
}
