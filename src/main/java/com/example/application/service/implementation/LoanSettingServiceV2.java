package com.example.application.service.implementation;

import com.example.application.entity.DTO.LoanSettingDto;
import com.example.application.entity.LoanSetting;

import java.util.List;
import java.util.Optional;

public interface LoanSettingServiceV2 {
    List<LoanSettingDto> findAll();
    Optional<LoanSettingDto> findById(Long id);
    LoanSettingDto save(LoanSettingDto loanSetting);
    void deleteById(Long id);
}
