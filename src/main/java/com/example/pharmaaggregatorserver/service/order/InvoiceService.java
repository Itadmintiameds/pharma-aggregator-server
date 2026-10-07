package com.example.pharmaaggregatorserver.service.order;

import com.example.pharmaaggregatorserver.dto.order.InvoiceResponseDTO;
import com.example.pharmaaggregatorserver.service.order.support.InvoicePdfResult;

public interface InvoiceService {

    InvoiceResponseDTO generateInvoice(String sellerOrderId);

    /**
     * Same generation/idempotency behavior as {@link #generateInvoice}, but
     * also returns the freshly built PDF bytes so the caller (order
     * placement) can attach it straight to an email without a second S3 round
     * trip. Returns null pdfBytes if the invoice already existed.
     */
    InvoicePdfResult generateInvoiceWithPdfBytes(String sellerOrderId);

    InvoiceResponseDTO getInvoice(Long invoiceId);
}
