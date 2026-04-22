package com.example.pharmaaggregatorserver.service.product;

import org.springframework.web.multipart.MultipartFile;

public interface ProductUserManualService {

    String uploadManual(String productAttributeId, MultipartFile file);

}
