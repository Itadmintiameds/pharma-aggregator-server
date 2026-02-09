package com.example.pharmaaggregatorserver.controller.temp.seller;


import com.example.pharmaaggregatorserver.dto.seller.TempSellerRequestDTO;
import com.example.pharmaaggregatorserver.dto.seller.TempSellerResponseDTO;
import com.example.pharmaaggregatorserver.service.temp.seller.TempSellerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/temp-sellers")
@RequiredArgsConstructor
public class TempSellerController {

    private final TempSellerService tempSellerService;

    @PostMapping
    public ResponseEntity<TempSellerResponseDTO> createTempSeller(
            @Valid @RequestBody TempSellerRequestDTO requestDTO) {
        TempSellerResponseDTO response = tempSellerService.createTempSeller(requestDTO);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}
