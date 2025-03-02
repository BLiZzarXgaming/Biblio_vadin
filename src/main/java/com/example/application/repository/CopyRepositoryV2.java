package com.example.application.repository;

import com.example.application.entity.Copy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
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
}
