package com.example.application.repository;

import com.example.application.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface UserRepositoryV2 extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    @Query(value = "SELECT * FROM users WHERE username = :username ", nativeQuery = true)
    User findUserByUsernameAndPassword(@Param("username") String username);

    /**
     * Compte le nombre d'utilisateurs créés depuis une date spécifique
     * 
     * @param date Date à partir de laquelle compter les utilisateurs
     * @return Le nombre d'utilisateurs créés depuis la date spécifiée
     */
    @Query(value = "SELECT COUNT(*) FROM users WHERE created_at >= FROM_UNIXTIME(:date/1000)", nativeQuery = true)
    long countUsersCreatedSince(@Param("date") long date);

    /**
     * Trouve les utilisateurs actifs qui ont au moins un emprunt dans l'année
     * 
     * @param date Date à partir de laquelle compter les emprunts (généralement 1 an
     *             en arrière)
     * @return Le nombre d'utilisateurs distincts ayant effectué au moins un emprunt
     */
    @Query(value = "SELECT COUNT(DISTINCT u.id) FROM users u " +
            "JOIN loans l ON u.id = l.member_id " +
            "WHERE l.loan_date >= FROM_UNIXTIME(:date/1000)", nativeQuery = true)
    long countActiveUsersSince(@Param("date") long date);

    /**
     * Trouve l'utilisateur avec le plus d'emprunts dans une période donnée
     * 
     * @param date Date à partir de laquelle compter les emprunts (généralement 1 an
     *             en arrière)
     * @return L'utilisateur avec le plus d'emprunts et le nombre d'emprunts
     */
    @Query(value = "SELECT u.username, u.first_name, u.last_name, COUNT(l.id) AS loan_count FROM users u " +
            "JOIN loans l ON u.id = l.member_id " +
            "WHERE l.loan_date >= FROM_UNIXTIME(:date/1000) " +
            "GROUP BY u.id, u.username, u.first_name, u.last_name " +
            "ORDER BY loan_count DESC", nativeQuery = true)
    List<Object[]> findMostActiveUsersSince(@Param("date") long date);

    User findByEmail(String email);
}
