package com.example.pharmaaggregatorserver.controller.temp.seller;


import com.example.pharmaaggregatorserver.dto.admin.TempSellerAdminResponseDTO;
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

    @GetMapping("/coordinator/check-email")
    public ResponseEntity<Boolean> checkEmailExists(@RequestParam String email) {
        boolean exists = coordinatorService.checkEmailExists(email);
        return ResponseEntity.ok(exists);
    }

    @GetMapping("/coordinator/check-phone")
    public ResponseEntity<Boolean> checkPhoneExists(@RequestParam String mobile) {
        boolean exists = coordinatorService.checkPhoneExists(mobile);
        return ResponseEntity.ok(exists);
    }

    @PostMapping(value = "/{tempSellerId}/documents/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<TempSellerDocumentUploadResponse>> uploadDocuments(
            @PathVariable Long tempSellerId,
            @RequestPart(value = "sellerImage", required = false) MultipartFile sellerImage,
            @RequestPart(value = "gstFile", required = false) MultipartFile gstFile,
            @RequestPart(value = "bankFile", required = false) MultipartFile bankFile,
            @RequestPart(value = "licenseFiles", required = false) List<MultipartFile> licenseFiles,
            @RequestParam(value = "licenseNames", required = false) List<String> licenseNames,
            @RequestParam(value = "documentIds", required = false) List<Long> documentIds) {

        TempSellerDocumentUploadRequest request = new TempSellerDocumentUploadRequest();
        request.setSellerImage(sellerImage);
        request.setGstFile(gstFile);
        request.setBankFile(bankFile);
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

}
