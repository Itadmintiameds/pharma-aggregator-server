package com.example.pharmaaggregatorserver.controller.seller;
import com.example.pharmaaggregatorserver.dto.seller.SellerResponseDTO;
import com.example.pharmaaggregatorserver.service.seller.sellerImpl.SellerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/getallsellers")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Seller Management", description = "Endpoints for managing sellers")
public class GetAllSellerController {
    private final SellerService sellerService;

    @GetMapping
    @Operation(summary = "Get all sellers", description = "Retrieves a list of all active sellers with their details")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved sellers"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<SellerResponseDTO>> getAllSellers() {
        log.info("REST request to get all sellers");
        List<SellerResponseDTO> sellers = sellerService.getAllSellers();
        return new ResponseEntity<>(sellers, HttpStatus.OK);
    }
}
