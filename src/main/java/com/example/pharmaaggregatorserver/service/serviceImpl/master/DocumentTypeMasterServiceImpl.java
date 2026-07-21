package com.example.pharmaaggregatorserver.service.serviceImpl.master;

import com.example.pharmaaggregatorserver.dto.master.ResponseDTO.DocumentTypeResponseDTO;
import com.example.pharmaaggregatorserver.entity.master.DocumentTypeMaster;
import com.example.pharmaaggregatorserver.repository.master.DocumentTypeMasterRepository;
import com.example.pharmaaggregatorserver.service.master.DocumentTypeMasterService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DocumentTypeMasterServiceImpl implements DocumentTypeMasterService {

    private final DocumentTypeMasterRepository documentTypeMasterRepository;

    private DocumentTypeResponseDTO convertToResponseDTO(DocumentTypeMaster entity) {
        return new DocumentTypeResponseDTO(
                entity.getDocumentTypeId(),
                entity.getDocumentTypeName(),
                entity.getDocumentTypeCode(),
                entity.getIsActive()
        );
    }

    @Override
    public List<DocumentTypeResponseDTO> getAllDocumentTypes() {
        return documentTypeMasterRepository.findAll()
                .stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }
}
