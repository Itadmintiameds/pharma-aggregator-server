package com.example.pharmaaggregatorserver.controller.temp.seller;

import com.example.pharmaaggregatorserver.dto.response.temp.seller.TempSellerAdminResponseDTO;
import com.example.pharmaaggregatorserver.dto.response.temp.seller.TempSellerResponseDTO;
import com.example.pharmaaggregatorserver.response.ApiResponse;
import com.example.pharmaaggregatorserver.service.serviceImpl.temp.seller.TempSellerServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/temp-sellers")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class TempSellerController {

    private final TempSellerServiceImpl tempSellerService;

    @GetMapping
    public ResponseEntity<?> getAllTempSellers() {
        List<TempSellerAdminResponseDTO> allTempSellers = tempSellerService.getALLTempSellers();
        return ResponseEntity.ok(new ApiResponse<>(
                HttpStatus.OK.toString(),
                "Temporary Sellers Fetched successfully",
                allTempSellers,
                (long) allTempSellers.size()
        ));
    }

    @GetMapping("/id")
    public ResponseEntity<?> getTempSellerById(@PathVariable Long id) {
        TempSellerResponseDTO dto = tempSellerService.findById();
        return ResponseEntity.ok(new ApiResponse<>(
                HttpStatus.OK.toString(),
                "Temporary Sellers Fetched successfully",
                dto
        ));
    }
}
