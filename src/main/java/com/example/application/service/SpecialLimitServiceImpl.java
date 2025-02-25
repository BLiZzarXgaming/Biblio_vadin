package com.example.application.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.application.entity.SpecialLimit;
import com.example.application.entity.User;
import com.example.application.repository.SpecialLimitRepository;
import com.example.application.service.implementation.SpecialLimitService;

@Service
public class SpecialLimitServiceImpl implements SpecialLimitService {

    private final SpecialLimitRepository specialLimitRepository;

    public SpecialLimitServiceImpl(SpecialLimitRepository specialLimitRepository) {
        this.specialLimitRepository = specialLimitRepository;
    }

    public Optional<SpecialLimit> findFirstByUserOrderByCreatedAtDesc(User user) {
        return specialLimitRepository.findFirstByUserOrderByCreatedAtDesc(user);
    }
}
