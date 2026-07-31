package com.example.pharmaaggregatorserver.controller.product;

import com.example.pharmaaggregatorserver.dto.product.GstPercentageMasterDto;
import com.example.pharmaaggregatorserver.response.ApiResponse;
import com.example.pharmaaggregatorserver.service.product.GstPercentageMasterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/gst-percentages")
@RequiredArgsConstructor
public class GstPercentageMasterController {

    private final GstPercentageMasterService service;

    /**
     * Get all GST percentages
     */
    @GetMapping
    public ResponseEntity<?> getAllGstPercentages() {
        List<GstPercentageMasterDto> gstPercentages = service.getAllGstPercentages();
        return ResponseEntity.ok(new ApiResponse<>(
                HttpStatus.OK.toString(),
                "GST Percentages Fetched Successfully",
                gstPercentages,
                (long) gstPercentages.size()
        ));
    }

}
