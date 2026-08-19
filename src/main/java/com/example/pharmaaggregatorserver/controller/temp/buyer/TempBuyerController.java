package com.example.pharmaaggregatorserver.controller.temp.buyer;

import com.example.pharmaaggregatorserver.dto.admin.TempBuyerAdminResponseDTO;
import com.example.pharmaaggregatorserver.dto.buyer.TempBuyerResponseDTO;
import com.example.pharmaaggregatorserver.dto.temp.buyer.DocumentVerificationRequestDTO;
import com.example.pharmaaggregatorserver.dto.temp.buyer.GstVerificationRequestDTO;
import com.example.pharmaaggregatorserver.dto.temp.buyer.PanVerificationRequestDTO;
import com.example.pharmaaggregatorserver.dto.temp.buyer.TempBuyerDocumentUploadRequest;
import com.example.pharmaaggregatorserver.dto.temp.buyer.TempBuyerDocumentUploadResponse;
import com.example.pharmaaggregatorserver.dto.temp.buyer.TempBuyerDraftRequestDTO;
import com.example.pharmaaggregatorserver.dto.temp.buyer.TempBuyerRequestDTO;
import com.example.pharmaaggregatorserver.entity.temp.buyer.TempBuyer;
import com.example.pharmaaggregatorserver.exception.NotFoundException;
import com.example.pharmaaggregatorserver.exception.UnauthorizedException;
import com.example.pharmaaggregatorserver.response.ApiResponse;
import com.example.pharmaaggregatorserver.security.UserDetailsImpl;
import com.example.pharmaaggregatorserver.service.temp.buyer.TempBuyerContactService;
import com.example.pharmaaggregatorserver.service.temp.buyer.TempBuyerDocumentService;
import com.example.pharmaaggregatorserver.service.temp.buyer.TempBuyerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/temp-buyers")
@RequiredArgsConstructor
public class TempBuyerController {

    private final TempBuyerService tempBuyerService;
    private final TempBuyerDocumentService tempBuyerDocumentService;
    private final TempBuyerContactService contactService;

