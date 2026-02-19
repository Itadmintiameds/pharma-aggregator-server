package com.example.pharmaaggregatorserver.controller.product;

import com.example.pharmaaggregatorserver.dto.product.MoleculeDto;
import com.example.pharmaaggregatorserver.repository.product.MoleculeRepository;
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
    private final MoleculeRepository moleculeRepository;

    @PostMapping("/create")
    public ResponseEntity<MoleculeDto> saveMolecule(@RequestBody MoleculeDto dto) {
        return new ResponseEntity<>(
                moleculeService.saveMolecule(dto),
                HttpStatus.CREATED
        );
    }

    @GetMapping("/by-name")
    public ResponseEntity<MoleculeDto> getByName(
            @RequestParam String name
    ) {
        MoleculeDto dto = moleculeService.getMoleculeByName(name);

        return ResponseEntity.ok(dto); // dto can be null
    }



    @GetMapping("/getAll")
    public ResponseEntity<List<MoleculeDto>> getAll() {
        return ResponseEntity.ok(
                moleculeService.getAllMolecules()
        );
    }
}
