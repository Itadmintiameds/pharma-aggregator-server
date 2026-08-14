package com.example.pharmaaggregatorserver.service.order;

import com.example.pharmaaggregatorserver.dto.order.InvoiceResponseDTO;

public interface InvoiceService {

    InvoiceResponseDTO generateInvoice(String sellerOrderId);

    InvoiceResponseDTO getInvoice(Long invoiceId);
}
