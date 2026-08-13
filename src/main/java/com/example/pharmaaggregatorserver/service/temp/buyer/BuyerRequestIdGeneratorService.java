package com.example.pharmaaggregatorserver.service.temp.buyer;

import com.example.pharmaaggregatorserver.repository.temp.buyer.TempBuyerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Generates sequential "BREQ-NNN" request IDs for TempBuyer registrations.
 * Mirrors service.temp.seller.RequestIdGeneratorService.
 */
@Service
@RequiredArgsConstructor
public class BuyerRequestIdGeneratorService {

    private final TempBuyerRepository tempBuyerRepository;

    private static final String PREFIX = "BREQ-";
    private static final int PADDING_LENGTH = 3;

    @Transactional
    public synchronized String generateNextRequestId() {
        String maxRequestId = tempBuyerRepository.findMaxRequestId()
                .orElse(null);

        if (maxRequestId == null || !maxRequestId.startsWith(PREFIX)) {
            return PREFIX + String.format("%0" + PADDING_LENGTH + "d", 1);
        }

        try {
            String numericPart = maxRequestId.substring(PREFIX.length());
            int maxNumber = Integer.parseInt(numericPart);
            int nextNumber = maxNumber + 1;

            return PREFIX + String.format("%0" + PADDING_LENGTH + "d", nextNumber);
        } catch (NumberFormatException e) {
            return PREFIX + String.format("%0" + PADDING_LENGTH + "d", 1);
        }
    }
}
