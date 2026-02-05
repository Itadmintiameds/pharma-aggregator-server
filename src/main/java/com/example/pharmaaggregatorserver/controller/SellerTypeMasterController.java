package com.example.pharmaaggregatorserver.controller;

import com.example.pharmaaggregatorserver.entity.SellerTypeMaster;
import com.example.pharmaaggregatorserver.service.SellerTypeMasterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/seller-types")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SellerTypeMasterController {

    private final SellerTypeMasterService sellerTypeMasterService;

    /**
     * GET /api/v1/seller-types
     * Get all seller types
     */
    @GetMapping
    public ResponseEntity<List<SellerTypeMaster>> getAllSellerTypes() {
        List<SellerTypeMaster> sellerTypes = sellerTypeMasterService.getAllSellerTypes();
        return ResponseEntity.ok(sellerTypes);
    }
}