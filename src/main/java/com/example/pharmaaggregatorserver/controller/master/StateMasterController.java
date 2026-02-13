package com.example.pharmaaggregatorserver.controller.master;


import com.example.pharmaaggregatorserver.dto.master.ResponseDTO.StateResponseDTO;
import com.example.pharmaaggregatorserver.service.master.StateMasterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/master/states")
@RequiredArgsConstructor
public class StateMasterController {

    private final StateMasterService stateMasterService;

    @GetMapping
    public ResponseEntity<List<StateResponseDTO>> getAllStates() {
        return ResponseEntity.ok(stateMasterService.getAllStates());
    }
}