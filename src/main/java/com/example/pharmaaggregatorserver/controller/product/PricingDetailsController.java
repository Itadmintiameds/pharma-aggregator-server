package com.example.pharmaaggregatorserver.controller.product;

import com.example.pharmaaggregatorserver.security.UserDetailsImpl;
import com.example.pharmaaggregatorserver.service.product.PricingDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/pricing")
@RequiredArgsConstructor
public class PricingDetailsController {

    private final PricingDetailsService pricingDetailsService;

    @GetMapping("/validateBatchNumber")
    public ResponseEntity<?> validateBatchNumber(
            @RequestParam String batchLotNumber,
            @RequestParam Long categoryId,
            Authentication authentication
    ) {

        UserDetailsImpl user =
                (UserDetailsImpl) authentication.getPrincipal();

        Long sellerId = user.getId();

        boolean exists = pricingDetailsService
                .isBatchNumberExistsForSeller(
                        batchLotNumber,
                        sellerId,
                        categoryId
                );

        Map<String, Object> response = new HashMap<>();
        response.put("exists", exists);

        if (exists) {
            response.put("message", "Batch number already exists");
        } else {
            response.put("message", "Batch number available");
        }

        return ResponseEntity.ok(response);
    }

}
