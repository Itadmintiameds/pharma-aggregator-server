package com.example.pharmaaggregatorserver.controller;

import com.example.pharmaaggregatorserver.entity.DistrictMaster;
import com.example.pharmaaggregatorserver.service.DistrictMasterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/districts")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DistrictMasterController {

    private final DistrictMasterService districtMasterService;

    /**
     * 1. GET /api/v1/districts
     * Get all districts
     */
    @GetMapping
    public ResponseEntity<?> getAllDistricts() {
        try {
            List<DistrictMaster> districts = districtMasterService.getAllDistricts();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", districts.isEmpty() ? "No districts found" : "Districts retrieved successfully");
            response.put("data", districts);
            response.put("count", districts.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error retrieving districts: " + e.getMessage());
            errorResponse.put("data", null);

            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * 2. GET /api/v1/districts/state/{stateId}
     * Get districts by state ID
     */
    @GetMapping("/state/{stateId}")
    public ResponseEntity<?> getDistrictsByStateId(@PathVariable Integer stateId) {
        try {
            List<DistrictMaster> districts = districtMasterService.getDistrictsByStateId(stateId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", districts.isEmpty() ? "No districts found for this state" : "Districts retrieved successfully");
            response.put("data", districts);
            response.put("count", districts.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error retrieving districts: " + e.getMessage());
            errorResponse.put("data", null);

            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
}