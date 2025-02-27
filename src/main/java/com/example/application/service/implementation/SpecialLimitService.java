package com.example.application.service.implementation;

import java.util.List;
import java.util.Optional;

import com.example.application.entity.SpecialLimit;
import com.example.application.entity.User;
import com.example.application.entity.DTO.SpecialLimitDto;

public interface SpecialLimitService {

    Optional<SpecialLimit> findFirstByUserOrderByCreatedAtDesc(User user);

    List<SpecialLimitDto> findAll();

    Optional<SpecialLimitDto> findById(Long id);

    Optional<SpecialLimitDto> findActiveByUser(User user);

    SpecialLimitDto save(SpecialLimitDto specialLimit);

    void deleteById(Long id);
}