    @PostMapping
    public ResponseEntity<TempBuyerResponseDTO> createTempBuyer(@Valid @RequestBody TempBuyerRequestDTO requestDTO) {
        TempBuyerResponseDTO response = tempBuyerService.createTempBuyer(requestDTO);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // Lists every pending registration across all buyers — admin-review data,
    // so only an authenticated ROLE_ADMIN caller may fetch it.
    @GetMapping
    public ResponseEntity<?> getAllTempBuyers(Authentication authentication) {
        requireAdmin(authentication);
        List<TempBuyerAdminResponseDTO> all = tempBuyerService.getAllTempBuyers();
        return ResponseEntity.ok(new ApiResponse<>(
                HttpStatus.OK.toString(),
                "Temporary Buyers fetched successfully",
                all,
                (long) all.size()
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getTempBuyerById(@PathVariable Long id) {
        TempBuyer buyer = tempBuyerService.findById(id);
        return ResponseEntity.ok(new ApiResponse<>(
                HttpStatus.OK.toString(),
                "Temporary Buyer fetched successfully",
                buyer
        ));
    }

    // 404 means the user hasn't submitted a registration yet.
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getTempBuyerByUserId(@PathVariable Long userId) {
        TempBuyer buyer = tempBuyerService.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("No temp buyer registration found for user id: " + userId));
        return ResponseEntity.ok(new ApiResponse<>(
                HttpStatus.OK.toString(),
                "Temporary Buyer fetched successfully",
                buyer
        ));
    }

    @PatchMapping("/{id}/verify/gst")
    public ResponseEntity<ApiResponse<Void>> verifyGst(@PathVariable Long id,
                                                        @RequestBody GstVerificationRequestDTO requestDTO) {
        tempBuyerService.updateGstVerification(id, requestDTO.isGstVerified());
        return ResponseEntity.ok(new ApiResponse<>("SUCCESS", "GST verification updated successfully", null));
    }

    @PatchMapping("/{id}/verify/pan")
    public ResponseEntity<ApiResponse<Void>> verifyPan(@PathVariable Long id,
                                                        @RequestBody PanVerificationRequestDTO requestDTO) {
        tempBuyerService.updatePanVerification(id, requestDTO.isPanVerified());
        return ResponseEntity.ok(new ApiResponse<>("SUCCESS", "PAN verification updated successfully", null));
    }

    @PatchMapping("/{id}/verify/document")
    public ResponseEntity<ApiResponse<Void>> verifyDocument(@PathVariable Long id,
                                                             @RequestBody DocumentVerificationRequestDTO requestDTO) {
        tempBuyerService.updateDocumentVerification(id, requestDTO.getDocumentId(), requestDTO.isDocumentVerified());
        return ResponseEntity.ok(new ApiResponse<>("SUCCESS", "Document verification updated successfully", null));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteById(@PathVariable Long id, Authentication authentication) {
        requireOwnerOrAdmin(id, authentication);
        tempBuyerService.deleteTempBuyer(id);
        return ResponseEntity.ok(new ApiResponse<>("SUCCESS", "Delete temp buyer successfully", null));
    }

    @DeleteMapping("/both/{id}")
    public ResponseEntity<?> deleteBoth(@PathVariable Long id, Authentication authentication) {
        requireOwnerOrAdmin(id, authentication);
        tempBuyerService.deleteBothBuyerAndTempBuyer(id);
        return ResponseEntity.ok(new ApiResponse<>("SUCCESS", "Delete temp buyer successfully", null));
    }

    // ---------------- Access control helpers ----------------

    private void requireAdmin(Authentication authentication) {
        if (!isAdmin(authentication)) {
            throw new UnauthorizedException("Unauthorized access");
        }
    }

    // Only the buyer who owns this draft/registration, or an admin, may act on
    // it. A draft with no linked BuyerUser (shouldn't normally happen — every
    // create/draft request carries a buyerUserId) can only be removed by an
    // admin, since there is no owner to authenticate as.
    private void requireOwnerOrAdmin(Long tempBuyerId, Authentication authentication) {
        if (isAdmin(authentication)) {
            return;
        }
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetailsImpl user)) {
            throw new UnauthorizedException("Unauthorized access");
        }
        TempBuyer tempBuyer = tempBuyerService.findById(tempBuyerId);
        if (tempBuyer.getUser() == null || !tempBuyer.getUser().getBuyerUserId().equals(user.getId())) {
            throw new UnauthorizedException("Unauthorized access");
        }
    }

    private boolean isAdmin(Authentication authentication) {
        return authentication != null
                && authentication.getPrincipal() instanceof UserDetailsImpl user
                && user.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .anyMatch(role -> role.equals("ROLE_ADMIN"));
    }

    @GetMapping("/contact/check-email")
    public ResponseEntity<Boolean> checkEmailExists(@RequestParam String email,
                                                     @RequestParam(required = false) Long tempBuyerId) {
        return ResponseEntity.ok(contactService.checkEmailExists(email, tempBuyerId));
    }

    @GetMapping("/contact/check-phone")
    public ResponseEntity<Boolean> checkPhoneExists(@RequestParam String mobile,
                                                     @RequestParam(required = false) Long tempBuyerId) {
        return ResponseEntity.ok(contactService.checkPhoneExists(mobile, tempBuyerId));
    }

    @GetMapping("/contact/check-gstnumber")
    public ResponseEntity<Boolean> checkGstNumberExists(@RequestParam String gstnumber,
                                                         @RequestParam(required = false) Long tempBuyerId) {
        return ResponseEntity.ok(contactService.checkGstNumberExists(gstnumber, tempBuyerId));
    }

    @GetMapping("/contact/check-pannumber")
    public ResponseEntity<Boolean> checkPanNumberExists(@RequestParam String pannumber,
                                                         @RequestParam(required = false) Long tempBuyerId) {
        return ResponseEntity.ok(contactService.checkPanNumberExists(pannumber, tempBuyerId));
    }

    @GetMapping("/contact/check-document")
    public ResponseEntity<Boolean> checkDocumentExists(@RequestParam String documentnumber) {
        return ResponseEntity.ok(contactService.checkDocumentExists(documentnumber));
    }

    @PostMapping(value = "/{tempBuyerId}/documents/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<TempBuyerDocumentUploadResponse>> uploadDocuments(
            @PathVariable Long tempBuyerId,
            @RequestPart(value = "orgLogo", required = false) MultipartFile orgLogo,
            @RequestPart(value = "gstFile", required = false) MultipartFile gstFile,
            @RequestPart(value = "panFile", required = false) MultipartFile panFile,
            @RequestPart(value = "licenseFiles", required = false) List<MultipartFile> licenseFiles,
            @RequestParam(value = "licenseNames", required = false) List<String> licenseNames,
            @RequestParam(value = "documentIds", required = false) List<Long> documentIds) {

        TempBuyerDocumentUploadRequest request = new TempBuyerDocumentUploadRequest();
        request.setOrgLogo(orgLogo);
        request.setGstFile(gstFile);
        request.setPanFile(panFile);
        request.setLicenseFiles(licenseFiles);
        request.setLicenseNames(licenseNames);
        request.setDocumentIds(documentIds);

        TempBuyerDocumentUploadResponse response = tempBuyerDocumentService.uploadDocuments(tempBuyerId, request);

        return ResponseEntity.ok(new ApiResponse<>(
                HttpStatus.OK.toString(),
                "Documents uploaded successfully",
                response
        ));
    }

    @DeleteMapping("/{tempBuyerId}/files/gst")
    public ResponseEntity<ApiResponse<Void>> deleteGstFile(@PathVariable Long tempBuyerId) {
        tempBuyerDocumentService.deleteGstFile(tempBuyerId);
        return ResponseEntity.ok(new ApiResponse<>("SUCCESS", "GST file deleted successfully", null));
    }

    @DeleteMapping("/{tempBuyerId}/files/pan")
    public ResponseEntity<ApiResponse<Void>> deletePanFile(@PathVariable Long tempBuyerId) {
        tempBuyerDocumentService.deletePanFile(tempBuyerId);
        return ResponseEntity.ok(new ApiResponse<>("SUCCESS", "PAN file deleted successfully", null));
    }

    @DeleteMapping("/{tempBuyerId}/files/org-logo")
    public ResponseEntity<ApiResponse<Void>> deleteOrgLogo(@PathVariable Long tempBuyerId) {
        tempBuyerDocumentService.deleteOrgLogo(tempBuyerId);
        return ResponseEntity.ok(new ApiResponse<>("SUCCESS", "Org logo deleted successfully", null));
    }

    @DeleteMapping("/{tempBuyerId}/documents/{documentId}/file")
    public ResponseEntity<ApiResponse<Void>> deleteDocumentFile(@PathVariable Long tempBuyerId,
                                                                 @PathVariable Long documentId) {
        tempBuyerDocumentService.deleteDocumentFile(tempBuyerId, documentId);
        return ResponseEntity.ok(new ApiResponse<>("SUCCESS", "Document file deleted successfully", null));
    }

    @PutMapping("/{tempBuyerId}")
    public ResponseEntity<?> updateTempBuyer(@PathVariable Long tempBuyerId,
                                             @RequestBody TempBuyerRequestDTO requestDTO) {
        TempBuyerResponseDTO response = tempBuyerService.updateTempBuyer(tempBuyerId, requestDTO);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/draft")
    public ResponseEntity<TempBuyerResponseDTO> createDraft(@RequestBody TempBuyerDraftRequestDTO draftRequestDTO) {
        TempBuyerResponseDTO response = tempBuyerService.saveDraft(null, draftRequestDTO);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/draft/{tempBuyerId}")
    public ResponseEntity<TempBuyerResponseDTO> updateDraft(@PathVariable Long tempBuyerId,
                                                            @RequestBody TempBuyerDraftRequestDTO draftRequestDTO) {
        TempBuyerResponseDTO response = tempBuyerService.saveDraft(tempBuyerId, draftRequestDTO);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/draft/{tempBuyerId}/finalize")
    public ResponseEntity<TempBuyerResponseDTO> finalizeDraft(@PathVariable Long tempBuyerId,
                                                              @Valid @RequestBody TempBuyerRequestDTO requestDTO) {
        TempBuyerResponseDTO response = tempBuyerService.finalizeDraft(tempBuyerId, requestDTO);
        return ResponseEntity.ok(response);
    }
}
