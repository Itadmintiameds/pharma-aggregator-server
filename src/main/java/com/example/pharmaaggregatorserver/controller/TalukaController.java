package com.example.pharmaaggregatorserver.controller;

import com.example.pharmaaggregatorserver.service.TalukaService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/talukas")
@AllArgsConstructor
public class TalukaController {

    private final TalukaService talukaService;

    @GetMapping("/districtId/{districtId}")
    public ResponseEntity<?> findAllTalukasByDistrictId(@PathVariable Long districtId) {
        return ResponseEntity.ok(talukaService.findByDistrictId(districtId));
    }
}
