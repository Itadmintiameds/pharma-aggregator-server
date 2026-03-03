package com.example.pharmaaggregatorserver.repository.seller.SellerLogIn;

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
public interface SellerUserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    @Query("SELECT u FROM User u WHERE u.resetPasswordToken = :token AND u.resetPasswordExpires > :now")
    Optional<User> findByValidResetToken(@Param("token") String token, @Param("now") LocalDateTime now);

    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.resetPasswordToken = :token, u.resetPasswordExpires = :expiry WHERE u.username = :username")
    int updateResetToken(@Param("username") String username,
                         @Param("token") String token,
                         @Param("expiry") LocalDateTime expiry);

    boolean existsByUsername(String username);
}