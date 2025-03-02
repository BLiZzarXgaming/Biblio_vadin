package com.example.application.repository;

import com.example.application.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
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
    @Query("SELECT COUNT(u) FROM User u WHERE u.createdAt >= :date")
    long countUsersCreatedSince(@Param("date") Date date);

    /**
     * Trouve les utilisateurs actifs qui ont au moins un emprunt dans l'année
     * 
     * @param date Date à partir de laquelle compter les emprunts (généralement 1 an
     *             en arrière)
     * @return Le nombre d'utilisateurs distincts ayant effectué au moins un emprunt
     */
    @Query("SELECT COUNT(DISTINCT u.id) FROM User u JOIN Loan l ON u.id = l.member.id WHERE l.loanDate >= :date")
    long countActiveUsersSince(@Param("date") Date date);

    /**
     * Trouve l'utilisateur avec le plus d'emprunts dans une période donnée
     * 
     * @param date Date à partir de laquelle compter les emprunts (généralement 1 an
     *             en arrière)
     * @return L'utilisateur avec le plus d'emprunts et le nombre d'emprunts
     */
    @Query("SELECT u.username, u.firstName, u.lastName, COUNT(l.id) AS loanCount FROM User u " +
            "JOIN Loan l ON u.id = l.member.id " +
            "WHERE l.loanDate >= :date " +
            "GROUP BY u.id, u.username, u.firstName, u.lastName " +
            "ORDER BY loanCount DESC")
    List<Object[]> findMostActiveUsersSince(@Param("date") Date date);

    User findByEmail(String email);
}
