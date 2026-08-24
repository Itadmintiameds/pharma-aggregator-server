package com.example.pharmaaggregatorserver.service.content;

import com.example.pharmaaggregatorserver.dto.content.ResponseDTO.LegalContentResponseDTO;

public interface LegalContentService {

    LegalContentResponseDTO getByContentKey(String contentKey);
}
