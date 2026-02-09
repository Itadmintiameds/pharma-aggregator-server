package com.example.pharmaaggregatorserver.service.serviceImpl.admin;

import com.example.pharmaaggregatorserver.dto.seller.SellerApprovalRequestDTO;
import com.example.pharmaaggregatorserver.entity.temp.seller.TempSeller;
import com.example.pharmaaggregatorserver.exception.ApplicationException;
import com.example.pharmaaggregatorserver.exception.NotFoundException;
import com.example.pharmaaggregatorserver.repository.temp.seller.TempSellerRepository;
import com.example.pharmaaggregatorserver.service.EmailService;
import com.example.pharmaaggregatorserver.service.PdfService;
import com.example.pharmaaggregatorserver.service.admin.SellerApprovalService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SellerApprovalServiceImpl implements SellerApprovalService {

    private final TempSellerRepository tempSellerRepo;
    //    private final SellerRepository sellerRepo;
    private final EmailService emailService;
    private final PdfService pdfService;
//    private final UserService userService;

    @Override
    public void processReview(SellerApprovalRequestDTO request) {

        TempSeller tempSeller = tempSellerRepo.findById(request.getId())
                .orElseThrow(() -> new NotFoundException("Seller not found"));

        switch (request.getStatus().toUpperCase()) {

            case "CORRECTION" -> handleCorrection(tempSeller, request.getComments());

            case "REJECT" -> handleRejection(tempSeller, request.getComments());

//            case "ACCEPT" -> handleApproval(tempSeller);

            default -> throw new ApplicationException("Invalid Status");
        }
    }

    private void handleCorrection(TempSeller seller, String comments) {

        seller.setStatus("CORRECTION_REQUIRED");
        tempSellerRepo.save(seller);

        String correctionUrl = "https://testdomain.com/seller/correction/" + seller.getTempSellerId();

        emailService.sendMail(
                seller.getEmail(),
                "Correction Required",
                "Please correct your application.\nComments: " + comments +
                        "\nUpdate here: " + correctionUrl
        );
    }

    private void handleRejection(TempSeller seller, String comments) {

        seller.setStatus("REJECTED");
        tempSellerRepo.save(seller);

        emailService.sendMail(
                seller.getEmail(),
                "Application Rejected",
                "Your seller registration was rejected.\nReason: " + comments
        );
    }

//    private void handleApproval(TempSeller tempSeller) {
//
//        // 1️⃣ Move to Main Seller Table
//        Seller seller = mapToMainSeller(tempSeller);
//        sellerRepo.save(seller);
//
//        // 2️⃣ Generate PDF
//        String pdfPath = pdfService.generateSellerAgreementPdf(seller);
//
//        // 3️⃣ Create Login Credentials
//        String username = userService.generateUsername(seller);
//        String password = userService.generateRandomPassword();
//        userService.createUserAccount(seller, username, password);
//
//        // 4️⃣ Send Reset Password Link
//        String resetLink = userService.generateResetLink(seller);
//
//        // 5️⃣ Email
//        emailService.sendMail(
//                seller.getEmail(),
//                "Seller Approved 🎉",
//                "Your account is approved.\nUsername: " + username +
//                        "\nReset Password: " + resetLink +
//                        "\nAgreement PDF attached."
//        );
//
//        // 6️⃣ Mark Temp Seller as Completed
//        tempSeller.setStatus("APPROVED");
//        tempSellerRepo.save(tempSeller);
//    }
//
//    private Seller mapToMainSeller(TempSeller temp) {
//        Seller seller = new Seller();
//        seller.setName(temp.getName());
//        seller.setEmail(temp.getEmail());
//        seller.setPhone(temp.getPhone());
//        seller.setAddress(temp.getAddress());
//        return seller;
//    }

}
