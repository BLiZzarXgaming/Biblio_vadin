package com.example.application.service;

import com.example.application.entity.Copy;
import com.example.application.entity.DTO.CopyDto;
import com.example.application.entity.Mapper.CopyMapper;
import com.example.application.repository.CopyRepositoryV2;
import com.example.application.service.implementation.CopyServiceV2;
import com.example.application.utils.DateUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CopyServiceImplV2 implements CopyServiceV2 {
    private final CopyRepositoryV2 copyRepository;
    private final CopyMapper copyMapper;

    public CopyServiceImplV2(CopyRepositoryV2 copyRepository, CopyMapper copyMapper) {
        this.copyRepository = copyRepository;
        this.copyMapper = copyMapper;
    }

    @Override
    public List<CopyDto> findAll() {
        return copyRepository.findAll().stream().map(copyMapper::toDto).collect(Collectors.toList());
    }

    @Override
    public Optional<CopyDto> findById(Long id) {
        return copyRepository.findById(id).map(copyMapper::toDto);
    }

    @Override
    public List<CopyDto> findByStatus(String status) {
        return copyRepository.findByStatus(status).stream().map(copyMapper::toDto).collect(Collectors.toList());
    }

    @Override
    public List<CopyDto> findByItem(Long itemId) {
        return copyRepository.findByItemId(itemId).stream().map(copyMapper::toDto).collect(Collectors.toList());
    }

    @Override
    public List<CopyDto> findByPriceRange(double minPrice, double maxPrice) {
        return copyRepository.findByPriceBetween(minPrice, maxPrice).stream().map(copyMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public CopyDto save(CopyDto copy) {
        // Validation supplémentaire si nécessaire
        return copyMapper.toDto(copyRepository.save(copyMapper.toEntity(copy)));
    }

    @Override
    public void deleteById(Long id) {
        copyRepository.deleteById(id);
    }

    @Override
    public Page<CopyDto> findAllPaginated(Pageable pageable) {
        return copyRepository.findAll(pageable).map(copyMapper::toDto);
    }

    @Override
    public Page<CopyDto> findBySearchTerm(String searchTerm, Pageable pageable) {
        return copyRepository.findByIdContainingOrTitleContaining(searchTerm.toLowerCase(), pageable)
                .map(copyMapper::toDto);
    }

    @Override
    public Page<CopyDto> findByStatusPaginated(String status, Pageable pageable) {
        return copyRepository.findByStatus(status, pageable).map(copyMapper::toDto);
    }

    @Override
    public Page<CopyDto> findBySearchTermAndStatus(String searchTerm, String status, Pageable pageable) {
        return copyRepository.findByIdContainingOrTitleContainingAndStatus(searchTerm.toLowerCase(), status, pageable)
                .map(copyMapper::toDto);
    }

    @Override
    public Page<CopyDto> findByAcquisitionDate(LocalDate date, Pageable pageable) {
        return copyRepository.findByAcquisitionDate(date, pageable).map(copyMapper::toDto);
    }

    @Override
    public Page<CopyDto> findBySearchTermAndAcquisitionDate(String searchTerm, LocalDate date, Pageable pageable) {
        return copyRepository
                .findByIdContainingOrTitleContainingAndAcquisitionDate(searchTerm.toLowerCase(), date, pageable)
                .map(copyMapper::toDto);
    }

    @Override
    public Page<CopyDto> findByStatusAndAcquisitionDate(String status, LocalDate date, Pageable pageable) {
        return copyRepository.findByStatusAndAcquisitionDate(status, date, pageable).map(copyMapper::toDto);
    }

    @Override
    public Page<CopyDto> findBySearchTermAndStatusAndAcquisitionDate(String searchTerm, String status, LocalDate date,
            Pageable pageable) {
        return copyRepository.findByIdContainingOrTitleContainingAndStatusAndAcquisitionDate(searchTerm.toLowerCase(),
                status, date, pageable).map(copyMapper::toDto);
    }

    @Override
    public long count() {
        return copyRepository.count();
    }

    @Override
    public int insertCopy(CopyDto copy) {
        return copyRepository.insertCopy(copy.getStatus(), copy.getItem().getId(), copy.getPrice(),
                DateUtils.convertToDateViaInstant(copy.getAcquisitionDate()));
    }
}
