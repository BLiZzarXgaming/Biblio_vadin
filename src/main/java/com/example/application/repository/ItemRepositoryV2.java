package com.example.application.repository;

import com.example.application.entity.Item;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Repository
public interface ItemRepositoryV2 extends JpaRepository<Item, Long> {
        List<Item> findByType(String type);

        List<Item> findByTitleContaining(String keyword);

        List<Item> findByCategoryId(Long categoryId);

        @Query(value = "SELECT * FROM items WHERE "
                        + "(:title IS NULL OR LOWER(title) LIKE LOWER(CONCAT('%', :title, '%'))) AND "
                        + "(:category IS NULL OR category = :category) AND "
                        + "(:publisher IS NULL OR publisher_id = :publisher) ORDER BY title", nativeQuery = true)
        List<Item> findByCriteriaWithPagination(@Param("title") String title,
                        @Param("category") Long category,
                        @Param("publisher") Long publisher,
                        Pageable pageable);

        @Query(value = "SELECT i.* FROM items i " +
                        "    inner join board_games g on i.id = g.item_id " +
                        "WHERE " +
                        "    (:numberOfPieces IS NULL OR g.number_of_pieces = :numberOfPieces) AND " +
                        "    (:recommendedAge IS NULL OR g.recommended_age = :recommendedAge) AND " +
                        "    (:title IS NULL OR LOWER(i.title) LIKE LOWER(CONCAT('%', :title, '%'))) AND " +
                        "    (:category IS NULL OR i.category = :category) AND " +
                        "    (:publisher IS NULL OR i.publisher_id = :publisher) AND" +
                        "    (:gtin IS NULL OR g.gtin = :gtin OR :gtin = '') ", nativeQuery = true)
        List<Item> findBoardGameByCriteriaWithPagination(@Param("title") String title,
                        @Param("numberOfPieces") Integer numberOfPieces,
                        @Param("recommendedAge") Integer recommendedAge,
                        @Param("category") Long category,
                        @Param("publisher") Long publisher,
                        @Param("gtin") String gtin,
                        Pageable pageable);

        @Query(value = "SELECT i.* FROM items i " +
                        "    inner join magazines m on i.id = m.item_id " +
                        "WHERE " +
                        "    (:title IS NULL OR LOWER(i.title) LIKE LOWER(CONCAT('%', :title, '%'))) AND " +
                        "    (:isni IS NULL OR m.isni = :isni OR :isni = '') AND " +
                        "    (:month IS NULL OR m.month = :month OR :month = '') AND " +
                        "    (:publicationDate IS NULL OR m.publication_date = :publicationDate) AND " +
                        "    (:category IS NULL OR i.category = :category) AND " +
                        "    (:publisher IS NULL OR i.publisher_id = :publisher) ", nativeQuery = true)
        List<Item> findMagazineByCriteriaWithPagination(@Param("title") String title,
                        @Param("isni") String isni,
                        @Param("month") String month,
                        @Param("publicationDate") LocalDate publicationDate,
                        @Param("category") Long category,
                        @Param("publisher") Long publisher,
                        Pageable pageable);

        @Query(value = "SELECT i.* FROM items i " +
                        "    inner join books b on i.id = b.item_id " +
                        "WHERE  " +
                        "    (:title IS NULL OR LOWER(i.title) LIKE LOWER(CONCAT('%', :title, '%'))) AND " +
                        "    (:isbn IS NULL OR b.isbn = :isbn OR :isbn = '') AND " +
                        "    (:author IS NULL OR LOWER(b.author) LIKE LOWER(CONCAT('%', :author, '%'))) AND " +
                        "    (:publicationDate IS NULL OR b.publication_date = :publicationDate) AND " +
                        "    (:category IS NULL OR i.category = :category) AND " +
                        "    (:publisher IS NULL OR i.publisher_id = :publisher) ", nativeQuery = true)
        List<Item> findBookByCriteriaWithPagination(@Param("title") String title,
                        @Param("author") String author,
                        @Param("isbn") String isbn,
                        @Param("publicationDate") LocalDate publicationDate,
                        @Param("category") Long category,
                        @Param("publisher") Long publisher,
                        Pageable pageable);

