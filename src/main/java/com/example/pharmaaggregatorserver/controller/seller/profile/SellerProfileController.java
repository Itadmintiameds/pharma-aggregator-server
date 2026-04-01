package com.example.pharmaaggregatorserver.controller.seller.profile;

import com.example.pharmaaggregatorserver.dto.product.ProductTypeResponseDto;
import com.example.pharmaaggregatorserver.dto.seller.profile.PendingSellerDocumentUploadRequest;
import com.example.pharmaaggregatorserver.dto.seller.profile.PendingSellerDocumentUploadResponse;
import com.example.pharmaaggregatorserver.dto.seller.profile.SellerEditRequest;
import com.example.pharmaaggregatorserver.dto.seller.profile.SellerResponseDTO;
import com.example.pharmaaggregatorserver.entity.seller.Seller;
import com.example.pharmaaggregatorserver.response.ApiResponse;
import com.example.pharmaaggregatorserver.security.UserDetailsImpl;
import com.example.pharmaaggregatorserver.service.profile.PendingSellerDocumentService;
import com.example.pharmaaggregatorserver.service.profile.SellerProfileService;
import com.example.pharmaaggregatorserver.service.seller.SellerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/sellers")
@RequiredArgsConstructor
public class SellerProfileController {

    private final SellerProfileService sellerprofileService;
    private final SellerService sellerService;
    private final PendingSellerDocumentService pendingSellerDocumentService;

    // -----------------------------------------------------------------------
    // STEP 1 — Submit seller profile update request (metadata only, no files)
    // Response contains pendingSellerId → pass it to STEP 2
    // -----------------------------------------------------------------------
    @PutMapping("/{sellerId}/request-update")
    public ResponseEntity<SellerResponseDTO> requestSellerUpdate(
            @PathVariable String sellerId,
            @Valid @RequestBody SellerEditRequest request,
            @RequestParam String requestedBy) {

        SellerResponseDTO response = sellerprofileService.requestSellerUpdate(sellerId, request, requestedBy);
        return ResponseEntity.ok(response);
    }
    // Find Seller by User ID
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> findSellerByUserId(@PathVariable Long userId) {
        Seller seller = sellerService.findSellerByUserId(userId);
        return ResponseEntity.ok(new ApiResponse<>("SUCCESS", "Seller found", seller));
    }

    // -----------------------------------------------------------------------
    // STEP 2 — Upload documents for the pending seller created in STEP 1
    // If this returns an error, frontend must call DELETE /{pendingSellerId}/cancel
    // -----------------------------------------------------------------------
    @PostMapping(value = "/{pendingSellerId}/documents/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<PendingSellerDocumentUploadResponse>> uploadDocuments(
            @PathVariable Long pendingSellerId,
            @RequestPart(value = "gstFile",         required = false) MultipartFile gstFile,
            @RequestPart(value = "bankFile",         required = false) MultipartFile bankFile,
            @RequestPart(value = "companyRegistrationCertificate", required = false) MultipartFile companyRegistrationCertificate,
            @RequestPart(value = "licenseFiles",     required = false) List<MultipartFile> licenseFiles,
            @RequestParam(value = "licenseNames",    required = false) List<String> licenseNames,
            @RequestParam(value = "documentIds",     required = false) List<Long> documentIds) {

        PendingSellerDocumentUploadRequest request = new PendingSellerDocumentUploadRequest();
        request.setGstFile(gstFile);
        request.setBankFile(bankFile);
        request.setCompanyRegistrationCertificate(companyRegistrationCertificate);
        request.setLicenseFiles(licenseFiles);
        request.setLicenseNames(licenseNames);
        request.setDocumentIds(documentIds);

        PendingSellerDocumentUploadResponse response =
                pendingSellerDocumentService.uploadDocuments(pendingSellerId, request);

        return ResponseEntity.ok(new ApiResponse<>(
                HttpStatus.OK.toString(),
                "Documents uploaded successfully",
                response
        ));
    }

    // -----------------------------------------------------------------------
    // ROLLBACK — Delete the pending seller when STEP 2 upload fails
    // Frontend calls this immediately after upload-documents returns an error
    // -----------------------------------------------------------------------
    @DeleteMapping("/pendingSellerId/{pendingSellerId}")
    public ResponseEntity<ApiResponse<Void>> cancelPendingSeller(
            @PathVariable Long pendingSellerId) {

        sellerprofileService.deletePendingSeller(pendingSellerId);
        return ResponseEntity.ok(new ApiResponse<>("SUCCESS", "Pending seller request cancelled successfully", null));
    }

    @GetMapping("/product-types")
    public ResponseEntity<ProductTypeResponseDto> getProductTypes(
            Authentication authentication
    ) {

        UserDetailsImpl user = (UserDetailsImpl) authentication.getPrincipal();
        Long userId = user.getId();

        return ResponseEntity.ok(
                sellerService.getProductTypesForLoggedInUser(userId)
        );
    }
}