package com.example.application.service;

import com.example.application.entity.DTO.LoanDto;
import com.example.application.entity.Mapper.LoanMapper;
import com.example.application.repository.LoanRepositoryV2;
import com.example.application.service.implementation.LoanServiceV2;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class LoanServiceImplV2 implements LoanServiceV2 {
    private final LoanRepositoryV2 loanRepository;
    private final LoanMapper loanMapper;

    public LoanServiceImplV2(LoanRepositoryV2 loanRepository, LoanMapper loanMapper) {
        this.loanRepository = loanRepository;
        this.loanMapper = loanMapper;
    }

    @Override
    public List<LoanDto> findAll() {
        return loanRepository.findAll().stream().map(loanMapper::toDto).collect(Collectors.toList());
    }

    @Override
    public Optional<LoanDto> findById(Long id) {
        return loanRepository.findById(id).map(loanMapper::toDto);
    }

    @Override
    public List<LoanDto> findByMember(Long memberId) {
        return loanRepository.findByMemberId(memberId).stream().map(loanMapper::toDto).collect(Collectors.toList());
    }

    @Override
    public List<LoanDto> findByCopy(Long copyId) {
        return loanRepository.findByCopyId(copyId).stream().map(loanMapper::toDto).collect(Collectors.toList());
    }

    @Override
    public List<LoanDto> findByStatus(String status) {
        return loanRepository.findByStatus(status).stream().map(loanMapper::toDto).collect(Collectors.toList());
    }

    @Override
    public List<LoanDto> findByLoanDateRange(Date startDate, Date endDate) {
        return loanRepository.findByLoanDateBetween(startDate, endDate).stream().map(loanMapper::toDto).collect(Collectors.toList());
    }

    @Override
    public LoanDto save(LoanDto loan) {
        // Validation spécifique si nécessaire
        return loanMapper.toDto(loanRepository.save(loanMapper.toEntity(loan)));
    }

    @Override
    public void deleteById(Long id) {
        loanRepository.deleteById(id);
    }
}
