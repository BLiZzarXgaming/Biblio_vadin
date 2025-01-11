package com.example.application.repository;

import com.example.application.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepositoryV2 extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    @Query(value ="SELECT * FROM users WHERE username = :username ", nativeQuery = true)
    User findUserByUsernameAndPassword(@Param("username") String username);
}
