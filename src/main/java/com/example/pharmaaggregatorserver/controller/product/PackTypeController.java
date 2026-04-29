package com.example.pharmaaggregatorserver.controller.product;

import com.example.pharmaaggregatorserver.dto.product.PackTypeDto;
import com.example.pharmaaggregatorserver.service.product.PackTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/packType")
@RequiredArgsConstructor
public class PackTypeController {

    private final PackTypeService packTypeService;

    @GetMapping("/packTypeById/{packTypeId}")
    public PackTypeDto getPackTypeById(@PathVariable("packTypeId") Long packId) {
        return packTypeService.getPackTypeById(packId);
    }
}