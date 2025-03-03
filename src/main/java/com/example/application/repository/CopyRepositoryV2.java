package com.example.application.repository;

import com.example.application.entity.Copy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.time.LocalDate;

@Repository
public interface CopyRepositoryV2 extends JpaRepository<Copy, Long> {
        // Méthodes personnalisées
        List<Copy> findByStatus(String status);

        List<Copy> findByItemId(Long itemId);

        List<Copy> findByPriceBetween(double minPrice, double maxPrice);

        @Modifying
        @Transactional
        @Query(value = "INSERT INTO copies (status, item_id, price, acquisition_date) VALUES (:status, :itemId, :price, :acquisition_date)", nativeQuery = true)
        int insertCopy(@Param("status") String status, @Param("itemId") Long itemId, @Param("price") double price,
                        @Param("acquisition_date") java.util.Date acquisitionDate);

        /**
         * Calcule la somme des prix de toutes les copies non supprimées
         * 
         * @return La somme des prix de toutes les copies actives
         */
        @Query(value = "SELECT SUM(price) FROM copies WHERE status <> 'deleted'", nativeQuery = true)
        double sumPriceOfActiveCopies();

        /**
         * Compte le nombre de copies actives
         * 
         * @return Le nombre de copies non supprimées
         */
        @Query(value = "SELECT COUNT(*) FROM copies WHERE status <> 'deleted'", nativeQuery = true)
        long countActiveCopies();

        /**
         * Calcule la valeur totale de l'inventaire basée sur les copies
         * Multiplie la valeur de chaque item par le nombre de ses copies actives
         * 
         * @return La valeur totale de l'inventaire
         */
        @Query(value = "SELECT SUM(i.value * (SELECT COUNT(*) FROM copies c WHERE c.item_id = i.id AND c.status <> 'deleted')) "
                        +
                        "FROM items i", nativeQuery = true)
        double calculateTotalInventoryValue();

        @Query(value = "SELECT SUM(i.value) FROM copies c inner join loans l on c.id = l.copy_id JOIN items i ON i.id = c.item_id WHERE l.loan_date BETWEEN :startDate AND :endDate", nativeQuery = true)
        double calculateTotalBorrowedValue(@Param("startDate") Date startDate, @Param("endDate") Date endDate);

        Page<Copy> findAll(Pageable pageable);

        @Query("SELECT c FROM Copy c WHERE CAST(c.id AS string) LIKE %:searchTerm% OR LOWER(c.item.title) LIKE %:searchTerm%")
        Page<Copy> findByIdContainingOrTitleContaining(@Param("searchTerm") String searchTerm, Pageable pageable);

        @Query("SELECT c FROM Copy c WHERE c.status = :status")
        Page<Copy> findByStatus(@Param("status") String status, Pageable pageable);

        @Query("SELECT c FROM Copy c WHERE (CAST(c.id AS string) LIKE %:searchTerm% OR LOWER(c.item.title) LIKE %:searchTerm%) AND c.status = :status")
        Page<Copy> findByIdContainingOrTitleContainingAndStatus(@Param("searchTerm") String searchTerm,
                        @Param("status") String status, Pageable pageable);

        @Query("SELECT c FROM Copy c WHERE c.acquisitionDate = :today")
        Page<Copy> findByAcquisitionDate(@Param("today") LocalDate today, Pageable pageable);

        @Query("SELECT c FROM Copy c WHERE (CAST(c.id AS string) LIKE %:searchTerm% OR LOWER(c.item.title) LIKE %:searchTerm%) AND c.acquisitionDate = :today")
        Page<Copy> findByIdContainingOrTitleContainingAndAcquisitionDate(@Param("searchTerm") String searchTerm,
                        @Param("today") LocalDate today, Pageable pageable);

        @Query("SELECT c FROM Copy c WHERE c.status = :status AND c.acquisitionDate = :today")
        Page<Copy> findByStatusAndAcquisitionDate(@Param("status") String status, @Param("today") LocalDate today,
                        Pageable pageable);

        @Query("SELECT c FROM Copy c WHERE (CAST(c.id AS string) LIKE %:searchTerm% OR LOWER(c.item.title) LIKE %:searchTerm%) AND c.status = :status AND c.acquisitionDate = :today")
        Page<Copy> findByIdContainingOrTitleContainingAndStatusAndAcquisitionDate(
                        @Param("searchTerm") String searchTerm, @Param("status") String status,
                        @Param("today") LocalDate today, Pageable pageable);
}
