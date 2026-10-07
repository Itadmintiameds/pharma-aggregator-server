package com.example.pharmaaggregatorserver.controller.product;

import com.example.pharmaaggregatorserver.dto.product.MoleculeStrengthFormatDto;
import com.example.pharmaaggregatorserver.service.product.MoleculeStrengthFormatService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/dosageMolecule")
@RequiredArgsConstructor
public class MoleculeStrengthFormatController {

    private final MoleculeStrengthFormatService service;

    @GetMapping("/strengthFormat/{dosageId}")
    public List<MoleculeStrengthFormatDto> getByDosageId(
            @PathVariable Long dosageId) {

        return service.getByDosageId(dosageId);
    }
}
