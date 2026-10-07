package com.example.pharmaaggregatorserver.controller.product;

import com.example.pharmaaggregatorserver.dto.product.FlavourMasterDto;
import com.example.pharmaaggregatorserver.service.product.FlavourService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/flavours")
@RequiredArgsConstructor
public class FlavourController {

    private final FlavourService flavourService;

    @GetMapping
    public ResponseEntity<?> findAll() {
        return ResponseEntity.ok(flavourService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> findById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(flavourService.findById(id));
    }

    @PostMapping
    public ResponseEntity<?> createFlavour(@RequestBody FlavourMasterDto dto) {
        return ResponseEntity.ok(flavourService.createFlavour(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateFlavour(@PathVariable("id") Long id,
                                           @RequestBody FlavourMasterDto dto) {
        return ResponseEntity.ok(flavourService.updateFlavour(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(flavourService.deleteById(id));
    }

}
