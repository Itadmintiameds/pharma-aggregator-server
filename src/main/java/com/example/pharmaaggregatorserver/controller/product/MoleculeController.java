package com.example.pharmaaggregatorserver.controller.product;

import com.example.pharmaaggregatorserver.dto.product.MoleculeDto;
import com.example.pharmaaggregatorserver.repository.product.MoleculeRepository;
import com.example.pharmaaggregatorserver.service.product.MoleculeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/molecules")
@RequiredArgsConstructor
public class MoleculeController {

    private final MoleculeService moleculeService;
    private final MoleculeRepository moleculeRepository;

    @GetMapping("/getAllMolecules")
    public ResponseEntity<List<MoleculeDto>> getAllMolecules() {
        return ResponseEntity.ok(moleculeService.getAllMolecules());
    }

    @GetMapping("/byName")
    public ResponseEntity<MoleculeDto> getByName(@RequestParam String name) {
        return ResponseEntity.ok(moleculeService.getMoleculeByName(name));
    }

    @GetMapping("/getMoleculeById")
    public ResponseEntity<MoleculeDto> getMoleculeById(
            @RequestParam Long moleculeId,
            @RequestParam String productAttributeId) {

        MoleculeDto molecule =
                moleculeService.getMoleculeById(moleculeId, productAttributeId);

        return ResponseEntity.ok(molecule);
    }
}
