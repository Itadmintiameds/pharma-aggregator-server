package com.example.pharmaaggregatorserver.controller.temp.seller;


import com.example.pharmaaggregatorserver.dto.admin.TempSellerAdminResponseDTO;
import com.example.pharmaaggregatorserver.dto.seller.TempSellerDraftRequestDTO;
import com.example.pharmaaggregatorserver.dto.seller.TempSellerRequestDTO;
import com.example.pharmaaggregatorserver.dto.seller.TempSellerResponseDTO;
import com.example.pharmaaggregatorserver.dto.temp.seller.*;
import com.example.pharmaaggregatorserver.entity.temp.seller.TempSeller;
import com.example.pharmaaggregatorserver.response.ApiResponse;
import com.example.pharmaaggregatorserver.service.temp.seller.TempSellerCoordinatorService;
import com.example.pharmaaggregatorserver.service.temp.seller.TempSellerDocumentService;
import com.example.pharmaaggregatorserver.service.temp.seller.TempSellerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/temp-sellers")
@RequiredArgsConstructor
public class TempSellerController {

    private final TempSellerService tempSellerService;
    private final TempSellerDocumentService tempSellerDocumentService;

    @PostMapping
    public ResponseEntity<TempSellerResponseDTO> createTempSeller(
            @Valid @RequestBody TempSellerRequestDTO requestDTO) {
        TempSellerResponseDTO response = tempSellerService.createTempSeller(requestDTO);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<?> getAllTempSellers() {
        List<TempSellerAdminResponseDTO> allTempSellers = tempSellerService.getALLTempSellers();
        return ResponseEntity.ok(new ApiResponse<>(
                HttpStatus.OK.toString(),
                "Temporary Sellers Fetched successfully",
                allTempSellers,
                (long) allTempSellers.size()
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getTempSellerById(@PathVariable Long id) {
        TempSeller seller = tempSellerService.findById(id);
        return ResponseEntity.ok(new ApiResponse<>(
                HttpStatus.OK.toString(),
                "Temporary Sellers Fetched successfully",
                seller
        ));
    }

    // Find the (pending) TempSeller registration submitted by the logged-in user, if any.
    // 404 means the user hasn't submitted a registration yet — signup only.
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getTempSellerByUserId(@PathVariable Long userId) {
        TempSeller seller = tempSellerService.findByUserId(userId)
                .orElseThrow(() -> new com.example.pharmaaggregatorserver.exception.NotFoundException(
                        "No temp seller registration found for user id: " + userId));
        return ResponseEntity.ok(new ApiResponse<>(
                HttpStatus.OK.toString(),
                "Temporary Seller fetched successfully",
                seller
        ));
    }

    // PATCH /api/admin/temp-sellers/{id}/verify/gst
    // { "isGstVerified": true }
    @PatchMapping("/{id}/verify/gst")
    public ResponseEntity<ApiResponse<Void>> verifyGst(
            @PathVariable Long id,
            @RequestBody GstVerificationRequestDTO requestDTO) {

        tempSellerService.updateGstVerification(id, requestDTO.isGstVerified());
        return ResponseEntity.ok(new ApiResponse<>("SUCCESS", "GST verification updated successfully", null));
    }

    // PATCH /api/admin/temp-sellers/{id}/verify/document
    // { "documentId": 5, "isDocumentVerified": true }
    @PatchMapping("/{id}/verify/document")
    public ResponseEntity<ApiResponse<Void>> verifyDocument(
            @PathVariable Long id,
            @RequestBody DocumentVerificationRequestDTO requestDTO) {

        tempSellerService.updateDocumentVerification(id, requestDTO.getDocumentId(), requestDTO.isDocumentVerified());
        return ResponseEntity.ok(new ApiResponse<>("SUCCESS", "Document verification updated successfully", null));
    }

    // PATCH /api/admin/temp-sellers/{id}/verify/bank
    // { "isBankDocumentVerified": true }
    @PatchMapping("/{id}/verify/bank")
    public ResponseEntity<ApiResponse<Void>> verifyBankDocument(
            @PathVariable Long id,
            @RequestBody BankVerificationRequestDTO requestDTO) {

        tempSellerService.updateBankDocumentVerification(id, requestDTO.isBankDocumentVerified());
        return ResponseEntity.ok(new ApiResponse<>("SUCCESS", "Bank document verification updated successfully", null));
    }

    // PATCH /api/admin/temp-sellers/{id}/verify/company-registration-certificate
    // { "isCompanyRegistrationCertificateVerified": true }
    @PatchMapping("/{id}/verify/company-registration-certificate")
    public ResponseEntity<ApiResponse<Void>> verifyCompanyRegistrationCertificate(
            @PathVariable Long id,
            @RequestBody CompanyRegistrationCertificateVerificationRequestDTO requestDTO) {

        tempSellerService.updateCompanyRegistrationCertificateVerification(id, requestDTO.isCompanyRegistrationCertificateVerified());
        return ResponseEntity.ok(new ApiResponse<>("SUCCESS", "Company registration certificate verification updated successfully", null));
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteByID(@PathVariable Long id) {
        tempSellerService.deleteTempSeller(id);
        return ResponseEntity.ok(new ApiResponse<>("SUCCESS", "Delete temp seller successfully", null));
    }

    @DeleteMapping("/both/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        tempSellerService.deleteBothSellerAndTempSeller(id);
        return ResponseEntity.ok(new ApiResponse<>("SUCCESS", "Delete temp seller successfully", null));
    }
    //Get All Coordinator

    @Autowired
    private TempSellerCoordinatorService coordinatorService;

    //Seller registration
    @GetMapping("/coordinator/check-email")
    public ResponseEntity<Boolean> checkEmailExists(@RequestParam String email) {
        boolean exists = coordinatorService.checkEmailExists(email);
        return ResponseEntity.ok(exists);
    }

    //Seller registration
    @GetMapping("/coordinator/check-phone")
    public ResponseEntity<Boolean> checkPhoneExists(@RequestParam String mobile) {
        boolean exists = coordinatorService.checkPhoneExists(mobile);
        return ResponseEntity.ok(exists);
    }

    //Seller registration
    @GetMapping("/coordinator/check-document")
    public ResponseEntity<Boolean> checkDocumentExists(@RequestParam String documentnumber) {
        boolean exists = coordinatorService.checkDocumentExists(documentnumber);
        return ResponseEntity.ok(exists); //checkGSTNumberExists
    }

    //Seller registration
    @GetMapping("/coordinator/check-gstnumber")
    public ResponseEntity<Boolean> checkGstNumberExists(@RequestParam String gstnumber) {
        boolean exists = coordinatorService.checkGSTNumberExists(gstnumber);
        return ResponseEntity.ok(exists);
    }

    //Seller profile update
    @GetMapping("/coordinator/check-profileemail")
    public ResponseEntity<Boolean> checkProfileEmailExists(@RequestParam String email) {
        boolean exists = coordinatorService.checkProfileEmailExists(email);
        return ResponseEntity.ok(exists);
    }

    //Seller profile update
    @GetMapping("/coordinator/check-profilephone")
    public ResponseEntity<Boolean> checkProfilePhoneExists(@RequestParam String mobile) {
        boolean exists = coordinatorService.checkProfilePhoneExists(mobile);
        return ResponseEntity.ok(exists);
    }

    //Seller profile update
    @GetMapping("/coordinator/check-profiledocument")
    public ResponseEntity<Boolean> checkProfileDocumentExists(@RequestParam String documentnumber) {
        boolean exists = coordinatorService.checkProfileDocumentExists(documentnumber);
        return ResponseEntity.ok(exists); //checkGSTNumberExists
    }

    //Seller profile update
    @GetMapping("/coordinator/check-profilegstnumber")
    public ResponseEntity<Boolean> checkProfileGstNumberExists(@RequestParam String gstnumber) {
        boolean exists = coordinatorService.checkProfileGSTNumberExists(gstnumber);
        return ResponseEntity.ok(exists);
    }


    @PostMapping(value = "/{tempSellerId}/documents/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<TempSellerDocumentUploadResponse>> uploadDocuments(
            @PathVariable Long tempSellerId,
            @RequestPart(value = "sellerImage", required = false) MultipartFile sellerImage,
            @RequestPart(value = "gstFile", required = false) MultipartFile gstFile,
            @RequestPart(value = "bankFile", required = false) MultipartFile bankFile,
            @RequestPart(value = "companyRegistrationCertificate", required = false) MultipartFile companyRegistrationCertificate,
            @RequestPart(value = "authorizationLetter", required = false) MultipartFile authorizationLetter,
            @RequestPart(value = "licenseFiles", required = false) List<MultipartFile> licenseFiles,
            @RequestParam(value = "licenseNames", required = false) List<String> licenseNames,
            @RequestParam(value = "documentIds", required = false) List<Long> documentIds) {

        TempSellerDocumentUploadRequest request = new TempSellerDocumentUploadRequest();
        request.setSellerImage(sellerImage);
        request.setGstFile(gstFile);
        request.setBankFile(bankFile);
        request.setCompanyRegistrationCertificate(companyRegistrationCertificate);
        request.setAuthorizationLetter(authorizationLetter);
        request.setLicenseFiles(licenseFiles);
        request.setLicenseNames(licenseNames);
        request.setDocumentIds(documentIds);

        TempSellerDocumentUploadResponse response =
                tempSellerDocumentService.uploadDocuments(tempSellerId, request);

        return ResponseEntity.ok(new ApiResponse<>(
                HttpStatus.OK.toString(),
                "Documents uploaded successfully",
                response
        ));
    }

    // ── Single-file delete endpoints (draft flow) ───────────────────────────
    // Each deletes the underlying S3 object (if a real one exists — never for
    // a "PENDING" placeholder) and resets the field, without touching any
    // other part of the registration. Mirrors the per-field granularity of
    // uploadDocuments above.

    @DeleteMapping("/{tempSellerId}/files/company-registration-certificate")
    public ResponseEntity<ApiResponse<Void>> deleteCompanyRegistrationCertificate(@PathVariable Long tempSellerId) {
        tempSellerDocumentService.deleteCompanyRegistrationCertificate(tempSellerId);
        return ResponseEntity.ok(new ApiResponse<>("SUCCESS", "Company registration certificate deleted successfully", null));
    }

    @DeleteMapping("/{tempSellerId}/files/gst")
    public ResponseEntity<ApiResponse<Void>> deleteGstFile(@PathVariable Long tempSellerId) {
        tempSellerDocumentService.deleteGstFile(tempSellerId);
        return ResponseEntity.ok(new ApiResponse<>("SUCCESS", "GST file deleted successfully", null));
    }

    @DeleteMapping("/{tempSellerId}/files/authorization-letter")
    public ResponseEntity<ApiResponse<Void>> deleteAuthorizationLetter(@PathVariable Long tempSellerId) {
        tempSellerDocumentService.deleteAuthorizationLetter(tempSellerId);
        return ResponseEntity.ok(new ApiResponse<>("SUCCESS", "Authorization letter deleted successfully", null));
    }

    @DeleteMapping("/{tempSellerId}/files/bank-document")
    public ResponseEntity<ApiResponse<Void>> deleteBankDocument(@PathVariable Long tempSellerId) {
        tempSellerDocumentService.deleteBankDocument(tempSellerId);
        return ResponseEntity.ok(new ApiResponse<>("SUCCESS", "Bank document deleted successfully", null));
    }

    @DeleteMapping("/{tempSellerId}/documents/{documentId}/file")
    public ResponseEntity<ApiResponse<Void>> deleteDocumentFile(@PathVariable Long tempSellerId,
                                                                @PathVariable Long documentId) {
        tempSellerDocumentService.deleteDocumentFile(tempSellerId, documentId);
        return ResponseEntity.ok(new ApiResponse<>("SUCCESS", "Document file deleted successfully", null));
    }

    @PutMapping("/{tempSellerId}")
    public ResponseEntity<?> updateTempSeller(@PathVariable("tempSellerId") Long tempSellerId,
                                              @RequestBody TempSellerRequestDTO tempSellerRequestDTO) {
        TempSellerResponseDTO responseDto = tempSellerService.updateTempSeller(tempSellerId, tempSellerRequestDTO);
        return ResponseEntity.ok(responseDto);
    }

    // ── Save-draft flow ──────────────────────────────────────────────────────
    // Every field of TempSellerDraftRequestDTO is optional — no @Valid here,
    // by design, so a registration can be saved mid-fill-out.

    @PostMapping("/draft")
    public ResponseEntity<TempSellerResponseDTO> createDraft(@RequestBody TempSellerDraftRequestDTO draftRequestDTO) {
        TempSellerResponseDTO response = tempSellerService.saveDraft(null, draftRequestDTO);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/draft/{tempSellerId}")
    public ResponseEntity<TempSellerResponseDTO> updateDraft(@PathVariable Long tempSellerId,
                                                              @RequestBody TempSellerDraftRequestDTO draftRequestDTO) {
        TempSellerResponseDTO response = tempSellerService.saveDraft(tempSellerId, draftRequestDTO);
        return ResponseEntity.ok(response);
    }

    // Promotes a DRAFT to a fully-submitted registration — full validation
    // applies here, same as POST /temp-sellers.
    @PostMapping("/draft/{tempSellerId}/finalize")
    public ResponseEntity<TempSellerResponseDTO> finalizeDraft(@PathVariable Long tempSellerId,
                                                                @Valid @RequestBody TempSellerRequestDTO requestDTO) {
        TempSellerResponseDTO response = tempSellerService.finalizeDraft(tempSellerId, requestDTO);
        return ResponseEntity.ok(response);
    }
}
