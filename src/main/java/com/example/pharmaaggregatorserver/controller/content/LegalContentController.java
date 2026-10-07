package com.example.pharmaaggregatorserver.controller.content;

import com.example.pharmaaggregatorserver.dto.content.ResponseDTO.LegalContentResponseDTO;
import com.example.pharmaaggregatorserver.service.content.LegalContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/content")
@RequiredArgsConstructor
public class LegalContentController {

    private final LegalContentService legalContentService;

    @GetMapping("/{contentKey}")
    public ResponseEntity<LegalContentResponseDTO> getByContentKey(@PathVariable String contentKey) {
        return ResponseEntity.ok(legalContentService.getByContentKey(contentKey.toUpperCase()));
    }
}
