package com.example.application.service;

import com.example.application.entity.DTO.MagazineDto;
import com.example.application.entity.Magazine;
import com.example.application.entity.Mapper.MagazineMapper;
import com.example.application.repository.MagazineRepositoryV2;
import com.example.application.service.implementation.MagazineServiceV2;
import com.example.application.utils.DateUtils;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MagazineServiceImplV2 implements MagazineServiceV2 {
    private final MagazineRepositoryV2 magazineRepository;
    private final MagazineMapper magazineMapper;

    public MagazineServiceImplV2(MagazineRepositoryV2 magazineRepository, MagazineMapper magazineMapper) {
        this.magazineRepository = magazineRepository;
        this.magazineMapper = magazineMapper;
    }

    @Override
    public List<MagazineDto> findAll() {
        return magazineRepository.findAll().stream().map(magazineMapper::toDto).collect(Collectors.toList());
    }

    @Override
    public Optional<MagazineDto> findById(Long id) {
        return magazineRepository.findById(id).map(magazineMapper::toDto);
    }

    @Override
    public List<MagazineDto> findByIsni(String isni) {
        return magazineRepository.findByIsni(isni).stream().map(magazineMapper::toDto).collect(Collectors.toList());
    }

    @Override
    public List<MagazineDto> findByYear(String year) {
        return magazineRepository.findByYear(year).stream().map(magazineMapper::toDto).collect(Collectors.toList());
    }

    @Override
    public List<MagazineDto> findByMonth(String month) {
        return magazineRepository.findByMonth(month).stream().map(magazineMapper::toDto).collect(Collectors.toList());
    }

    @Override
    public List<MagazineDto> findByPublicationDateRange(Date startDate, Date endDate) {
        return magazineRepository.findByPublicationDateBetween(startDate, endDate).stream().map(magazineMapper::toDto).collect(Collectors.toList());
    }

    @Override
    public MagazineDto save(MagazineDto magazine) {
        Optional<Magazine> existingMagazine = magazineRepository.findByIsniAndMonthAndYear(
                magazine.getIsni(),
                magazine.getMonth(),
                magazine.getYear()
        );
        if (existingMagazine.isPresent()) {
            throw new IllegalArgumentException("A magazine with the same ISNI, month, and year already exists.");
        }
        return magazineMapper.toDto(magazineRepository.save(magazineMapper.toEntity(magazine)));
    }

    @Override
    public void deleteById(Long id) {
        magazineRepository.deleteById(id);
    }

    @Override
    public Optional<MagazineDto> findByIsniAndMonthAndYear(String isni, String month, String year) {
        return magazineRepository.findByIsniAndMonthAndYear(isni, month, year).map(magazineMapper::toDto);
    }

    @Override
    public int insertMagazine(MagazineDto magazine) {
        Optional<Magazine> existingMagazine = magazineRepository.findByIsniAndMonthAndYear(
                magazine.getIsni(),
                magazine.getMonth(),
                magazine.getYear()
        );
        if (existingMagazine.isPresent()) {
            throw new IllegalArgumentException("A magazine with the same ISNI, month, and year already exists.");
        }
        return magazineRepository.insertMagazine(magazine.getItem().getId(), magazine.getIsni(), magazine.getYear(), magazine.getMonth(), DateUtils.convertToDateViaInstant(magazine.getPublicationDate()));
    }
}
