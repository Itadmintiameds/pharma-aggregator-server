package com.example.pharmaaggregatorserver.controller.product;

import com.example.pharmaaggregatorserver.dto.product.DosageFormDto;
import com.example.pharmaaggregatorserver.dto.product.PackTypeDto;
import com.example.pharmaaggregatorserver.service.product.DosageFormService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/dosage")
@RequiredArgsConstructor
public class DosageFormController {

    private final DosageFormService dosageFormService;
    @GetMapping("/allDosage")
    public List<DosageFormDto> getAllDosageForms() {
        return dosageFormService.getAllDosageForms();
    }


    @GetMapping("/packType/{dosageId}")
    public List<PackTypeDto> getPackTypesByDosageId(
            @PathVariable Long dosageId) {

        return dosageFormService.getPackTypesByDosageId(dosageId);
    }
}
