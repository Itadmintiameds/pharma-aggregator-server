package com.example.pharmaaggregatorserver.controller.order;

import com.example.pharmaaggregatorserver.dto.order.InvoiceResponseDTO;
import com.example.pharmaaggregatorserver.response.ApiResponse;
import com.example.pharmaaggregatorserver.service.order.InvoiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceService invoiceService;

    @PostMapping("/generate/{sellerOrderId}")
    public ResponseEntity<ApiResponse<InvoiceResponseDTO>> generateInvoice(@PathVariable String sellerOrderId) {
        InvoiceResponseDTO response = invoiceService.generateInvoice(sellerOrderId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(HttpStatus.CREATED.toString(), "Invoice generated successfully", response));
    }

    @GetMapping("/{invoiceId}")
    public ResponseEntity<ApiResponse<InvoiceResponseDTO>> getInvoice(@PathVariable Long invoiceId) {
        InvoiceResponseDTO response = invoiceService.getInvoice(invoiceId);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.toString(), "Invoice fetched successfully", response));
    }
}
