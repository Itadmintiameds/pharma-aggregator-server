package com.example.pharmaaggregatorserver.service.auth;

import com.example.pharmaaggregatorserver.entity.auth.User;
import com.example.pharmaaggregatorserver.entity.master.RoleMaster;
import com.example.pharmaaggregatorserver.exception.ApplicationException;
import com.example.pharmaaggregatorserver.repository.auth.RoleMasterRepository;
import com.example.pharmaaggregatorserver.repository.auth.UserRepository;
import com.example.pharmaaggregatorserver.utils.PasswordGeneratorUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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
     * @param coordinatorEmail used as username
     * @return plain text temp password (to be emailed — NOT stored anywhere)
     */
    public UserCreationResult createSellerUser(String coordinatorEmail) {

        // 1. Check if user already exists (safety check)
        if (userRepository.existsByUsername(coordinatorEmail)) {
            throw new ApplicationException(
                    HttpStatus.CONFLICT,
                    "A user account already exists for coordinator email: " + coordinatorEmail
            );
        }

        // 2. Fetch SELLER role from DB
        RoleMaster sellerRole = roleMasterRepository.findByRoleName("SELLER")
                .orElseThrow(() -> new ApplicationException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
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
// =========================================================================
    // Rotate credentials when the coordinator e-mail changes
    // Called during UPDATE-type approval when coordinator email has changed.
    // =========================================================================

    /**
     * Rotates the username and password for an existing seller {@link User}
     * when the coordinator's e-mail address is updated through an approved
     * profile change request.
     * <p>
     * What this method does:
     * <ol>
     *   <li>Looks up the User by {@code oldEmail} (their current username).</li>
     *   <li>Updates {@code username} to {@code newEmail}.</li>
     *   <li>Generates a new random temporary password, BCrypt-hashes it, stores the hash.</li>
     *   <li>Sets {@code isPasswordTemporary = true} so the coordinator is forced to
     *       change it on their very next login.</li>
     * </ol>
     * <p>
     * The plain-text password is returned so the caller
     * ({@link com.example.pharmaaggregatorserver.service.seller.history.SellerHistoryService})
     * can e-mail it to the new coordinator address. It is NEVER logged or stored.
     *
     * @param oldEmail the coordinator's previous e-mail (current value of tbl_user.username)
     * @param newEmail the coordinator's new e-mail    (new value of tbl_user.username)
     * @return {@link CredentialRotationResult} with the updated User and plain password
     * @throws IllegalStateException if no user record exists for {@code oldEmail}
     */
    public CredentialRotationResult rotateCoordinatorCredentials(String oldEmail, String newEmail) {

        // 1. Find the user by their current username (= old coordinator email)
        User user = userRepository.findByUsername(oldEmail)
                .orElseThrow(() -> new IllegalStateException(
                        "No user account found for coordinator email: " + oldEmail));

        // 2. Generate a new temporary password (plain — never stored)
        String plainTempPassword = passwordGeneratorUtils.generateTemporaryPassword();

        // 3. Hash for storage
        String hashedPassword = passwordEncoder.encode(plainTempPassword);

        // 4. Update user record
        user.setUsername(newEmail);               // new login username
        user.setPasswordHash(hashedPassword);     // new BCrypt hash
        user.setPasswordTemporary(true);          // force change on first login
        user.setUpdatedAt(LocalDateTime.now());

        // 5. Persist
        User savedUser = userRepository.save(user);
        log.info("Credentials rotated: userId={}, username [{}] → [{}]",
                savedUser.getUserId(), oldEmail, newEmail);

        // 6. Return saved entity + plain password for e-mailing
        return new CredentialRotationResult(savedUser, plainTempPassword);
    }

    // Simple result holder — keeps method return clean
    public record UserCreationResult(User user, String plainTempPassword) {
    }

    /**
     * Returned by {@link #rotateCoordinatorCredentials}.
     */
    public record CredentialRotationResult(User user, String plainTempPassword) {
    }
}