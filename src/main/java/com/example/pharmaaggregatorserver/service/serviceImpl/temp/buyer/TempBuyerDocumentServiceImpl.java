package com.example.pharmaaggregatorserver.service.serviceImpl.temp.buyer;

import com.example.pharmaaggregatorserver.dto.temp.buyer.TempBuyerDocumentUploadRequest;
import com.example.pharmaaggregatorserver.dto.temp.buyer.TempBuyerDocumentUploadResponse;
import com.example.pharmaaggregatorserver.entity.temp.buyer.TempBuyer;
import com.example.pharmaaggregatorserver.entity.temp.buyer.TempBuyerDocument;
import com.example.pharmaaggregatorserver.exception.NotFoundException;
import com.example.pharmaaggregatorserver.repository.temp.buyer.TempBuyerDocumentRepository;
import com.example.pharmaaggregatorserver.repository.temp.buyer.TempBuyerRepository;
import com.example.pharmaaggregatorserver.service.S3Service;
import com.example.pharmaaggregatorserver.service.temp.buyer.TempBuyerDocumentService;
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

/**
 * Handles S3 uploads for TempBuyer documents. Mirrors
 * service.serviceImpl.temp.seller.TempSellerDocumentServiceImpl.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TempBuyerDocumentServiceImpl implements TempBuyerDocumentService {

    private static final DateTimeFormatter TS_FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private static final String PENDING = "PENDING";

    private final TempBuyerRepository tempBuyerRepository;
    private final TempBuyerDocumentRepository tempBuyerDocumentRepository;
    private final S3Service s3Service;

    @Override
    @Transactional
    public TempBuyerDocumentUploadResponse uploadDocuments(Long tempBuyerId,
                                                            TempBuyerDocumentUploadRequest request) {

        TempBuyer buyer = tempBuyerRepository.findById(tempBuyerId)
                .orElseThrow(() -> new NotFoundException("TempBuyer not found for id: " + tempBuyerId));

        String reqId = buyer.getTempBuyerRequestId();
        String now = LocalDateTime.now().format(TS_FORMATTER);

        String orgLogoUrl = null;
        String orgLogoFileName = null;
        String gstUrl = null;
        String gstFileName = null;
        String panUrl = null;
        String panFileName = null;
        List<TempBuyerDocumentUploadResponse.LicenseUploadResult> licenseResults = new ArrayList<>();

        // ── Org logo ─────────────────────────────────────────────────────
        if (hasFile(request.getOrgLogo())) {
            deleteIfRealUrl(buyer.getOrgLogoUrl());
            String key = buildOrgLogoKey(reqId, now, request.getOrgLogo());
            orgLogoUrl = s3Service.uploadFile(key, request.getOrgLogo());
            orgLogoFileName = request.getOrgLogo().getOriginalFilename();
            buyer.setOrgLogoUrl(orgLogoUrl);
            buyer.setOrgLogoFileName(orgLogoFileName);
            log.info("Org logo uploaded → {}", orgLogoUrl);
        }

        // ── GST file ─────────────────────────────────────────────────────
        if (hasFile(request.getGstFile())) {
            deleteIfRealUrl(buyer.getGstFileUrl());
            String key = buildGstKey(reqId, now, request.getGstFile());
            gstUrl = s3Service.uploadFile(key, request.getGstFile());
            gstFileName = request.getGstFile().getOriginalFilename();
            buyer.setGstFileUrl(gstUrl);
            buyer.setGstFileName(gstFileName);
            buyer.setGstVerified(false);
            log.info("GST file uploaded → {}", gstUrl);
        }

        // ── PAN file ─────────────────────────────────────────────────────
        if (hasFile(request.getPanFile())) {
            deleteIfRealUrl(buyer.getPanFileUrl());
            String key = buildPanKey(reqId, now, request.getPanFile());
            panUrl = s3Service.uploadFile(key, request.getPanFile());
            panFileName = request.getPanFile().getOriginalFilename();
            buyer.setPanFileUrl(panUrl);
            buyer.setPanFileName(panFileName);
            buyer.setPanVerified(false);
            log.info("PAN file uploaded → {}", panUrl);
        }

        // ── License / document files ────────────────────────────────────
        List<MultipartFile> licenseFiles = request.getLicenseFiles();
        List<String> licenseNames = request.getLicenseNames();
        List<Long> documentIds = request.getDocumentIds();

        if (licenseFiles != null && !licenseFiles.isEmpty()) {
            validateLicenseLists(licenseFiles, licenseNames, documentIds);

            for (int i = 0; i < licenseFiles.size(); i++) {
                MultipartFile file = licenseFiles.get(i);
                String name = licenseNames.get(i);
                Long docId = documentIds.get(i);

                if (!hasFile(file)) continue;

                TempBuyerDocument doc = tempBuyerDocumentRepository.findById(docId)
                        .orElseThrow(() -> new NotFoundException("TempBuyerDocument not found for id: " + docId));

                if (!doc.getBuyer().getTempBuyerId().equals(tempBuyerId)) {
                    throw new IllegalArgumentException(
                            "Document id=" + docId + " does not belong to buyerId=" + tempBuyerId);
                }

                deleteIfRealUrl(doc.getDocumentFileUrl());

                String key = buildLicenseKey(reqId, name, now, file);
                String docUrl = s3Service.uploadFile(key, file);
                String docFileName = file.getOriginalFilename();

                doc.setDocumentFileUrl(docUrl);
                doc.setDocumentFileName(docFileName);
                doc.setDocumentVerified(false);
                tempBuyerDocumentRepository.save(doc);

                licenseResults.add(TempBuyerDocumentUploadResponse.LicenseUploadResult.builder()
                        .documentId(docId)
                        .documentFileUrl(docUrl)
                        .documentFileName(docFileName)
                        .build());

                log.info("License '{}' uploaded → {}", name, docUrl);
            }
        }

        tempBuyerRepository.save(buyer);

        return TempBuyerDocumentUploadResponse.builder()
                .tempBuyerId(buyer.getTempBuyerId())
                .tempBuyerRequestId(reqId)
                .orgLogoUrl(orgLogoUrl)
                .orgLogoFileName(orgLogoFileName)
                .gstFileUrl(gstUrl)
                .gstFileName(gstFileName)
                .panFileUrl(panUrl)
                .panFileName(panFileName)
                .licenseResults(licenseResults)
                .build();
    }

    @Override
    @Transactional
    public void deleteGstFile(Long tempBuyerId) {
        TempBuyer buyer = tempBuyerRepository.findById(tempBuyerId)
                .orElseThrow(() -> new NotFoundException("TempBuyer not found for id: " + tempBuyerId));

        deleteIfRealUrl(buyer.getGstFileUrl());
        buyer.setGstFileUrl(null);
        buyer.setGstFileName(null);
        buyer.setGstVerified(false);
        tempBuyerRepository.save(buyer);
    }

    @Override
    @Transactional
    public void deletePanFile(Long tempBuyerId) {
        TempBuyer buyer = tempBuyerRepository.findById(tempBuyerId)
                .orElseThrow(() -> new NotFoundException("TempBuyer not found for id: " + tempBuyerId));

        deleteIfRealUrl(buyer.getPanFileUrl());
        buyer.setPanFileUrl(null);
        buyer.setPanFileName(null);
        buyer.setPanVerified(false);
        tempBuyerRepository.save(buyer);
    }

    @Override
    @Transactional
    public void deleteOrgLogo(Long tempBuyerId) {
        TempBuyer buyer = tempBuyerRepository.findById(tempBuyerId)
                .orElseThrow(() -> new NotFoundException("TempBuyer not found for id: " + tempBuyerId));

        deleteIfRealUrl(buyer.getOrgLogoUrl());
        buyer.setOrgLogoUrl(null);
        buyer.setOrgLogoFileName(null);
        tempBuyerRepository.save(buyer);
    }

    @Override
    @Transactional
    public void deleteDocumentFile(Long tempBuyerId, Long documentId) {
        TempBuyerDocument doc = tempBuyerDocumentRepository.findById(documentId)
                .orElseThrow(() -> new NotFoundException("TempBuyerDocument not found for id: " + documentId));

        if (!doc.getBuyer().getTempBuyerId().equals(tempBuyerId)) {
            throw new IllegalArgumentException(
                    "Document id=" + documentId + " does not belong to buyerId=" + tempBuyerId);
        }

        deleteIfRealUrl(doc.getDocumentFileUrl());
        doc.setDocumentFileUrl(PENDING);
        doc.setDocumentFileName(null);
        doc.setDocumentVerified(false);
        tempBuyerDocumentRepository.save(doc);
    }

    // ── S3 key builders ──────────────────────────────────────────────────

    private String buildOrgLogoKey(String reqId, String timestamp, MultipartFile file) {
        return String.format("tempbuyers/%s/orglogo/ORG_LOGO_%s.%s", reqId, timestamp, extension(file));
    }

    private String buildGstKey(String reqId, String timestamp, MultipartFile file) {
        return String.format("tempbuyers/%s/gst/GST_IMAGE_%s.%s", reqId, timestamp, extension(file));
    }

    private String buildPanKey(String reqId, String timestamp, MultipartFile file) {
        return String.format("tempbuyers/%s/pan/PAN_IMAGE_%s.%s", reqId, timestamp, extension(file));
    }

    private String buildLicenseKey(String reqId, String docTypeName, String timestamp, MultipartFile file) {
        String safeName = docTypeName.trim()
                .replaceAll("[\\s/\\\\:*?\"<>|#]+", "_")
                .replaceAll("_+", "_")
                .replaceAll("^_|_$", "");
        return String.format("tempbuyers/%s/licenses/%s_%s.%s", reqId, safeName, timestamp, extension(file));
    }

    // ── Helpers ──────────────────────────────────────────────────────────

    private boolean hasFile(MultipartFile file) {
        return file != null && !file.isEmpty();
    }

    private String extension(MultipartFile file) {
        String original = Objects.requireNonNullElse(file.getOriginalFilename(), "");
        int dot = original.lastIndexOf('.');
        return (dot >= 0 && dot < original.length() - 1)
                ? original.substring(dot + 1).toLowerCase()
                : "bin";
    }

    private void validateLicenseLists(List<MultipartFile> files, List<String> names, List<Long> ids) {
        if (names == null || names.size() != files.size()) {
            throw new IllegalArgumentException(
                    "licenseNames must be provided and must match the number of licenseFiles (" + files.size() + ").");
        }
        if (ids == null || ids.size() != files.size()) {
            throw new IllegalArgumentException(
                    "documentIds must be provided and must match the number of licenseFiles (" + files.size() + ").");
        }
    }

    private void deleteIfRealUrl(String url) {
        if (url == null || url.isBlank() || PENDING.equalsIgnoreCase(url.trim())) return;
        try {
            s3Service.deleteFile(s3Service.extractKeyFromUrl(url));
        } catch (Exception e) {
            log.warn("Could not delete old S3 file (url={}): {}", url, e.getMessage());
        }
    }
}
