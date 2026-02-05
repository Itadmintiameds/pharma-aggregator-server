package com.example.pharmaaggregatorserver.controller;

import com.example.pharmaaggregatorserver.entity.StateMaster;
import com.example.pharmaaggregatorserver.service.StateMasterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/states")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // Allow frontend access
public class StateMasterController {

    private final StateMasterService stateMasterService;

    /**
     * GET /api/v1/states
     * Get all states from the database
     */
    @GetMapping
    public ResponseEntity<?> getAllStates() {
        try {
            List<StateMaster> states = stateMasterService.getAllStates();

            // Create response
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", states.isEmpty() ? "No states found" : "States retrieved successfully");
            response.put("data", states);
            response.put("count", states.size());

            return ResponseEntity.ok(states);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error retrieving states: " + e.getMessage());
            errorResponse.put("data", null);

            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
}
