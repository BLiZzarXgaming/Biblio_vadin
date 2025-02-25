package com.example.application.service.implementation;

import java.util.Optional;

import com.example.application.entity.SpecialLimit;
import com.example.application.entity.User;

public interface SpecialLimitService {

    Optional<SpecialLimit> findFirstByUserOrderByCreatedAtDesc(User user);
}
