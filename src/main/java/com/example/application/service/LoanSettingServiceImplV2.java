package com.example.application.service;

import com.example.application.entity.DTO.LoanSettingDto;
import com.example.application.entity.Mapper.LoanSettingMapper;
import com.example.application.repository.LoanSettingRepositoryV2;
import com.example.application.service.implementation.LoanSettingServiceV2;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class LoanSettingServiceImplV2 implements LoanSettingServiceV2 {
    private final LoanSettingRepositoryV2 loanSettingRepository;
    private final LoanSettingMapper loanSettingMapper;

    public LoanSettingServiceImplV2(LoanSettingRepositoryV2 loanSettingRepository, LoanSettingMapper loanSettingMapper) {
        this.loanSettingRepository = loanSettingRepository;
        this.loanSettingMapper = loanSettingMapper;
    }

    @Override
    public List<LoanSettingDto> findAll() {
        return loanSettingRepository.findAll().stream().map(loanSettingMapper::toDto).collect(Collectors.toList());
    }

    @Override
    public Optional<LoanSettingDto> findById(Long id) {
        return loanSettingRepository.findById(id).map(loanSettingMapper::toDto);
    }

    @Override
    public LoanSettingDto save(LoanSettingDto loanSetting) {
        // Validation ou logique métier supplémentaire ici si nécessaire
        return loanSettingMapper.toDto(loanSettingRepository.save(loanSettingMapper.toEntity(loanSetting)));
    }

    @Override
    public void deleteById(Long id) {
        loanSettingRepository.deleteById(id);
    }
}
