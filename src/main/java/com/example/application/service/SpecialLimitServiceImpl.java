package com.example.application.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.application.entity.SpecialLimit;
import com.example.application.entity.User;
import com.example.application.entity.DTO.SpecialLimitDto;
import com.example.application.entity.Mapper.SpecialLimitMapper;
import com.example.application.repository.SpecialLimitRepository;
import com.example.application.service.implementation.SpecialLimitService;

@Service
public class SpecialLimitServiceImpl implements SpecialLimitService {

    private final SpecialLimitRepository specialLimitRepository;
    private final SpecialLimitMapper specialLimitMapper;

    public SpecialLimitServiceImpl(SpecialLimitRepository specialLimitRepository,
            SpecialLimitMapper specialLimitMapper) {
        this.specialLimitRepository = specialLimitRepository;
        this.specialLimitMapper = specialLimitMapper;
    }

    public Optional<SpecialLimit> findFirstByUserOrderByCreatedAtDesc(User user) {
        return specialLimitRepository.findFirstByUserOrderByCreatedAtDesc(user);
    }

    @Override
    public List<SpecialLimitDto> findAll() {
        return specialLimitRepository.findAll().stream()
                .map(specialLimitMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<SpecialLimitDto> findById(Long id) {
        return specialLimitRepository.findById(id)
                .map(specialLimitMapper::toDto);
    }

    @Override
    public Optional<SpecialLimitDto> findActiveByUser(User user) {
        return specialLimitRepository.findFirstByUserOrderByCreatedAtDesc(user)
                .filter(limit -> "active".equals(limit.getStatus()))
                .map(specialLimitMapper::toDto);
    }

    @Override
    public SpecialLimitDto save(SpecialLimitDto specialLimit) {
        SpecialLimit entity = specialLimitMapper.toEntity(specialLimit);
        return specialLimitMapper.toDto(specialLimitRepository.save(entity));
    }

    @Override
    public void deleteById(Long id) {
        specialLimitRepository.deleteById(id);
    }
}
