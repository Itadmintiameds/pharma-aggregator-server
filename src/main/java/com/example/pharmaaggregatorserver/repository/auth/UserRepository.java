package com.example.pharmaaggregatorserver.repository.auth;

import com.example.pharmaaggregatorserver.entity.auth.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {  // Changed from String to Long
    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.failedLoginAttempts = :attempts WHERE u.userId = :userId")
    void updateFailedLoginAttempts(@Param("userId") Long userId, @Param("attempts") int attempts);

    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.lastLoginAt = :lastLoginAt WHERE u.userId = :userId")
    void updateLastLogin(@Param("userId") Long userId, @Param("lastLoginAt") LocalDateTime lastLoginAt);

    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.isAccountLocked = :locked WHERE u.userId = :userId")
    void updateAccountLocked(@Param("userId") Long userId, @Param("locked") boolean locked);
}