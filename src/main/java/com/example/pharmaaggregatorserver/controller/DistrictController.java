package com.example.pharmaaggregatorserver.controller;

import com.example.pharmaaggregatorserver.service.DistrictService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/districts")
@AllArgsConstructor
public class DistrictController {

    private DistrictService districtService;

    @GetMapping("/stateId/{stateId}")
    public ResponseEntity<?> findAllDistrictsByStateId(@PathVariable Long stateId) {
        return ResponseEntity.ok(districtService.findByStateId(stateId));
    }
}
