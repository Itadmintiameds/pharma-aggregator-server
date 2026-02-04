package com.example.pharmaaggregatorserver.controller;

import com.example.pharmaaggregatorserver.service.StateService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/states")
@AllArgsConstructor
public class StateController {

    private final StateService stateService;

    @GetMapping
    public ResponseEntity<?> findAll() {
        return ResponseEntity.ok(stateService.findAll());
    }
}
