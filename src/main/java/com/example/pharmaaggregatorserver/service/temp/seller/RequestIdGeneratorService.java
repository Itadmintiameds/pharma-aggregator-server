package com.example.pharmaaggregatorserver.service.temp.seller;

import com.example.pharmaaggregatorserver.repository.temp.seller.TempSellerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RequestIdGeneratorService {

    private final TempSellerRepository tempSellerRepository;

    private static final String PREFIX = "REQ-";
    private static final int PADDING_LENGTH = 3;

    @Transactional
    public synchronized String generateNextRequestId() {
        // Get the maximum request ID
        String maxRequestId = tempSellerRepository.findMaxRequestId()
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
