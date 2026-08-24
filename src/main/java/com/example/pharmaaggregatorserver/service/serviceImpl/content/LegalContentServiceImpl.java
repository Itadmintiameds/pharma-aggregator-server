package com.example.pharmaaggregatorserver.service.serviceImpl.content;

import com.example.pharmaaggregatorserver.dto.content.ResponseDTO.LegalContentResponseDTO;
import com.example.pharmaaggregatorserver.entity.content.LegalContent;
import com.example.pharmaaggregatorserver.exception.ResourceNotFoundException;
import com.example.pharmaaggregatorserver.repository.content.LegalContentRepository;
import com.example.pharmaaggregatorserver.service.content.LegalContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LegalContentServiceImpl implements LegalContentService {

    private final LegalContentRepository legalContentRepository;

    @Override
    public LegalContentResponseDTO getByContentKey(String contentKey) {
        LegalContent legalContent = legalContentRepository
                .findByContentKeyAndIsActiveTrue(contentKey)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No active legal content found for key: " + contentKey));

        return convertToResponseDTO(legalContent);
    }

    private LegalContentResponseDTO convertToResponseDTO(LegalContent legalContent) {
        return new LegalContentResponseDTO(
                legalContent.getContentKey(),
                legalContent.getTitle(),
                legalContent.getContent(),
                legalContent.getVersion(),
                legalContent.getUpdatedAt()
        );
    }
}
