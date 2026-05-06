package com.example.pharmaaggregatorserver.service.product.productImpl;

import com.example.pharmaaggregatorserver.dto.product.CertificateDocumentDto;
import com.example.pharmaaggregatorserver.dto.product.CertificateUploadResponse;
import com.example.pharmaaggregatorserver.entity.product.ProductAttributeConsumableMedical;
import com.example.pharmaaggregatorserver.entity.product.ProductAttributeCosmeticandPersonalCare;
import com.example.pharmaaggregatorserver.entity.product.ProductAttributeNonConsumableMedical;
import com.example.pharmaaggregatorserver.entity.product.ProductAttributeSupplementsOrNutraceuticals;
import com.example.pharmaaggregatorserver.entity.product.ProductCertificateDocument;
import com.example.pharmaaggregatorserver.repository.product.ProductAttributeConsumableMedicalRepository;
import com.example.pharmaaggregatorserver.repository.product.ProductAttributeCosmeticAndPersonalUseRepository;
import com.example.pharmaaggregatorserver.repository.product.ProductAttributeNonConsumableMedicalRepository;
import com.example.pharmaaggregatorserver.repository.product.ProductAttributeSupplementsOrNutraceuticalsRepository;
import com.example.pharmaaggregatorserver.repository.product.ProductCertificateDocumentRepository;
import com.example.pharmaaggregatorserver.service.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductDocumentService {

    private static final DateTimeFormatter TS_FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final S3Service s3Service;
    private final ProductCertificateDocumentRepository certificateDocumentRepository;
    private final ProductAttributeNonConsumableMedicalRepository nonConsumableRepository;
    private final ProductAttributeConsumableMedicalRepository consumableRepository;
    private final ProductAttributeSupplementsOrNutraceuticalsRepository supplementsOrNutraceuticalsRepository;
    private final ProductAttributeCosmeticAndPersonalUseRepository cosmeticRepository;



    // ─────────────────────────────────────────────────────────────
    // NON-CONSUMABLE — CERTIFICATES
    // ─────────────────────────────────────────────────────────────

    /**
     * Replaces the certificateUrl on existing ProductCertificateDocument rows
     * (already created during createProduct) for a non-consumable attribute.
     * <p>
     * documentIds    → productCertificateDocumentId values from the createProduct response
     * certificateFiles → actual PDF/image files, same order as documentIds
     */
    @Transactional
    public CertificateUploadResponse uploadNonConsumableCertificates(
            String productAttributeId,
            List<Long> documentIds,
            List<MultipartFile> certificateFiles,
            String uploadedBy
    ) {
        validateParallelLists(certificateFiles, documentIds);

        // Verify the attribute exists and belongs to a real record
        ProductAttributeNonConsumableMedical attribute = nonConsumableRepository
                .findById(productAttributeId)
                .orElseThrow(() -> new RuntimeException(
                        "Non-consumable attribute not found: " + productAttributeId));

        String now = LocalDateTime.now().format(TS_FORMATTER);
        List<CertificateDocumentDto> uploaded = new ArrayList<>();

        for (int i = 0; i < certificateFiles.size(); i++) {

            MultipartFile file = certificateFiles.get(i);
            Long docId = documentIds.get(i);

            if (!hasFile(file)) continue;

            // Fetch the existing certificate document row created during createProduct
            ProductCertificateDocument doc = certificateDocumentRepository
                    .findById(docId)
                    .orElseThrow(() -> new RuntimeException(
                            "Certificate document not found: " + docId));

            // Guard: document must belong to this attribute
            if (doc.getNonConsumableMedical() == null ||
                    !doc.getNonConsumableMedical().getProductAttributeId().equals(productAttributeId)) {
                throw new IllegalArgumentException(
                        "Certificate document id=" + docId
                                + " does not belong to productAttributeId=" + productAttributeId);
            }

            // Delete old S3 file if it's a real URL (not a placeholder like /certs/cdsco.pdf)
            deleteIfRealUrl(doc.getCertificateUrl());

            String productId = attribute.getProductDetails().getProductId();

            String key = buildCertKey("non-consumable", productId, productAttributeId,
                    doc.getCertification().getCertificationName(), now, file);

            String url = s3Service.uploadFile(key, file);
            log.info("Non-consumable certificate '{}' uploaded → {}",
                    doc.getCertification().getCertificationName(), url);

            // Update the existing row — no new row inserted
            doc.setCertificateUrl(url);
            doc.setModifiedBy(uploadedBy);
            certificateDocumentRepository.save(doc);

            uploaded.add(CertificateDocumentDto.builder()
                    .productCertificateDocumentId(docId)
                    .certificationId(doc.getCertification().getCertificationId())
                    .certificationName(doc.getCertification().getCertificationName())
                    .certificateUrl(url)
                    .build());
        }

        return CertificateUploadResponse.builder()
                .productAttributeId(productAttributeId)
                .attributeType("non-consumable")
                .uploadedCertificates(uploaded)
                .brochureUrl(null)
                .build();
    }

    // ─────────────────────────────────────────────────────────────
    // NON-CONSUMABLE — BROCHURE
    // ─────────────────────────────────────────────────────────────

    /**
     * Replaces the brochurePath on an existing non-consumable attribute
     * (already set as a placeholder path during createProduct).
     */
    @Transactional
    public CertificateUploadResponse uploadNonConsumableBrochure(
            String productAttributeId,
            MultipartFile brochureFile,
            String uploadedBy
    ) {
        if (!hasFile(brochureFile)) {
            throw new RuntimeException("Brochure file is empty or missing");
        }

        ProductAttributeNonConsumableMedical attribute = nonConsumableRepository
                .findById(productAttributeId)
                .orElseThrow(() -> new RuntimeException(
                        "Non-consumable attribute not found: " + productAttributeId));

        // Delete old S3 file if it's a real URL (not a placeholder like /docs/accucheck-pro.pdf)
        deleteIfRealUrl(attribute.getBrochurePath());

        String productId = attribute.getProductDetails().getProductId();
        String now = LocalDateTime.now().format(TS_FORMATTER);
        String key = buildBrochureKey("non-consumable", productId, productAttributeId, now, brochureFile);
        String url = s3Service.uploadFile(key, brochureFile);
        log.info("Non-consumable brochure uploaded → {}", url);

        attribute.setBrochurePath(url);
        attribute.setModifiedBy(uploadedBy);
        nonConsumableRepository.save(attribute);

        return CertificateUploadResponse.builder()
                .productAttributeId(productAttributeId)
                .attributeType("non-consumable")
                .uploadedCertificates(List.of())
                .brochureUrl(url)
                .build();
    }

    // ─────────────────────────────────────────────────────────────
    // CONSUMABLE — CERTIFICATES
    // ─────────────────────────────────────────────────────────────

    /**
     * Replaces the certificateUrl on existing ProductCertificateDocument rows
     * for a consumable attribute.
     * <p>
     * documentIds    → productCertificateDocumentId values from the createProduct response
     * certificateFiles → actual files, same order as documentIds
     */
    @Transactional
    public CertificateUploadResponse uploadConsumableCertificates(
            String productAttributeId,
            List<Long> documentIds,
            List<MultipartFile> certificateFiles,
            String uploadedBy
    ) {
        validateParallelLists(certificateFiles, documentIds);

        ProductAttributeConsumableMedical attribute = consumableRepository
                .findById(productAttributeId)
                .orElseThrow(() -> new RuntimeException(
                        "Consumable attribute not found: " + productAttributeId));

        String now = LocalDateTime.now().format(TS_FORMATTER);
        List<CertificateDocumentDto> uploaded = new ArrayList<>();

        for (int i = 0; i < certificateFiles.size(); i++) {

            MultipartFile file = certificateFiles.get(i);
            Long docId = documentIds.get(i);

            if (!hasFile(file)) continue;

            ProductCertificateDocument doc = certificateDocumentRepository
                    .findById(docId)
                    .orElseThrow(() -> new RuntimeException(
                            "Certificate document not found: " + docId));

            // Guard: document must belong to this attribute
            if (doc.getConsumableMedical() == null ||
                    !doc.getConsumableMedical().getProductAttributeId().equals(productAttributeId)) {
                throw new IllegalArgumentException(
                        "Certificate document id=" + docId
                                + " does not belong to productAttributeId=" + productAttributeId);
            }

            deleteIfRealUrl(doc.getCertificateUrl());

            String productId = attribute.getProductDetails().getProductId();

            String key = buildCertKey("consumable", productId, productAttributeId,
                    doc.getCertification().getCertificationName(), now, file);

            String url = s3Service.uploadFile(key, file);
            log.info("Consumable certificate '{}' uploaded → {}",
                    doc.getCertification().getCertificationName(), url);

            doc.setCertificateUrl(url);
            doc.setModifiedBy(uploadedBy);
            certificateDocumentRepository.save(doc);

            uploaded.add(CertificateDocumentDto.builder()
                    .productCertificateDocumentId(docId)
                    .certificationId(doc.getCertification().getCertificationId())
                    .certificationName(doc.getCertification().getCertificationName())
                    .certificateUrl(url)
                    .build());
        }

        return CertificateUploadResponse.builder()
                .productAttributeId(productAttributeId)
                .attributeType("consumable")
                .uploadedCertificates(uploaded)
                .brochureUrl(null)
                .build();
    }

    // ─────────────────────────────────────────────────────────────
    // CONSUMABLE — BROCHURE
    // ─────────────────────────────────────────────────────────────

    /**
     * Replaces the brochurePath on an existing consumable attribute.
     */
    @Transactional
    public CertificateUploadResponse uploadConsumableBrochure(
            String productAttributeId,
            MultipartFile brochureFile,
            String uploadedBy
    ) {
        if (!hasFile(brochureFile)) {
            throw new RuntimeException("Brochure file is empty or missing");
        }

        ProductAttributeConsumableMedical attribute = consumableRepository
                .findById(productAttributeId)
                .orElseThrow(() -> new RuntimeException(
                        "Consumable attribute not found: " + productAttributeId));

        deleteIfRealUrl(attribute.getBrochurePath());

        String productId = attribute.getProductDetails().getProductId();
        String now = LocalDateTime.now().format(TS_FORMATTER);
        String key = buildBrochureKey("consumable", productId, productAttributeId, now, brochureFile);
        String url = s3Service.uploadFile(key, brochureFile);
        log.info("Consumable brochure uploaded → {}", url);

        attribute.setBrochurePath(url);
        attribute.setModifiedBy(uploadedBy);
        consumableRepository.save(attribute);

        return CertificateUploadResponse.builder()
                .productAttributeId(productAttributeId)
                .attributeType("consumable")
                .uploadedCertificates(List.of())
                .brochureUrl(url)
                .build();
    }

    public CertificateUploadResponse uploadSupplementsOrNutraceuticalsCertificates(
            String productAttributeId,
            List<Long> documentIds,
            List<MultipartFile> certificateFiles,
            String username) {

        validateParallelLists(certificateFiles, documentIds);

        ProductAttributeSupplementsOrNutraceuticals attribute = supplementsOrNutraceuticalsRepository
                .findById(productAttributeId)
                .orElseThrow(() -> new RuntimeException(
                        "Supplements/Nutraceuticals attribute not found: " + productAttributeId));

        String now = LocalDateTime.now().format(TS_FORMATTER);
        List<CertificateDocumentDto> uploaded = new ArrayList<>();

        for (int i = 0; i < certificateFiles.size(); i++) {

            MultipartFile file = certificateFiles.get(i);
            Long docId = documentIds.get(i);

            if (!hasFile(file)) continue;

            ProductCertificateDocument doc = certificateDocumentRepository
                    .findById(docId)
                    .orElseThrow(() -> new RuntimeException(
                            "Certificate document not found: " + docId));

            // Guard: document must belong to this attribute
            if (doc.getSupplementsOrNutraceuticals() == null ||
                    !doc.getSupplementsOrNutraceuticals().getProductAttributeId().equals(productAttributeId)) {
                throw new IllegalArgumentException(
                        "Certificate document id=" + docId
                                + " does not belong to productAttributeId=" + productAttributeId);
            }

            deleteIfRealUrl(doc.getCertificateUrl());

            String productId = attribute.getProductDetails().getProductId();

            String key = buildCertKey("supplements-or-nutraceuticals", productId, productAttributeId,
                    doc.getCertification().getCertificationName(), now, file);

            String url = s3Service.uploadFile(key, file);
            log.info("Supplements/Nutraceuticals certificate '{}' uploaded → {}",
                    doc.getCertification().getCertificationName(), url);

            doc.setCertificateUrl(url);
            doc.setModifiedBy(username);
            certificateDocumentRepository.save(doc);

            uploaded.add(CertificateDocumentDto.builder()
                    .productCertificateDocumentId(docId)
                    .certificationId(doc.getCertification().getCertificationId())
                    .certificationName(doc.getCertification().getCertificationName())
                    .certificateUrl(url)
                    .build());
        }

        return CertificateUploadResponse.builder()
                .productAttributeId(productAttributeId)
                .attributeType("supplements-or-nutraceuticals")
                .uploadedCertificates(uploaded)
                .brochureUrl(null)
                .build();
    }

    public CertificateUploadResponse uploadSupplementsOrNutraceuticalsBrochure(
            String productAttributeId,
            MultipartFile brochureFile,
            String username) {

        if (!hasFile(brochureFile)) {
            throw new RuntimeException("Brochure file is empty or missing");
        }

        ProductAttributeSupplementsOrNutraceuticals attribute = supplementsOrNutraceuticalsRepository
                .findById(productAttributeId)
                .orElseThrow(() -> new RuntimeException(
                        "Supplements/Nutraceuticals attribute not found: " + productAttributeId));

        deleteIfRealUrl(attribute.getBrochurePath());

        String productId = attribute.getProductDetails().getProductId();
        String now = LocalDateTime.now().format(TS_FORMATTER);
        String key = buildBrochureKey("supplements-or-nutraceuticals", productId, productAttributeId, now, brochureFile);
        String url = s3Service.uploadFile(key, brochureFile);
        log.info("Supplements/Nutraceuticals brochure uploaded → {}", url);

        attribute.setBrochurePath(url);
        attribute.setModifiedBy(username);
        supplementsOrNutraceuticalsRepository.save(attribute);

        return CertificateUploadResponse.builder()
                .productAttributeId(productAttributeId)
                .attributeType("supplements-or-nutraceuticals")
                .uploadedCertificates(List.of())
                .brochureUrl(url)
                .build();
    }

    // ─────────────────────────────────────────────────────────────
    // COSMETIC — CERTIFICATES
    // ─────────────────────────────────────────────────────────────

    /**
     * Replaces the certificateUrl on existing ProductCertificateDocument rows
     * for a consumable attribute.
     * <p>
     * documentIds    → productCertificateDocumentId values from the createProduct response
     * certificateFiles → actual files, same order as documentIds
     */
    @Transactional
    public CertificateUploadResponse uploadCosmeticCertificates(
            String productAttributeId,
            List<Long> documentIds,
            List<MultipartFile> certificateFiles,
            String uploadedBy
    ) {
        validateParallelLists(certificateFiles, documentIds);

        ProductAttributeCosmeticandPersonalCare attribute = cosmeticRepository
                .findById(productAttributeId)
                .orElseThrow(() -> new RuntimeException(
                        "Cosmetic attribute not found: " + productAttributeId));

        String now = LocalDateTime.now().format(TS_FORMATTER);
        List<CertificateDocumentDto> uploaded = new ArrayList<>();

        for (int i = 0; i < certificateFiles.size(); i++) {

            MultipartFile file = certificateFiles.get(i);
            Long docId = documentIds.get(i);

            if (!hasFile(file)) continue;

            ProductCertificateDocument doc = certificateDocumentRepository
                    .findById(docId)
                    .orElseThrow(() -> new RuntimeException(
                            "Certificate document not found: " + docId));

            // Guard: document must belong to this attribute
            if (doc.getCosmeticAndPersonalUse() == null ||
                    !doc.getCosmeticAndPersonalUse().getProductAttributeId().equals(productAttributeId)) {
                throw new IllegalArgumentException(
                        "Certificate document id=" + docId
                                + " does not belong to productAttributeId=" + productAttributeId);
            }

            deleteIfRealUrl(doc.getCertificateUrl());

            String productId = attribute.getProductDetails().getProductId();

            String key = buildCertKey("cosmetic", productId, productAttributeId,
                    doc.getCertification().getCertificationName(), now, file);

            String url = s3Service.uploadFile(key, file);
            log.info("Cosmetic certificate '{}' uploaded → {}",
                    doc.getCertification().getCertificationName(), url);

            doc.setCertificateUrl(url);
            doc.setModifiedBy(uploadedBy);
            certificateDocumentRepository.save(doc);

            uploaded.add(CertificateDocumentDto.builder()
                    .productCertificateDocumentId(docId)
                    .certificationId(doc.getCertification().getCertificationId())
                    .certificationName(doc.getCertification().getCertificationName())
                    .certificateUrl(url)
                    .build());
        }

        return CertificateUploadResponse.builder()
                .productAttributeId(productAttributeId)
                .attributeType("cosmetic")
                .uploadedCertificates(uploaded)
                .brochureUrl(null)
                .build();
    }

    // ─────────────────────────────────────────────────────────────
    // COSMETIC — BROCHURE
    // ─────────────────────────────────────────────────────────────

    /**
     * Replaces the brochurePath on an existing consumable attribute.
     */
    @Transactional
    public CertificateUploadResponse uploadCosmeticBrochure(
            String productAttributeId,
            MultipartFile brochureFile,
            String uploadedBy
    ) {
        if (!hasFile(brochureFile)) {
            throw new RuntimeException("Brochure file is empty or missing");
        }

        ProductAttributeCosmeticandPersonalCare attribute = cosmeticRepository
                .findById(productAttributeId)
                .orElseThrow(() -> new RuntimeException(
                        "Cosmetic attribute not found: " + productAttributeId));

        deleteIfRealUrl(attribute.getBrochurePath());

        String productId = attribute.getProductDetails().getProductId();
        String now = LocalDateTime.now().format(TS_FORMATTER);
        String key = buildBrochureKey("cosmetic", productId, productAttributeId, now, brochureFile);
        String url = s3Service.uploadFile(key, brochureFile);
        log.info("Cosmetic brochure uploaded → {}", url);

        attribute.setBrochurePath(url);
        attribute.setModifiedBy(uploadedBy);
        cosmeticRepository.save(attribute);

        return CertificateUploadResponse.builder()
                .productAttributeId(productAttributeId)
                .attributeType("cosmetic")
                .uploadedCertificates(List.of())
                .brochureUrl(url)
                .build();
    }



    // ─────────────────────────────────────────────────────────────
    // S3 KEY BUILDERS
    // ─────────────────────────────────────────────────────────────

    /**
     * products/{type}/{attributeId}/certificates/{CERT_NAME}_{timestamp}.{ext}
     * e.g. products/non-consumable/ATTR123/certificates/ISO_20250407120000.pdf
     */
    private String buildCertKey(String type, String productId, String attributeId,
                                String certName, String timestamp, MultipartFile file) {
        String safeName = sanitizeCertName(certName);

        return String.format("products/%s/%s/%s/certificates/%s_%s.%s",
                productId, type, attributeId, safeName, timestamp, extension(file));
    }

    private String sanitizeCertName(String certName) {
        if (certName == null) return "UNKNOWN_CERT";

        return certName.trim()
                .toUpperCase() // optional but recommended for consistency
                .replaceAll("[^A-Z0-9]+", "_")   // replace anything not alphanumeric
                .replaceAll("_+", "_")           // collapse multiple _
                .replaceAll("^_|_$", "");        // remove leading/trailing _
    }

    /**
     * products/{type}/{attributeId}/brochure/BROCHURE_{timestamp}.{ext}
     * e.g. products/non-consumable/ATTR123/brochure/BROCHURE_20250407120000.pdf
     */
    private String buildBrochureKey(String type, String productId, String attributeId,
                                    String timestamp, MultipartFile file) {
        return String.format("products/%s/%s/%s/brochure/BROCHURE_%s.%s",
                productId, type, attributeId, timestamp, extension(file));
    }

    // ─────────────────────────────────────────────────────────────
    // HELPERS
    // ─────────────────────────────────────────────────────────────

    /**
     * Returns true when a MultipartFile is non-null and non-empty.
     */
    private boolean hasFile(MultipartFile file) {
        return file != null && !file.isEmpty();
    }

    /**
     * Extracts file extension from the original filename.
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
     * Deletes the existing S3 object only when {@code url} is a real S3 URL.
     * Skips deletion when the value is null, blank, or a local placeholder
     * like "/certs/cdsco.pdf" or "/docs/accucheck-pro.pdf" sent during createProduct.
     */
    private void deleteIfRealUrl(String url) {
        if (url == null || url.isBlank()) return;
        if (!url.startsWith("https://")) return;        // skip local placeholders
        try {
            s3Service.deleteFile(s3Service.extractKeyFromUrl(url));
        } catch (Exception e) {
            log.warn("Could not delete old S3 file (url={}): {}", url, e.getMessage());
        }
    }

    /**
     * Ensures documentIds list is present and has the same size as certificateFiles.
     */
    private void validateParallelLists(List<MultipartFile> files, List<Long> documentIds) {
        if (documentIds == null || documentIds.size() != files.size()) {
            throw new IllegalArgumentException(
                    "documentIds must be provided and must match the number of certificateFiles ("
                            + files.size() + ").");
        }
    }
}