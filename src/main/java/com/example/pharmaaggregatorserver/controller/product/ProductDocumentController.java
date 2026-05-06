package com.example.pharmaaggregatorserver.controller.product;

import com.example.pharmaaggregatorserver.dto.product.CertificateUploadResponse;
import com.example.pharmaaggregatorserver.response.ApiResponse;
import com.example.pharmaaggregatorserver.security.UserDetailsImpl;
import com.example.pharmaaggregatorserver.service.product.productImpl.ProductDocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/product-documents")
@RequiredArgsConstructor
public class ProductDocumentController {

    private final ProductDocumentService productDocumentService;

    // ─────────────────────────────────────────────────────────────
    // NON-CONSUMABLE — CERTIFICATES
    // ─────────────────────────────────────────────────────────────

    /**
     * POST /product-documents/non-consumable/{productAttributeId}/certificates
     * <p>
     * multipart/form-data:
     * certificateFiles  → file1, file2, ...         (@RequestPart, repeated)
     * documentIds       → 4, 5, 6                   (@RequestParam, repeated, same order)
     * (productCertificateDocumentId from createProduct response)
     */
    @PostMapping(
            value = "/non-consumable/{productAttributeId}/certificates",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<ApiResponse<CertificateUploadResponse>> uploadNonConsumableCertificates(
            @PathVariable String productAttributeId,
            @RequestPart(value = "certificateFiles") List<MultipartFile> certificateFiles,
            @RequestParam(value = "documentIds") List<Long> documentIds,
            Authentication authentication
    ) {
        CertificateUploadResponse response = productDocumentService.uploadNonConsumableCertificates(
                productAttributeId, documentIds, certificateFiles, getUsername(authentication)
        );

        return ResponseEntity.ok(new ApiResponse<>(
                HttpStatus.OK.toString(),
                "Non-consumable certificates uploaded successfully",
                response
        ));
    }

    // ─────────────────────────────────────────────────────────────
    // NON-CONSUMABLE — BROCHURE
    // ─────────────────────────────────────────────────────────────

    /**
     * POST /product-documents/non-consumable/{productAttributeId}/brochure
     * <p>
     * multipart/form-data:
     * brochureFile → single PDF file (@RequestPart)
     */
    @PostMapping(
            value = "/non-consumable/{productAttributeId}/brochure",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<ApiResponse<CertificateUploadResponse>> uploadNonConsumableBrochure(
            @PathVariable String productAttributeId,
            @RequestPart(value = "brochureFile") MultipartFile brochureFile,
            Authentication authentication
    ) {
        CertificateUploadResponse response = productDocumentService.uploadNonConsumableBrochure(
                productAttributeId, brochureFile, getUsername(authentication)
        );

        return ResponseEntity.ok(new ApiResponse<>(
                HttpStatus.OK.toString(),
                "Non-consumable brochure uploaded successfully",
                response
        ));
    }

    // ─────────────────────────────────────────────────────────────
    // CONSUMABLE — CERTIFICATES
    // ─────────────────────────────────────────────────────────────

    /**
     * POST /product-documents/consumable/{productAttributeId}/certificates
     * <p>
     * multipart/form-data:
     * certificateFiles  → file1, file2, ...         (@RequestPart, repeated)
     * documentIds       → 4, 5, 6                   (@RequestParam, repeated, same order)
     * (productCertificateDocumentId from createProduct response)
     */
    @PostMapping(
            value = "/consumable/{productAttributeId}/certificates",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<ApiResponse<CertificateUploadResponse>> uploadConsumableCertificates(
            @PathVariable String productAttributeId,
            @RequestPart(value = "certificateFiles") List<MultipartFile> certificateFiles,
            @RequestParam(value = "documentIds") List<Long> documentIds,
            Authentication authentication
    ) {
        CertificateUploadResponse response = productDocumentService.uploadConsumableCertificates(
                productAttributeId, documentIds, certificateFiles, getUsername(authentication)
        );

        return ResponseEntity.ok(new ApiResponse<>(
                HttpStatus.OK.toString(),
                "Consumable certificates uploaded successfully",
                response
        ));
    }

    // ─────────────────────────────────────────────────────────────
    // CONSUMABLE — BROCHURE
    // ─────────────────────────────────────────────────────────────

    /**
     * POST /product-documents/consumable/{productAttributeId}/brochure
     * <p>
     * multipart/form-data:
     * brochureFile → single PDF file (@RequestPart)
     */
    @PostMapping(
            value = "/consumable/{productAttributeId}/brochure",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<ApiResponse<CertificateUploadResponse>> uploadConsumableBrochure(
            @PathVariable String productAttributeId,
            @RequestPart(value = "brochureFile") MultipartFile brochureFile,
            Authentication authentication
    ) {
        CertificateUploadResponse response = productDocumentService.uploadConsumableBrochure(
                productAttributeId, brochureFile, getUsername(authentication)
        );

        return ResponseEntity.ok(new ApiResponse<>(
                HttpStatus.OK.toString(),
                "Consumable brochure uploaded successfully",
                response
        ));
    }

    // ─────────────────────────────────────────────────────────────
    // Supplements / Nutraceuticals — CERTIFICATES
    // ─────────────────────────────────────────────────────────────

    /**
     * POST /product-documents/supplements/{productAttributeId}/certificates
     * <p>
     * multipart/form-data:
     * certificateFiles  → file1, file2, ...         (@RequestPart, repeated)
     * documentIds       → 4, 5, 6                   (@RequestParam, repeated, same order)
     * (productCertificateDocumentId from createProduct response)
     */
    @PostMapping(
            value = "/supplements/{productAttributeId}/certificates",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<ApiResponse<CertificateUploadResponse>> uploadSupplementsOrNutraceuticalsCertificates(
            @PathVariable String productAttributeId,
            @RequestPart(value = "certificateFiles") List<MultipartFile> certificateFiles,
            @RequestParam(value = "documentIds") List<Long> documentIds,
            Authentication authentication
    ) {
        CertificateUploadResponse response = productDocumentService.uploadSupplementsOrNutraceuticalsCertificates(
                productAttributeId, documentIds, certificateFiles, getUsername(authentication)
        );

        return ResponseEntity.ok(new ApiResponse<>(
                HttpStatus.OK.toString(),
                "Supplements Or Nutraceuticals certificates uploaded successfully",
                response
        ));
    }

    // ─────────────────────────────────────────────────────────────
    // Supplements / Nutraceuticals — BROCHURE
    // ─────────────────────────────────────────────────────────────

    /**
     * POST /product-documents/supplements/{productAttributeId}/brochure
     * <p>
     * multipart/form-data:
     * brochureFile → single PDF file (@RequestPart)
     */
    @PostMapping(
            value = "/supplements/{productAttributeId}/brochure",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<ApiResponse<CertificateUploadResponse>> uploadSupplementsOrNutraceuticalsBrochure(
            @PathVariable String productAttributeId,
            @RequestPart(value = "brochureFile") MultipartFile brochureFile,
            Authentication authentication
    ) {
        CertificateUploadResponse response = productDocumentService.uploadSupplementsOrNutraceuticalsBrochure(
                productAttributeId, brochureFile, getUsername(authentication)
        );

        return ResponseEntity.ok(new ApiResponse<>(
                HttpStatus.OK.toString(),
                "Supplements Or Nutraceuticals brochure uploaded successfully",
                response
        ));
    }

    // ─────────────────────────────────────────────────────────────
    // CONSUMABLE — CERTIFICATES
    // ─────────────────────────────────────────────────────────────

    /**
     * POST /product-documents/cosmetic/{productAttributeId}/certificates
     * <p>
     * multipart/form-data:
     * certificateFiles  → file1, file2, ...         (@RequestPart, repeated)
     * documentIds       → 4, 5, 6                   (@RequestParam, repeated, same order)
     * (productCertificateDocumentId from createProduct response)
     */
    @PostMapping(
            value = "/cosmetic/{productAttributeId}/certificates",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<ApiResponse<CertificateUploadResponse>> uploadCosmeticCertificates(
            @PathVariable String productAttributeId,
            @RequestPart(value = "certificateFiles") List<MultipartFile> certificateFiles,
            @RequestParam(value = "documentIds") List<Long> documentIds,
            Authentication authentication
    ) {
        CertificateUploadResponse response = productDocumentService.uploadCosmeticCertificates(
                productAttributeId, documentIds, certificateFiles, getUsername(authentication)
        );

        return ResponseEntity.ok(new ApiResponse<>(
                HttpStatus.OK.toString(),
                "Cosmetic certificates uploaded successfully",
                response
        ));
    }

    // ─────────────────────────────────────────────────────────────
    // Cosmetic — BROCHURE
    // ─────────────────────────────────────────────────────────────

    /**
     * POST /product-documents/cosmetic/{productAttributeId}/brochure
     * <p>
     * multipart/form-data:
     * brochureFile → single PDF file (@RequestPart)
     */
    @PostMapping(
            value = "/cosmetic/{productAttributeId}/brochure",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<ApiResponse<CertificateUploadResponse>> uploadCosmeticBrochure(
            @PathVariable String productAttributeId,
            @RequestPart(value = "brochureFile") MultipartFile brochureFile,
            Authentication authentication
    ) {
        CertificateUploadResponse response = productDocumentService.uploadCosmeticBrochure(
                productAttributeId, brochureFile, getUsername(authentication)
        );

        return ResponseEntity.ok(new ApiResponse<>(
                HttpStatus.OK.toString(),
                "Cosmetic brochure uploaded successfully",
                response
        ));
    }


    // ─────────────────────────────────────────────────────────────
    // HELPER
    // ─────────────────────────────────────────────────────────────

    private String getUsername(Authentication authentication) {
        UserDetailsImpl user = (UserDetailsImpl) authentication.getPrincipal();
        return user.getUsername();
    }
}