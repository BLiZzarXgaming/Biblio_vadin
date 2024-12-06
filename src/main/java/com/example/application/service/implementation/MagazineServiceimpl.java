package com.example.application.service.implementation;

import com.example.application.entity.Magazine;
import com.example.application.repository.MagazineRepository;
import org.springframework.stereotype.Service;

@Service
public class MagazineServiceimpl {

    private MagazineRepository magazineRepository;

    public MagazineServiceimpl(MagazineRepository magazineRepository) {
        this.magazineRepository = magazineRepository;
    }

    public int save(Magazine magazine) {
        Magazine existingMagazine = magazineRepository.findByIsniAndMonthAndYear(magazine.getIsni(), magazine.getMonth(), magazine.getYear()).orElse(null);

        if (existingMagazine != null) {
            return 0;
        }

        magazineRepository.save(magazine);

        return 1;
    }

    public Magazine findByIsniAndMonthAndYear(String isni, String month, String year) {
        return magazineRepository.findByIsniAndMonthAndYear(isni, month, year).orElse(null);
    }

    public Magazine findByItemId(long itemId) {
        return magazineRepository.findByItemId(itemId).orElse(null);
    }
}
