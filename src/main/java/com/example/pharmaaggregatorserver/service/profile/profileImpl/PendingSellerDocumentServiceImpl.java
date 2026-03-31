package com.example.pharmaaggregatorserver.service.profile.profileImpl;

import com.example.pharmaaggregatorserver.dto.seller.profile.PendingSellerDocumentUploadRequest;
import com.example.pharmaaggregatorserver.dto.seller.profile.PendingSellerDocumentUploadResponse;
import com.example.pharmaaggregatorserver.entity.seller.profile.PendingSeller;
import com.example.pharmaaggregatorserver.entity.seller.profile.PendingSellerDocument;
import com.example.pharmaaggregatorserver.exception.ResourceNotFoundException;
import com.example.pharmaaggregatorserver.repository.seller.profile.PendingSellerDocumentRepository;
import com.example.pharmaaggregatorserver.repository.seller.profile.PendingSellerRepository;
import com.example.pharmaaggregatorserver.service.S3Service;
import com.example.pharmaaggregatorserver.service.profile.PendingSellerDocumentService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class PendingSellerDocumentServiceImpl implements PendingSellerDocumentService {

    // Produces: 20250101123045  — same format as TempSellerDocumentServiceImpl
    private static final DateTimeFormatter TS_FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private static final String PENDING = "PENDING";

    private final PendingSellerRepository pendingSellerRepository;
    private final PendingSellerDocumentRepository pendingSellerDocumentRepository;
    private final S3Service s3Service;

    // ── Public API ─────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public PendingSellerDocumentUploadResponse uploadDocuments(Long pendingSellerId,
                                                               PendingSellerDocumentUploadRequest request) {

        PendingSeller pendingSeller = pendingSellerRepository.findById(pendingSellerId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "PendingSeller not found for id: " + pendingSellerId));

        String sellerId = pendingSeller.getSellerId(); // original seller ID used in S3 path
        String now = LocalDateTime.now().format(TS_FORMATTER);

        String gstUrl = null;
        String bankUrl = null;
        List<PendingSellerDocumentUploadResponse.LicenseUploadResult> licenseResults = new ArrayList<>();

        // ── 1. GST file ────────────────────────────────────────────────────────
        if (hasFile(request.getGstFile())) {
            deleteIfRealUrl(pendingSeller.getGstFileUrl());

            String key = buildGstKey(sellerId, now, request.getGstFile());
            gstUrl = s3Service.uploadFile(key, request.getGstFile());

            pendingSeller.setGstFileUrl(gstUrl);
            log.info("GST file uploaded → {}", gstUrl);
        }

        // ── 2. Bank document ───────────────────────────────────────────────────
        if (hasFile(request.getBankFile())) {
            deleteIfRealUrl(pendingSeller.getBankDocumentFileUrl());

            String key = buildBankKey(sellerId, now, request.getBankFile());
            bankUrl = s3Service.uploadFile(key, request.getBankFile());

            pendingSeller.setBankDocumentFileUrl(bankUrl);
            log.info("Bank document uploaded → {}", bankUrl);
        }

        // ── 3. License / document files ────────────────────────────────────────
        List<MultipartFile> licenseFiles = request.getLicenseFiles();
        List<String> licenseNames = request.getLicenseNames();
        List<Long> documentIds = request.getDocumentIds();

        if (licenseFiles != null && !licenseFiles.isEmpty()) {

            validateLicenseLists(licenseFiles, licenseNames, documentIds);

            for (int i = 0; i < licenseFiles.size(); i++) {
                MultipartFile file = licenseFiles.get(i);
                String name = licenseNames.get(i);   // e.g. "Drug"
                Long docId = documentIds.get(i);

                if (!hasFile(file)) continue;

                PendingSellerDocument doc = pendingSellerDocumentRepository.findById(docId)
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "PendingSellerDocument not found for id: " + docId));

                // Guard: document must belong to this pending seller
                if (!doc.getPendingSeller().getPendingSellerId().equals(pendingSellerId)) {
                    throw new IllegalArgumentException(
                            "Document id=" + docId + " does not belong to pendingSellerId=" + pendingSellerId);
                }

                deleteIfRealUrl(doc.getDocumentFileUrl());

                String key = buildLicenseKey(sellerId, name, now, file);
                String docUrl = s3Service.uploadFile(key, file);

                doc.setDocumentFileUrl(docUrl);
                pendingSellerDocumentRepository.save(doc);

                licenseResults.add(PendingSellerDocumentUploadResponse.LicenseUploadResult.builder()
                        .documentId(docId)
                        .licenseName(name)
                        .documentFileUrl(docUrl)
                        .build());

                log.info("License '{}' uploaded → {}", name, docUrl);
            }
        }

        // Cascade save covers gstFileUrl + bankDocumentFileUrl
        pendingSellerRepository.save(pendingSeller);

        return PendingSellerDocumentUploadResponse.builder()
                .pendingSellerId(pendingSellerId)
                .gstFileUrl(gstUrl)
                .bankDocumentFileUrl(bankUrl)
                .licenseResults(licenseResults)
                .build();
    }

    // ── S3 key builders ────────────────────────────────────────────────────────

    /**
     * pendingsellers/{sellerId}/gst/GST_IMAGE_{timestamp}.{ext}
     * e.g. pendingsellers/SELL_001/gst/GST_IMAGE_20250101123045.jpg
     */
    private String buildGstKey(String sellerId, String timestamp, MultipartFile file) {
        return String.format("pendingsellers/%s/gst/GST_IMAGE_%s.%s",
                sellerId, timestamp, extension(file));
    }

    /**
     * pendingsellers/{sellerId}/bankdocument/BANK_DETAILS_{timestamp}.{ext}
     * e.g. pendingsellers/SELL_001/bankdocument/BANK_DETAILS_20250101123045.pdf
     */
    private String buildBankKey(String sellerId, String timestamp, MultipartFile file) {
        return String.format("pendingsellers/%s/bankdocument/BANK_DETAILS_%s.%s",
                sellerId, timestamp, extension(file));
    }

    /**
     * pendingsellers/{sellerId}/licenses/{LICENSE_NAME}_{timestamp}.{ext}
     * e.g. pendingsellers/SELL_001/licenses/Drug_20250101123045.png
     * <p>
     * Spaces / special chars in licenseName are replaced with underscores for a safe S3 key.
     */
    private String buildLicenseKey(String sellerId, String licenseName, String timestamp, MultipartFile file) {
        String safeName = licenseName.trim()
                .replaceAll("[\\s/\\\\:*?\"<>|#]+", "_")
                .replaceAll("_+", "_")
                .replaceAll("^_|_$", "");
        return String.format("pendingsellers/%s/licenses/%s_%s.%s",
                sellerId, safeName, timestamp, extension(file));
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    /**
     * Returns true when a MultipartFile is non-null and non-empty.
     */
    private boolean hasFile(MultipartFile file) {
        return file != null && !file.isEmpty();
    }

    /**
     * Extracts the file extension from the original filename. Falls back to "bin".
     */
    private String extension(MultipartFile file) {
        String original = Objects.requireNonNullElse(file.getOriginalFilename(), "");
        int dot = original.lastIndexOf('.');
        return (dot >= 0 && dot < original.length() - 1)
                ? original.substring(dot + 1).toLowerCase()
                : "bin";
    }

    /**
     * Ensures all three license lists are present and have the same size.
     */
    private void validateLicenseLists(List<MultipartFile> files,
                                      List<String> names,
                                      List<Long> ids) {
        if (names == null || names.size() != files.size()) {
            throw new IllegalArgumentException(
                    "licenseNames must be provided and must match the number of licenseFiles (" + files.size() + ").");
        }
        if (ids == null || ids.size() != files.size()) {
            throw new IllegalArgumentException(
                    "documentIds must be provided and must match the number of licenseFiles (" + files.size() + ").");
        }
    }

    /**
     * Deletes a previously uploaded pending S3 file before replacing it with a new one.
     * Skips deletion if the URL is null, blank, "PENDING", or belongs to the live sellers/ area.
     */
    private void deleteIfRealUrl(String url) {
        if (url == null || url.isBlank() || PENDING.equalsIgnoreCase(url.trim())) return;

        // ✅ Never delete files from the live sellers/ area — those belong to the approved seller
        if (!url.contains("/pendingsellers/")) {
            log.debug("Skipping deletion — not a pending S3 URL: {}", url);
            return;
        }

        try {
            s3Service.deleteFile(s3Service.extractKeyFromUrl(url));
        } catch (Exception e) {
            log.warn("Could not delete old S3 file (url={}): {}", url, e.getMessage());
        }
    }
}