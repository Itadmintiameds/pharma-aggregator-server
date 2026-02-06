package com.example.pharmaaggregatorserver.controller.master;

import com.example.pharmaaggregatorserver.entity.master.StateMaster;
import com.example.pharmaaggregatorserver.response.ApiResponse;
import com.example.pharmaaggregatorserver.service.serviceImpl.master.StateMasterServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/states")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // Allow frontend access
public class StateMasterController {

    private final StateMasterServiceImpl stateMasterService;

    /**
     * GET /api/v1/states
     * Get all states from the database
     */
    @GetMapping
    public ResponseEntity<?> getAllStates() {

        /* TODO: Remove below comments later */
//        try {
//            List<StateMaster> states = stateMasterService.getAllStates();
//
//            // Create response
//            Map<String, Object> response = new HashMap<>();
//            response.put("success", true);
//            response.put("message", states.isEmpty() ? "No states found" : "States retrieved successfully");
//            response.put("data", states);
//            response.put("count", states.size());
//
//            return ResponseEntity.ok(states);
//
//        } catch (Exception e) {
//            Map<String, Object> errorResponse = new HashMap<>();
//            errorResponse.put("success", false);
//            errorResponse.put("message", "Error retrieving states: " + e.getMessage());
//            errorResponse.put("data", null);
//
//            return ResponseEntity.internalServerError().body(errorResponse);
//        }

        List<StateMaster> states = stateMasterService.getAllStates();

        return ResponseEntity.ok(new ApiResponse<>(
                HttpStatus.OK.toString(),
                "States Fetched Successfully",
                states,
                (long) states.size()));
    }
}
