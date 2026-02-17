package com.example.pharmaaggregatorserver.dto.seller.OnSubmit;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmailRequestDTO {

    // Application Details
    private String applicationRequestId;
    private String submissionDate;
    private String applicationStatus;

    // Seller Information
    private String sellerName;
    private String sellerEmail;
    private String sellerPhone;

    // Coordinator Information
    private String coordinatorName;
    private String coordinatorEmail;
    private String coordinatorMobile;
    private String coordinatorDesignation;

    // ============================================================
    // 🏢 ADDRESS INFORMATION - ADD THESE FIELDS
    // ============================================================
    private String addressCity;
    private String addressStreet;
    private String addressBuildingNo;
    private String addressLandmark;
    private String addressPinCode;
    private String addressState;
    private String addressDistrict;
    private String addressTaluka;

    // ============================================================
    // 🏦 BANK DETAILS - ADD THESE FIELDS (Optional)
    // ============================================================
    private String bankName;
    private String bankBranch;
    private String bankIfscCode;
    private String bankAccountNumber;
    private String bankAccountHolderName;

    // ============================================================
    // 📄 DOCUMENT INFORMATION - ADD THESE FIELDS (Optional)
    // ============================================================
    private String gstNumber;
    private String documentNumber;
}