package com.example.pharmaaggregatorserver.controller.master;

import com.example.pharmaaggregatorserver.dto.master.RequestDTO.BuyerTypeMasterDTO;
import com.example.pharmaaggregatorserver.response.ApiResponse;
import com.example.pharmaaggregatorserver.service.master.BuyerTypeMasterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/buyer-types")
@RequiredArgsConstructor
public class BuyerTypeMasterController {

    private final BuyerTypeMasterService buyerTypeMasterService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<BuyerTypeMasterDTO>>> getAllBuyerTypes() {
        List<BuyerTypeMasterDTO> buyerTypes = buyerTypeMasterService.findAllActive();
        return ResponseEntity.ok(new ApiResponse<>(
                HttpStatus.OK.toString(),
                "Buyer types fetched successfully",
                buyerTypes,
                (long) buyerTypes.size()
        ));
    }
}
