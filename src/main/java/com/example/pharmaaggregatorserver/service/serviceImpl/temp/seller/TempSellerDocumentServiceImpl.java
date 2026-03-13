package com.example.pharmaaggregatorserver.service.serviceImpl.temp.seller;

import com.example.pharmaaggregatorserver.dto.temp.seller.TempSellerDocumentUploadRequest;
import com.example.pharmaaggregatorserver.dto.temp.seller.TempSellerDocumentUploadResponse;
import com.example.pharmaaggregatorserver.entity.temp.seller.TempSeller;
import com.example.pharmaaggregatorserver.entity.temp.seller.TempSellerDocument;
import com.example.pharmaaggregatorserver.exception.NotFoundException;
import com.example.pharmaaggregatorserver.repository.temp.seller.TempSellerDocumentRepository;
import com.example.pharmaaggregatorserver.repository.temp.seller.TempSellerRepository;
import com.example.pharmaaggregatorserver.service.S3Service;
import com.example.pharmaaggregatorserver.service.temp.seller.TempSellerDocumentService;
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
public class TempSellerDocumentServiceImpl implements TempSellerDocumentService {

    // ── Timestamp format used in every S3 key ──────────────────────────────────
    // Produces: 20250101123045
    private static final DateTimeFormatter TS_FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final TempSellerRepository tempSellerRepository;
    private final TempSellerDocumentRepository tempSellerDocumentRepository;
    private final S3Service s3Service;

    // ── Public API ─────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public TempSellerDocumentUploadResponse uploadDocuments(Long tempSellerId,
                                                            TempSellerDocumentUploadRequest request) {

        TempSeller seller = tempSellerRepository.findById(tempSellerId)
                .orElseThrow(() -> new NotFoundException("TempSeller not found for id: " + tempSellerId));

        String reqId = seller.getTempSellerRequestId(); // e.g. REQ_001
        String now = LocalDateTime.now().format(TS_FORMATTER);

        String gstUrl = null;
        String bankUrl = null;
        List<TempSellerDocumentUploadResponse.LicenseUploadResult> licenseResults = new ArrayList<>();

        // ── 1. GST file ────────────────────────────────────────────────────────
        if (hasFile(request.getGstFile())) {
            String key = buildGstKey(reqId, now, request.getGstFile());
            gstUrl = s3Service.uploadFile(key, request.getGstFile());

            seller.setGstFileUrl(gstUrl);
            log.info("GST file uploaded → {}", gstUrl);
        }

        // ── 2. Bank document ───────────────────────────────────────────────────
        if (hasFile(request.getBankFile())) {
            String key = buildBankKey(reqId, now, request.getBankFile());
            bankUrl = s3Service.uploadFile(key, request.getBankFile());

            if (seller.getBankDetails() == null) {
                throw new NotFoundException("BankDetails record not found for sellerId: " + tempSellerId
                        + ". Create the seller record (step 1) before uploading files.");
            }
            seller.getBankDetails().setBankDocumentFileUrl(bankUrl);
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

                String key = buildLicenseKey(reqId, name, now, file);
                String docUrl = s3Service.uploadFile(key, file);

                // Persist URL on the existing TempSellerDocument row
                TempSellerDocument doc = tempSellerDocumentRepository.findById(docId)
                        .orElseThrow(() -> new NotFoundException("TempSellerDocument not found for id: " + docId));

                // Guard: document must belong to this seller
                if (!doc.getSeller().getTempSellerId().equals(tempSellerId)) {
                    throw new IllegalArgumentException(
                            "Document id=" + docId + " does not belong to sellerId=" + tempSellerId);
                }

                doc.setDocumentFileUrl(docUrl);
                tempSellerDocumentRepository.save(doc);

                licenseResults.add(TempSellerDocumentUploadResponse.LicenseUploadResult.builder()
                        .documentId(docId)
                        .licenseName(name)
                        .documentFileUrl(docUrl)
                        .build());

                log.info("License '{}' uploaded → {}", name, docUrl);
            }
        }

        // Cascade save covers gstFileUrl + bankDetails URL
        tempSellerRepository.save(seller);

        return TempSellerDocumentUploadResponse.builder()
                .tempSellerId(seller.getTempSellerId())
                .tempSellerRequestId(reqId)
                .gstFileUrl(gstUrl)
                .bankDocumentFileUrl(bankUrl)
                .licenseResults(licenseResults)
                .build();
    }

    // ── S3 key builders ────────────────────────────────────────────────────────

    /**
     * tempsellers/{REQ_ID}/gst/GST_IMAGE_{timestamp}.{ext}
     * e.g. tempsellers/REQ_001/gst/GST_IMAGE_20250101123045.jpg
     */
    private String buildGstKey(String reqId, String timestamp, MultipartFile file) {
        return String.format("tempsellers/%s/gst/GST_IMAGE_%s.%s",
                reqId, timestamp, extension(file));
    }

    /**
     * tempsellers/{REQ_ID}/bankdocument/BANK_DETAILS_{timestamp}.{ext}
     * e.g. tempsellers/REQ_001/bankdocument/BANK_DETAILS_20250101123045.jpg
     */
    private String buildBankKey(String reqId, String timestamp, MultipartFile file) {
        return String.format("tempsellers/%s/bankdocument/BANK_DETAILS_%s.%s",
                reqId, timestamp, extension(file));
    }

    /**
     * tempsellers/{REQ_ID}/licenses/{LICENSE_NAME}_{timestamp}.{ext}
     * e.g. tempsellers/REQ_001/licenses/Drug_20250101123045.png
     * <p>
     * Spaces in licenseName are replaced with underscores for a safe S3 key.
     */
    private String buildLicenseKey(String reqId, String licenseName, String timestamp, MultipartFile file) {
        String safeName = licenseName.trim().replaceAll("\\s+", "_");
        return String.format("tempsellers/%s/licenses/%s_%s.%s",
                reqId, safeName, timestamp, extension(file));
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    /**
     * Returns true when a MultipartFile is non-null and non-empty.
     */
    private boolean hasFile(MultipartFile file) {
        return file != null && !file.isEmpty();
    }

    /**
     * Extracts the file extension from the original filename.
     * Falls back to "bin" when the extension cannot be determined.
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
}