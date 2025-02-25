package com.example.application.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.application.entity.SpecialLimit;
import com.example.application.entity.User;

public interface SpecialLimitRepository extends JpaRepository<SpecialLimit, Long> {

    Optional<SpecialLimit> findFirstByUserOrderByCreatedAtDesc(User user);

}
