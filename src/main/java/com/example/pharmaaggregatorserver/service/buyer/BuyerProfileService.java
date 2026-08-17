package com.example.pharmaaggregatorserver.service.buyer;

import com.example.pharmaaggregatorserver.dto.buyer.BuyerProfileResponseDTO;
import com.example.pharmaaggregatorserver.entity.buyer.Buyer;
import com.example.pharmaaggregatorserver.exception.ResourceNotFoundException;
import com.example.pharmaaggregatorserver.repository.buyer.BuyerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Resolves the approved Buyer business ID (tbl_buyer.buyer_id) for the
 * logged-in BuyerUser. Order placement/history need this buyerId, but a
 * buyer's login response only ever carries buyerUserId (BuyerUser's own PK) —
 * this fills that gap without touching the login/token flow itself.
 */
@Service
@RequiredArgsConstructor
public class BuyerProfileService {

    private final BuyerRepository buyerRepository;

    public BuyerProfileResponseDTO getByUserId(Long buyerUserId) {
        Buyer buyer = buyerRepository.findByUser_BuyerUserId(buyerUserId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No approved buyer profile found for user " + buyerUserId));

        return BuyerProfileResponseDTO.builder()
                .buyerId(buyer.getBuyerId())
                .organizationName(buyer.getOrganizationName())
                .status(buyer.getStatus())
                .build();
    }
}
