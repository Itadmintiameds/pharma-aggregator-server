package com.example.pharmaaggregatorserver.controller.master;

import com.example.pharmaaggregatorserver.dto.master.ResponseDTO.DistrictResponseDTO;
import com.example.pharmaaggregatorserver.service.master.DistrictMasterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;


import java.util.List;

@RestController
@RequestMapping("/api/v1/master/districts")
@RequiredArgsConstructor
public class DistrictMasterController {

    private final DistrictMasterService districtMasterService;

    @GetMapping
    public ResponseEntity<List<DistrictResponseDTO>> getAllDistricts() {
        return ResponseEntity.ok(districtMasterService.getAllDistricts());
    }

    @GetMapping("/state/{stateId}")
    public ResponseEntity<List<DistrictResponseDTO>> getDistrictsByStateId(@PathVariable Long stateId) {
        return ResponseEntity.ok(districtMasterService.getDistrictsByStateId(stateId));
    }
}
