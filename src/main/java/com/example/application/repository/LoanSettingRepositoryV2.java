package com.example.application.repository;

import com.example.application.entity.LoanSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LoanSettingRepositoryV2 extends JpaRepository<LoanSetting,Long> {

}
