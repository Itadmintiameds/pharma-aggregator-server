package com.example.pharmaaggregatorserver.dto.seller.profile;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
public class PendingSellerDocumentUploadRequest {

    private MultipartFile gstFile;
    private MultipartFile bankFile;
    private MultipartFile companyRegistrationCertificate;

    /** One file per license document row */
    private List<MultipartFile> licenseFiles;

    /** License type names, positionally matching licenseFiles (e.g. "Drug", "Cosmetic") */
    private List<String> licenseNames;

    /** PendingSellerDocument IDs, positionally matching licenseFiles */
    private List<Long> documentIds;
}