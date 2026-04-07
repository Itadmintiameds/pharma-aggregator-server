package com.example.pharmaaggregatorserver.service.profile;

import com.example.pharmaaggregatorserver.dto.seller.profile.CoordinatorEmailDTO;
import com.example.pharmaaggregatorserver.dto.seller.profile.SellerUpdateEmailDTO;

public interface CoordinatorEmailService {
    void sendHtmlMail(String to, String subject, String htmlBody);
    void sendCoordinatorDetailsUpdateWithoutEmailChange(CoordinatorEmailDTO dto);
    void sendCoordinatorDetailsUpdateWithEmailChange(CoordinatorEmailDTO dto);
    void sendCoordinatorEmailChangeSecurityAlert(CoordinatorEmailDTO dto);

    // Seller update submission acknowledgement
    void sendSellerUpdateSubmissionAcknowledgement(SellerUpdateEmailDTO dto);

    // Seller update approved
    void sendSellerUpdateApproved(SellerUpdateEmailDTO dto);

    // Seller update rejected
    void sendSellerUpdateRejected(SellerUpdateEmailDTO dto);

    // ========== NEW METHODS FOR SellerProfileService ==========

    // Send seller profile approved email (for CREATE/UPDATE approvals)
    void sendSellerProfileApproved(String coordinatorEmail, String coordinatorName, String sellerName,
                                   String sellerId, String approvedBy, String requestType);

    // Send seller profile rejected email (for CREATE/UPDATE rejections)
    void sendSellerProfileRejected(String coordinatorEmail, String coordinatorName, String sellerName,
                                   String rejectionReason, String rejectedBy, String requestType);

    // Send auto-approval email notification
    void sendAutoApprovalEmail(String coordinatorEmail, String coordinatorName, String sellerId,
                               String sellerName, String approvedBy);
}