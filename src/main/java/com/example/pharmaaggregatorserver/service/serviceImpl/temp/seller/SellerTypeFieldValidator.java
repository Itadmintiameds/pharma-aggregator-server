package com.example.pharmaaggregatorserver.service.serviceImpl.temp.seller;

import com.example.pharmaaggregatorserver.dto.seller.TempSellerDocumentDTO;
import com.example.pharmaaggregatorserver.dto.seller.TempSellerRequestDTO;
import com.example.pharmaaggregatorserver.entity.master.DocumentTypeMaster;
import com.example.pharmaaggregatorserver.entity.master.SellerTypeMaster;
import com.example.pharmaaggregatorserver.exception.ApplicationException;
import com.example.pharmaaggregatorserver.repository.master.DocumentTypeMasterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Enforces the per-seller-type mandatory/optional/not-applicable document and
 * field matrix from the onboarding requirement spec (4 known, fixed seller
 * types: Manufacturer, White Labeling Company/Marketer, Distributor, PCD).
 * <p>
 * Deliberately a plain hardcoded check rather than a configurable rules engine —
 * the 4 seller types and their requirements are stable/compliance-driven, not
 * expected to change frequently or need runtime configuration.
 */
@Component
@RequiredArgsConstructor
public class SellerTypeFieldValidator {

    private static final String WHITE_LABELING = "WHITE_LABELING";
    private static final String DISTRIBUTOR = "DISTRIBUTOR";
    private static final String PCD = "PCD";
    private static final String MANUFACTURER = "MANUFACTURER";

    private final DocumentTypeMasterRepository documentTypeMasterRepository;

    public void validate(TempSellerRequestDTO requestDTO, SellerTypeMaster sellerType) {
        String type = classify(sellerType);

        // Parent Manufacturer Name: mandatory for PCD, optional elsewhere
        if (PCD.equals(type) && isBlank(requestDTO.getParentManufacturerName())) {
            throw new ApplicationException("Parent Manufacturer Name is required for PCD sellers");
        }

        Set<String> submittedDocumentTypeCodes = resolveDocumentTypeCodes(requestDTO.getDocuments());

        if (WHITE_LABELING.equals(type)) {
            requireDocumentTypes(submittedDocumentTypeCodes,
                    "BRAND_OWNER_AGREEMENT", "Brand Owner Agreement is required for White Labeling Company/Marketer sellers");
            requireDocumentTypes(submittedDocumentTypeCodes,
                    "WHITE_LABELING_AGREEMENT", "White Labeling Agreement is required for White Labeling Company/Marketer sellers");
            requireDocumentTypes(submittedDocumentTypeCodes,
                    "TRADEMARK_CERTIFICATE", "Trademark Certificate is required for White Labeling Company/Marketer sellers");
        } else if (DISTRIBUTOR.equals(type)) {
            requireDocumentTypes(submittedDocumentTypeCodes,
                    "DISTRIBUTION_AGREEMENT", "Distribution Agreement is required for Distributor sellers");
        } else if (PCD.equals(type)) {
            requireDocumentTypes(submittedDocumentTypeCodes,
                    "PCD_AGREEMENT", "PCD Agreement is required for PCD sellers");
        } else {
            // MANUFACTURER
            requireDocumentTypes(submittedDocumentTypeCodes,
                    "GMP_WHO_GMP_CERTIFICATE", "GMP / WHO-GMP Certificate is required for Manufacturer sellers");
        }
    }

    private void requireDocumentTypes(Set<String> submitted, String requiredCode, String errorMessage) {
        if (!submitted.contains(requiredCode)) {
            throw new ApplicationException(errorMessage);
        }
    }

    private Set<String> resolveDocumentTypeCodes(List<TempSellerDocumentDTO> documents) {
        if (documents == null || documents.isEmpty()) {
            return Set.of();
        }
        List<Long> documentTypeIds = documents.stream()
                .map(TempSellerDocumentDTO::getDocumentTypeId)
                .filter(id -> id != null)
                .toList();
        return documentTypeMasterRepository.findAllById(documentTypeIds).stream()
                .map(DocumentTypeMaster::getDocumentTypeCode)
                .collect(Collectors.toSet());
    }

    private String classify(SellerTypeMaster sellerType) {
        String name = sellerType.getSellerTypeName() == null ? "" : sellerType.getSellerTypeName().toUpperCase(Locale.ROOT);
        if (name.contains("WHITE LABEL") || name.contains("MARKETER")) return WHITE_LABELING;
        if (name.contains("DISTRIBUTOR")) return DISTRIBUTOR;
        if (name.contains("PCD")) return PCD;
        return MANUFACTURER;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
