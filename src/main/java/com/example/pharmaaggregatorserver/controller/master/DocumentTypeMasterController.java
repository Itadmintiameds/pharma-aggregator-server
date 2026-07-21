package com.example.pharmaaggregatorserver.controller.master;

import com.example.pharmaaggregatorserver.dto.master.ResponseDTO.DocumentTypeResponseDTO;
import com.example.pharmaaggregatorserver.service.master.DocumentTypeMasterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/document-types")
@RequiredArgsConstructor
public class DocumentTypeMasterController {

    private final DocumentTypeMasterService documentTypeMasterService;

    @GetMapping
    public ResponseEntity<List<DocumentTypeResponseDTO>> getAllDocumentTypes() {
        return ResponseEntity.ok(documentTypeMasterService.getAllDocumentTypes());
    }
}
