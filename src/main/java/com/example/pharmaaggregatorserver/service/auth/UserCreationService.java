package com.example.pharmaaggregatorserver.service.auth;

import com.example.pharmaaggregatorserver.entity.auth.User;
import com.example.pharmaaggregatorserver.entity.master.RoleMaster;
import com.example.pharmaaggregatorserver.repository.auth.RoleMasterRepository;
import com.example.pharmaaggregatorserver.repository.auth.UserRepository;
import com.example.pharmaaggregatorserver.utils.PasswordGeneratorUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserCreationService {

    private final UserRepository userRepository;
    private final RoleMasterRepository roleMasterRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordGeneratorUtils passwordGeneratorUtils;

    /**
     * Creates a User for a seller.
     * Called during TempSeller → Seller approval.
     *
     * @param coordinatorEmail  used as username
     * @return plain text temp password (to be emailed — NOT stored anywhere)
     */
    public UserCreationResult createSellerUser(String coordinatorEmail) {

        // 1. Check if user already exists (safety check)
        if (userRepository.existsByUsername(coordinatorEmail)) {
            throw new IllegalStateException(
                    "User already exists for email: " + coordinatorEmail
            );
        }

        // 2. Fetch SELLER role from DB
        RoleMaster sellerRole = roleMasterRepository.findByRoleName("SELLER")
                .orElseThrow(() -> new IllegalStateException(
                        "SELLER role not found in tbl_role_master. Please seed the roles table."
                ));

        // 3. Generate temp password (plain text — only used here, never stored)
        String plainTempPassword = passwordGeneratorUtils.generateTemporaryPassword();

        // 4. Hash the password — this is what gets stored in DB
        String hashedPassword = passwordEncoder.encode(plainTempPassword);

        // 5. Build the User entity
        User user = new User();
        user.setUsername(coordinatorEmail);        // coordinator email = login username
        user.setPasswordHash(hashedPassword);      // BCrypt hash stored in DB
        user.setPasswordTemporary(true);           // forces password change on first login
        user.setActive(true);
        user.setAccountLocked(false);
        user.setFailedLoginAttempts(0);
        user.setRoles(Set.of(sellerRole));

        // 6. Save to tbl_user
        User savedUser = userRepository.save(user);
        log.info("User created for seller coordinator: {}", coordinatorEmail);

        // 7. Return both saved user and plain password
        //    Plain password is returned ONLY to be emailed — never logged or stored
        return new UserCreationResult(savedUser, plainTempPassword);
    }

    // Simple result holder — keeps method return clean
    public record UserCreationResult(User user, String plainTempPassword) {}
}