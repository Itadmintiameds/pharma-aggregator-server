package com.example.pharmaaggregatorserver.controller.product;

import com.example.pharmaaggregatorserver.dto.product.StrengthUnitDto;
import com.example.pharmaaggregatorserver.response.ApiResponse;
import com.example.pharmaaggregatorserver.service.product.StrengthUnitService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/strengthUnit")
@RequiredArgsConstructor
public class StrengthUnitController {

    private final StrengthUnitService strengthUnitService;

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<?> getByCategoryId(@PathVariable Long categoryId) {
        List<StrengthUnitDto> strengthUnits = strengthUnitService.getByCategoryId(categoryId);
        return ResponseEntity.ok(new ApiResponse<>(
                HttpStatus.OK.toString(),
                "Strength Units Fetched Successfully",
                strengthUnits,
                (long) strengthUnits.size()
        ));
    }

    @GetMapping("/{strengthUnitId}")
    public ResponseEntity<?> getById(@PathVariable Long strengthUnitId) {
        StrengthUnitDto strengthUnit = strengthUnitService.getById(strengthUnitId);
        return ResponseEntity.ok(new ApiResponse<>(
                HttpStatus.OK.toString(),
                "Strength Unit Fetched Successfully",
                strengthUnit
        ));
    }
}
