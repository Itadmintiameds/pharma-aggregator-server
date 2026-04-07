package com.example.pharmaaggregatorserver.dto.seller.profile;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CoordinatorEmailDTO {
    private String coordinatorName;
    private String sellerCompanyName;
    private String oldName;
    private String newName;
    private String oldDesignation;
    private String newDesignation;
    private String oldMobile;
    private String newMobile;
    private String oldEmail;
    private String newEmail;
    private String temporaryPassword;
    private String loginUrl;
    private String supportEmail;
}
