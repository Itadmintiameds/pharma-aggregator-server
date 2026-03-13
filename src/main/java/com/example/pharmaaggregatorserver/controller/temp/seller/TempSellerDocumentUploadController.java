package com.example.pharmaaggregatorserver.controller.temp.seller;

// ── Add these imports to TempSellerController (or keep as a separate controller) ──

import com.example.pharmaaggregatorserver.dto.temp.seller.TempSellerDocumentUploadRequest;
import com.example.pharmaaggregatorserver.dto.temp.seller.TempSellerDocumentUploadResponse;
import com.example.pharmaaggregatorserver.response.ApiResponse;
import com.example.pharmaaggregatorserver.service.temp.seller.TempSellerDocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Endpoint: POST /temp-sellers/{tempSellerId}/documents/upload
 * <p>
 * Called immediately after the existing POST /temp-sellers (step 1) to upload
 * GST, bank, and license files for the newly created TempSeller.
 * <p>
 * ─────────────────────────────────────────────────────────────────────────────
 * You can either:
 * (a) Merge the single method below into your existing TempSellerController, OR
 * (b) Keep this as a dedicated controller (as shown here).
 * ─────────────────────────────────────────────────────────────────────────────
 */
@RestController
@RequestMapping("/temp-sellers")
@RequiredArgsConstructor
public class TempSellerDocumentUploadController {

    private final TempSellerDocumentService documentService;

    /**
     * POST /temp-sellers/{tempSellerId}/documents/upload
     * <p>
     * Multipart form fields:
     * ┌──────────────┬────────────────────────────────────────────────────────┐
     * │ Field        │ Description                                            │
     * ├──────────────┼────────────────────────────────────────────────────────┤
     * │ gstFile      │ Single GST certificate file (image or PDF)            │
     * │ bankFile     │ Single bank document file (image or PDF)              │
     * │ licenseFiles │ One file per license document (multiple allowed)      │
     * │ licenseNames │ Name for each license, same order as licenseFiles     │
     * │              │ e.g. "Drug", "Wholesale"                               │
     * │ documentIds  │ DB ID of the TempSellerDocument row for each license  │
     * └──────────────┴────────────────────────────────────────────────────────┘
     * <p>
     * All fields are optional — send only the files that are available.
     * At least one file should be present for a meaningful call.
     * <p>
     * S3 key patterns:
     * GST     → tempsellers/{REQ_ID}/gst/GST_IMAGE_{timestamp}.{ext}
     * Bank    → tempsellers/{REQ_ID}/bankdocument/BANK_DETAILS_{timestamp}.{ext}
     * License → tempsellers/{REQ_ID}/licenses/{LICENSE_NAME}_{timestamp}.{ext}
     */
    @PostMapping(
            value = "/{tempSellerId}/documents/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<ApiResponse<TempSellerDocumentUploadResponse>> uploadDocuments(
            @PathVariable Long tempSellerId,

            // GST certificate
            @RequestPart(value = "gstFile", required = false) MultipartFile gstFile,

            // Bank document
            @RequestPart(value = "bankFile", required = false) MultipartFile bankFile,

            // License files + metadata (parallel lists)
            @RequestPart(value = "licenseFiles", required = false) List<MultipartFile> licenseFiles,
            @RequestPart(value = "licenseNames", required = false) List<String> licenseNames,
            @RequestPart(value = "documentIds", required = false) List<Long> documentIds
    ) {
        // Assemble the request object
        TempSellerDocumentUploadRequest request = new TempSellerDocumentUploadRequest();
        request.setGstFile(gstFile);
        request.setBankFile(bankFile);
        request.setLicenseFiles(licenseFiles);
        request.setLicenseNames(licenseNames);
        request.setDocumentIds(documentIds);

        TempSellerDocumentUploadResponse response =
                documentService.uploadDocuments(tempSellerId, request);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ApiResponse<>(
                        HttpStatus.OK.toString(),
                        "Documents uploaded successfully",
                        response
                ));
    }
}