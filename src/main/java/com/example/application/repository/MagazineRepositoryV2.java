package com.example.application.repository;

import com.example.application.entity.Magazine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface MagazineRepositoryV2 extends JpaRepository<Magazine, Long> {
    List<Magazine> findByIsni(String isni);
    List<Magazine> findByYear(String year);
    List<Magazine> findByMonth(String month);
    List<Magazine> findByPublicationDateBetween(java.util.Date startDate, java.util.Date endDate);
    Optional<Magazine> findByIsniAndMonthAndYear(String isni, String month, String year);

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO magazines (item_id, isni, year, month, publication_date) VALUES (:item_id, :isni, :year, :month, :publicationDate)", nativeQuery = true)
    int insertMagazine(@Param("item_id")Long item_id,@Param("isni") String isni,@Param("year") String year,@Param("month") String month,@Param("publicationDate") java.util.Date publicationDate);
}
