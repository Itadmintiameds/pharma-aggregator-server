package com.example.pharmaaggregatorserver.controller.product;

import com.example.pharmaaggregatorserver.dto.product.AgeGroupMasterDto;
import com.example.pharmaaggregatorserver.service.product.AgeGroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/ageGroup")
@RequiredArgsConstructor
public class AgeGroupController {

    private final AgeGroupService service;

    @GetMapping("/getAll")
    public List<AgeGroupMasterDto> getAllAgeGroups() {
        return service.getAllAgeGroups();
    }
}