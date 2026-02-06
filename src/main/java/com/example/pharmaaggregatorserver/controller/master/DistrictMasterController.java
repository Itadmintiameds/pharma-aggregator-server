package com.example.pharmaaggregatorserver.controller.master;

import com.example.pharmaaggregatorserver.entity.master.DistrictMaster;
import com.example.pharmaaggregatorserver.response.ApiResponse;
import com.example.pharmaaggregatorserver.service.serviceImpl.master.DistrictMasterServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/districts")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DistrictMasterController {

    private final DistrictMasterServiceImpl districtMasterService;

    /**
     * 1. GET /api/v1/districts
     * Get all districts
     */
    @GetMapping
    public ResponseEntity<?> getAllDistricts() {

        /* TODO: Remove below comments later */
//        try {
//            List<DistrictMaster> districts = districtMasterService.getAllDistricts();
//
//            Map<String, Object> response = new HashMap<>();
//            response.put("success", true);
//            response.put("message", districts.isEmpty() ? "No districts found" : "Districts retrieved successfully");
//            response.put("data", districts);
//            response.put("count", districts.size());
//
//            return ResponseEntity.ok(response);
//
//        } catch (Exception e) {
//            Map<String, Object> errorResponse = new HashMap<>();
//            errorResponse.put("success", false);
//            errorResponse.put("message", "Error retrieving districts: " + e.getMessage());
//            errorResponse.put("data", null);
//
//            return ResponseEntity.internalServerError().body(errorResponse);
//        }

        List<DistrictMaster> districts = districtMasterService.getAllDistricts();
        return ResponseEntity.ok(new ApiResponse<>(
                HttpStatus.OK.toString(),
                "Districts Fetched Successfully",
                districts,
                (long) districts.size())
        );
    }

    /**
     * 2. GET /api/v1/districts/state/{stateId}
     * Get districts by state ID
     */
    @GetMapping("/state/{stateId}")
    public ResponseEntity<?> getDistrictsByStateId(@PathVariable Integer stateId) {
        /* TODO: Remove below comments later */

//        try {
//            List<DistrictMaster> districts = districtMasterService.getDistrictsByStateId(stateId);
//
//            Map<String, Object> response = new HashMap<>();
//            response.put("success", true);
//            response.put("message", districts.isEmpty() ? "No districts found for this state" : "Districts retrieved successfully");
//            response.put("data", districts);
//            response.put("count", districts.size());
//
//            return ResponseEntity.ok(response);
//
//        } catch (Exception e) {
//            Map<String, Object> errorResponse = new HashMap<>();
//            errorResponse.put("success", false);
//            errorResponse.put("message", "Error retrieving districts: " + e.getMessage());
//            errorResponse.put("data", null);
//
//            return ResponseEntity.internalServerError().body(errorResponse);
//        }
        List<DistrictMaster> districts = districtMasterService.getDistrictsByStateId(stateId);
        return ResponseEntity.ok().body(new ApiResponse<>(
                HttpStatus.OK.toString(),
                "Districts Fetched Successfully",
                districts,
                (long) districts.size())
        );
    }
}