        /**
         * Compte le nombre d'items créés depuis une date spécifique
         * 
         * @param date Date à partir de laquelle compter les items
         * @return Le nombre d'items créés depuis la date spécifiée
         */
        @Query("SELECT COUNT(i) FROM Item i WHERE i.createdAt >= :date")
        long countItemsCreatedSince(@Param("date") Date date);

        /**
         * Calcule la valeur totale de l'inventaire (somme de la valeur de tous les
         * items)
         * 
         * @return La somme des valeurs de tous les items
         */
        @Query("SELECT SUM(i.value) FROM Item i")
        double sumTotalItemsValue();

        /**
         * Trouve la catégorie la plus populaire basée sur le nombre d'emprunts
         * 
         * @param startDate Date à partir de laquelle compter les emprunts (généralement 1 an
         *             en arrière)
         * @return La catégorie la plus populaire et son nombre d'emprunts
         */
        @Query(value = "SELECT c.name, COUNT(l.id) AS loan_count " +
                        "FROM categories c " +
                        "JOIN items i ON c.id = i.category " +
                        "JOIN copies cp ON i.id = cp.item_id " +
                        "JOIN loans l ON cp.id = l.copy_id " +
                        "WHERE l.loan_date BETWEEN :startDate AND :endDate " +
                        "GROUP BY c.id, c.name " +
                        "ORDER BY loan_count DESC " +
                        "LIMIT 1", nativeQuery = true)
        Object[] findMostPopularCategorySince(@Param("startDate") Date startDate, @Param("endDate") Date endDate);

        /**
         * Trouve le type de document le plus emprunté
         * 
         * @param startDate Date à partir de laquelle compter les emprunts (généralement 1 an
         *             en arrière)
         * @return Le type le plus emprunté et son nombre d'emprunts
         */
        @Query(value = "SELECT i.type, COUNT(l.id) AS loan_count " +
                        "FROM items i " +
                        "JOIN copies cp ON i.id = cp.item_id " +
                        "JOIN loans l ON cp.id = l.copy_id " +
                        "WHERE l.loan_date BETWEEN :startDate AND :endDate " +
                        "GROUP BY i.type " +
                        "ORDER BY loan_count DESC " +
                        "LIMIT 1", nativeQuery = true)
        Object[] findMostBorrowedTypeSince(@Param("startDate") Date startDate,@Param("endDate") Date endDate);

        /**
         * Trouve le document le plus populaire basé sur le nombre d'emprunts
         * 
         * @param startDate Date à partir de laquelle compter les emprunts (généralement 1 an
         *             en arrière)
         * @return Le document le plus populaire et son nombre d'emprunts
         */
        @Query(value = "SELECT i.id, i.title, i.type, COUNT(l.id) AS loan_count " +
                        "FROM items i " +
                        "JOIN copies cp ON i.id = cp.item_id " +
                        "JOIN loans l ON cp.id = l.copy_id " +
                        "WHERE l.loan_date BETWEEN :startDate AND :endDate " +
                        "GROUP BY i.id, i.title, i.type " +
                        "ORDER BY loan_count DESC " +
                        "LIMIT 1", nativeQuery = true)
        Object[] findMostPopularItemSince(@Param("startDate") Date startDate, @Param("endDate") Date endDate);

        @Query(value = "SELECT COUNT(DISTINCT i.id) FROM items i INNER JOIN copies c ON c.item_id = i.id WHERE c.status <> 'deleted'", nativeQuery = true)
        int countItemByCopies();

        @Query(value = "SELECT DISTINCT i.* FROM items i INNER JOIN copies c ON c.item_id = i.id WHERE c.status <> 'deleted'", nativeQuery = true)
        List<Item> findAllItemByType();
}
