package com.example.pharmaaggregatorserver.controller.master;


import com.example.pharmaaggregatorserver.dto.master.ResponseDTO.SellerTypeResponseDTO;
import com.example.pharmaaggregatorserver.service.master.SellerTypeMasterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/seller-types")
@RequiredArgsConstructor
public class SellerTypeMasterController {

    private final SellerTypeMasterService sellerTypeMasterService;

    @GetMapping
    public ResponseEntity<List<SellerTypeResponseDTO>> getAllSellerTypes() {
        return ResponseEntity.ok(sellerTypeMasterService.getAllSellerTypes());
    }
}