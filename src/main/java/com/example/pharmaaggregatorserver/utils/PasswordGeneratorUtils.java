package com.example.pharmaaggregatorserver.utils;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class PasswordGeneratorUtils {

    // Secure random for passwords
    private static final SecureRandom RANDOM = new SecureRandom();

    private static final String UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String DIGITS = "0123456789";
    private static final String SPECIAL = "@#$%&*!";
    private static final String ALL = UPPER + LOWER + DIGITS + SPECIAL;

    // Generates a strong 12-character temporary password
    // Always contains at least 1 uppercase, 1 lowercase, 1 digit, 1 special char
    public String generateTemporaryPassword() {
        StringBuilder password = new StringBuilder();

        // Guarantee at least one of each required character type
        password.append(UPPER.charAt(RANDOM.nextInt(UPPER.length())));
        password.append(LOWER.charAt(RANDOM.nextInt(LOWER.length())));
        password.append(DIGITS.charAt(RANDOM.nextInt(DIGITS.length())));
        password.append(SPECIAL.charAt(RANDOM.nextInt(SPECIAL.length())));

        // Fill remaining 8 characters from the full set
        for (int i = 4; i < 12; i++) {
            password.append(ALL.charAt(RANDOM.nextInt(ALL.length())));
        }

        // Shuffle to avoid predictable pattern (e.g., always Upper first)
        char[] chars = password.toString().toCharArray();
        for (int i = chars.length - 1; i > 0; i--) {
            int j = RANDOM.nextInt(i + 1);
            char temp = chars[i];
            chars[i] = chars[j];
            chars[j] = temp;
        }

        return new String(chars);
    }
}