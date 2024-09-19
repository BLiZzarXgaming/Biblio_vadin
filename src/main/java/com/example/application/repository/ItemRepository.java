package com.example.application.repository;

import com.example.application.entity.Item;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {

    @Query("SELECT Item FROM Item i WHERE "
            + "(:keyword IS NULL OR LOWER(i.title) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND "
            + "(:category IS NULL OR i.category = :category) AND "
            + "(:publisher IS NULL OR i.publisher = :publisher)")
    List<Item> findByCriteriaWithPagination(@Param("keyword") String keyword,
                                            @Param("category") Long category,
                                            @Param("publisher") Long publisher,
                                            Pageable pageable);

    @Query("SELECT COUNT(i) FROM Item i WHERE "
            + "(:keyword IS NULL OR LOWER(i.title) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND "
            + "(:category IS NULL OR i.category = :category) AND "
            + "(:publisher IS NULL OR i.publisher = :publisher)")
    int countByCriteria(@Param("keyword") String keyword,
                        @Param("category") Long category,
                        @Param("publisher") Long publisher);

    // Méthode pour trouver un Item par son ID
    @Override
    @Query(value = "SELECT * FROM items WHERE id = :id", nativeQuery = true)
    Optional<Item> findById(@Param("id") Long id);
}
