package com.example.pharmaaggregatorserver.controller;

import com.example.pharmaaggregatorserver.entity.TalukaMaster;
import com.example.pharmaaggregatorserver.response.ApiResponse;
import com.example.pharmaaggregatorserver.service.TalukaMasterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/talukas")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class TalukaMasterController {

    private final TalukaMasterService talukaMasterService;

    /**
     * 1. GET /api/v1/talukas
     * Get all talukas
     */
    @GetMapping
    public ResponseEntity<?> getAllTalukas() {
        List<TalukaMaster> talukas = talukaMasterService.getAllTalukas();
        return ResponseEntity.ok(new ApiResponse<>(
                HttpStatus.OK.toString(),
                "Talukas Fetched Successfully",
                talukas,
                (long) talukas.size()));
    }

    /**
     * 2. GET /api/v1/talukas/district/{districtId}
     * Get talukas by district ID
     */
    @GetMapping("/district/{districtId}")
    public ResponseEntity<?> getTalukasByDistrictId(@PathVariable Integer districtId) {
        List<TalukaMaster> talukas = talukaMasterService.getTalukasByDistrictId(districtId);
        return ResponseEntity.ok(new ApiResponse<>(
                HttpStatus.OK.toString(),
                "Talukas Fetched Successfully",
                talukas,
                (long) talukas.size()));
    }
}
