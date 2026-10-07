package com.example.pharmaaggregatorserver.controller.master;


import com.example.pharmaaggregatorserver.dto.master.ResponseDTO.TalukaResponseDTO;
import com.example.pharmaaggregatorserver.service.master.TalukaMasterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/talukas")
@RequiredArgsConstructor
public class TalukaMasterController {

    private final TalukaMasterService talukaMasterService;

    @GetMapping
    public ResponseEntity<List<TalukaResponseDTO>> getAllTalukas() {
        return ResponseEntity.ok(talukaMasterService.getAllTalukas());
    }

    @GetMapping("/district/{districtId}")
    public ResponseEntity<List<TalukaResponseDTO>> getTalukasByDistrictId(@PathVariable Long districtId) {
        return ResponseEntity.ok(talukaMasterService.getTalukasByDistrictId(districtId));
    }
}
