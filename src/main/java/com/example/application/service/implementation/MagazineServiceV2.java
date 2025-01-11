package com.example.application.service.implementation;

import com.example.application.entity.DTO.MagazineDto;
import com.example.application.entity.Magazine;

import java.util.List;
import java.util.Optional;

public interface MagazineServiceV2 {
    List<MagazineDto> findAll();
    Optional<MagazineDto> findById(Long id);
    List<MagazineDto> findByIsni(String isni);
    List<MagazineDto> findByYear(String year);
    List<MagazineDto> findByMonth(String month);
    List<MagazineDto> findByPublicationDateRange(java.util.Date startDate, java.util.Date endDate);
    MagazineDto save(MagazineDto magazine);
    void deleteById(Long id);
    Optional<MagazineDto> findByIsniAndMonthAndYear(String isni, String month, String year);

    int insertMagazine(MagazineDto magazine);
}
