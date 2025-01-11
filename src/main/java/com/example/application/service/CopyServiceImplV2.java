package com.example.application.service;

import com.example.application.entity.DTO.CopyDto;
import com.example.application.entity.Mapper.CopyMapper;
import com.example.application.repository.CopyRepositoryV2;
import com.example.application.service.implementation.CopyServiceV2;
import com.example.application.utils.DateUtils;
import org.springframework.stereotype.Service;

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
        return copyRepository.findByPriceBetween(minPrice, maxPrice).stream().map(copyMapper::toDto).collect(Collectors.toList());
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
    public int insertCopy(CopyDto copy) {
        return copyRepository.insertCopy(copy.getStatus(), copy.getItem().getId(), copy.getPrice(), DateUtils.convertToDateViaInstant(copy.getAcquisitionDate()));
    }
}
