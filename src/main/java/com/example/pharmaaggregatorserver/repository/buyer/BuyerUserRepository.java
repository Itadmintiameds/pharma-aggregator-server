package com.example.pharmaaggregatorserver.repository.buyer;

import com.example.pharmaaggregatorserver.entity.buyer.BuyerUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface BuyerUserRepository extends JpaRepository<BuyerUser, Long> {
    Optional<BuyerUser> findByEmail(String email);

    boolean existsByEmail(String email);

    @Modifying
    @Transactional
    @Query("UPDATE BuyerUser b SET b.failedLoginAttempts = :attempts WHERE b.buyerUserId = :buyerUserId")
    void updateFailedLoginAttempts(@Param("buyerUserId") Long buyerUserId, @Param("attempts") int attempts);

    @Modifying
    @Transactional
    @Query("UPDATE BuyerUser b SET b.lastLoginAt = :lastLoginAt WHERE b.buyerUserId = :buyerUserId")
    void updateLastLogin(@Param("buyerUserId") Long buyerUserId, @Param("lastLoginAt") LocalDateTime lastLoginAt);

    @Modifying
    @Transactional
    @Query("UPDATE BuyerUser b SET b.isAccountLocked = :locked WHERE b.buyerUserId = :buyerUserId")
    void updateAccountLocked(@Param("buyerUserId") Long buyerUserId, @Param("locked") boolean locked);
}
