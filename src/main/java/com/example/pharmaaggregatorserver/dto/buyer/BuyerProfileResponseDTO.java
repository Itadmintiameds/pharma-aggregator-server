package com.example.pharmaaggregatorserver.dto.buyer;

import lombok.Builder;
import lombok.Getter;

/**
 * Minimal approved-Buyer projection keyed off the logged-in BuyerUser. Exists
 * because nothing on the frontend has any way to resolve the Buyer business ID
 * (tbl_buyer.buyer_id — the FK order placement/history need) from the
 * buyerUserId a buyer's login response carries; see BuyerProfileService.
 */
@Getter
@Builder
public class BuyerProfileResponseDTO {
    private String buyerId;
    private String organizationName;
    private String status;
}
