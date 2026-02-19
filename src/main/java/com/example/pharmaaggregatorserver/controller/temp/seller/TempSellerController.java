package com.example.pharmaaggregatorserver.controller.temp.seller;


import com.example.pharmaaggregatorserver.dto.admin.TempSellerAdminResponseDTO;
import com.example.pharmaaggregatorserver.dto.seller.TempSellerRequestDTO;
import com.example.pharmaaggregatorserver.dto.seller.TempSellerResponseDTO;
import com.example.pharmaaggregatorserver.dto.temp.seller.BankVerificationRequestDTO;
import com.example.pharmaaggregatorserver.dto.temp.seller.DocumentVerificationRequestDTO;
import com.example.pharmaaggregatorserver.dto.temp.seller.GstVerificationRequestDTO;
import com.example.pharmaaggregatorserver.entity.temp.seller.TempSeller;
import com.example.pharmaaggregatorserver.entity.temp.seller.TempSellerCoordinator;
import com.example.pharmaaggregatorserver.response.ApiResponse;
import com.example.pharmaaggregatorserver.service.temp.seller.TempSellerCoordinatorService;
import com.example.pharmaaggregatorserver.service.temp.seller.TempSellerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/temp-sellers")
@RequiredArgsConstructor
public class TempSellerController {

    private final TempSellerService tempSellerService;

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

    //Get All Coordinator

    @Autowired
    private TempSellerCoordinatorService coordinatorService;

    @GetMapping("/coordinator/check-email")
    public ResponseEntity<Boolean> checkEmailExists(@RequestParam String email) {
        boolean exists = coordinatorService.checkEmailExists(email);
        return ResponseEntity.ok(exists);
    }

}
