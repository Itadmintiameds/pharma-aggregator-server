package com.example.pharmaaggregatorserver.service.master;
import com.example.pharmaaggregatorserver.dto.master.ResponseDTO.DocumentTypeResponseDTO;
import java.util.List;
public interface DocumentTypeMasterService {
    List<DocumentTypeResponseDTO> getAllDocumentTypes();
}
