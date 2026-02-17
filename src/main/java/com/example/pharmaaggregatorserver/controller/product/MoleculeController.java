package com.example.pharmaaggregatorserver.controller.product;

import com.example.pharmaaggregatorserver.dto.product.MoleculeDto;
import com.example.pharmaaggregatorserver.service.product.MoleculeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/molecules")
@RequiredArgsConstructor
public class MoleculeController {

    private final MoleculeService moleculeService;

    // ✅ Save molecule
    @PostMapping("/create")
    public ResponseEntity<MoleculeDto> saveMolecule(@RequestBody MoleculeDto dto) {
        return new ResponseEntity<>(
                moleculeService.saveMolecule(dto),
                HttpStatus.CREATED
        );
    }

    // ✅ Fetch molecule by name (used for auto-populate in frontend)
    @GetMapping("/by-name")
    public ResponseEntity<MoleculeDto> getByName(@RequestParam String name) {
        return ResponseEntity.ok(
                moleculeService.getMoleculeByName(name)
        );
    }

    // ✅ Fetch all molecules (dropdown/autocomplete)
    @GetMapping("/getAll")
    public ResponseEntity<List<MoleculeDto>> getAll() {
        return ResponseEntity.ok(
                moleculeService.getAllMolecules()
        );
    }
}
