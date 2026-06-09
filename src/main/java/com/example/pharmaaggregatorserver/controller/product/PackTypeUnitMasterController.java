package com.example.pharmaaggregatorserver.controller.product;

import com.example.pharmaaggregatorserver.dto.product.PackTypeUnitMasterDto;
import com.example.pharmaaggregatorserver.response.ApiResponse;
import com.example.pharmaaggregatorserver.service.product.PackTypeUnitMasterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/pack-type-units")
@RequiredArgsConstructor
public class PackTypeUnitMasterController {

    private final PackTypeUnitMasterService service;

    /**
     * Get all pack type units
     */
    @GetMapping
    public ResponseEntity<?> getAllUnits() {
        List<PackTypeUnitMasterDto> units = service.getAllUnits();
        return ResponseEntity.ok(new ApiResponse<>(
                HttpStatus.OK.toString(),
                "Pack Type Units Fetched Successfully",
                units,
                (long) units.size()
        ));
    }

}

