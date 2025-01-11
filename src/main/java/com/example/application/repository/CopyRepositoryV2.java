package com.example.application.repository;

import com.example.application.entity.Copy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface CopyRepositoryV2 extends JpaRepository<Copy, Long> {
    // Méthodes personnalisées
    List<Copy> findByStatus(String status);
    List<Copy> findByItemId(Long itemId);
    List<Copy> findByPriceBetween(double minPrice, double maxPrice);

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO copies (status, item_id, price, acquisition_date) VALUES (:status, :itemId, :price, :acquisition_date)", nativeQuery = true)
    int insertCopy(@Param("status") String status,@Param("itemId") Long itemId,@Param("price") double price, @Param("acquisition_date") java.util.Date acquisitionDate);
}
