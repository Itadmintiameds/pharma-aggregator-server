package com.example.pharmaaggregatorserver.entity.auth;

import com.example.pharmaaggregatorserver.entity.master.RoleMaster;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "tbl_user")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "user_id")
    private String userId;

    // coordinator email from TempSellerCoordinator
    @Column(name = "username", unique = true, nullable = false, length = 100)
    private String username;

    // BCrypt hashed — NEVER store plain text
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    // true = seller must change password on first login
    @Column(name = "is_password_temporary", nullable = false)
    private boolean isPasswordTemporary = true;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @Column(name = "is_account_locked", nullable = false)
    private boolean isAccountLocked = false;

    @Column(name = "failed_login_attempts", nullable = false)
    private int failedLoginAttempts = 0;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "tbl_user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"),
            uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "role_id"})
    )
    private Set<RoleMaster> roles = new HashSet<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}