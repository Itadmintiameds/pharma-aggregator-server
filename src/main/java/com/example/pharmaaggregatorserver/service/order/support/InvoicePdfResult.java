package com.example.pharmaaggregatorserver.service.order.support;

import com.example.pharmaaggregatorserver.dto.order.InvoiceResponseDTO;

/**
 * Carries the freshly generated PDF bytes alongside the persisted Invoice
 * record — needed by OrderPlacementServiceImpl to attach the invoice
 * directly to the order-confirmation email without re-downloading it from
 * S3 right after uploading it there. {@code pdfBytes} is null when the
 * invoice already existed (idempotent replay) since the original bytes
 * aren't kept around after upload.
 */
public record InvoicePdfResult(InvoiceResponseDTO invoice, byte[] pdfBytes) {
}
