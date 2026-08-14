package com.example.pharmaaggregatorserver.service.order.orderImpl;

import com.example.pharmaaggregatorserver.dto.order.InvoiceResponseDTO;
import com.example.pharmaaggregatorserver.entity.order.Invoice;
import com.example.pharmaaggregatorserver.entity.order.OrderItem;
import com.example.pharmaaggregatorserver.entity.order.SellerOrder;
import com.example.pharmaaggregatorserver.entity.order.SellerOrderStatus;
import com.example.pharmaaggregatorserver.exception.BadRequestException;
import com.example.pharmaaggregatorserver.exception.ResourceNotFoundException;
import com.example.pharmaaggregatorserver.repository.order.InvoiceRepository;
import com.example.pharmaaggregatorserver.repository.order.SellerOrderRepository;
import com.example.pharmaaggregatorserver.service.S3Service;
import com.example.pharmaaggregatorserver.service.order.InvoiceService;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * Generates a per-SellerOrder invoice once fulfilment has reached SHIPPED.
 * <p>
 * Deviation: the spec asks to "generate PDF via existing PdfService", but
 * PdfService (which is in the do-not-modify list for this build) only has
 * methods for the seller-approval agreement PDF — there is no generic/
 * invoice PDF method on it, and one cannot be added without modifying that
 * file. This implementation therefore builds the invoice PDF directly with
 * iText (the same library PdfService itself uses), following the exact same
 * structural pattern PdfService uses — build a Document, write it to a local
 * file under a dedicated directory, then hand that file to
 * S3Service.uploadFileFromResource (used completely as-is) — rather than
 * either modifying PdfService or leaving invoice PDFs unimplemented.
 */
@Service
@RequiredArgsConstructor
public class InvoiceServiceImpl implements InvoiceService {

    private static final String INVOICE_PDF_DIR = "invoices/";
    private static final Set<String> INVOICE_ELIGIBLE_STATUSES = Set.of(
            SellerOrderStatus.SHIPPED, SellerOrderStatus.OUT_FOR_DELIVERY, SellerOrderStatus.DELIVERED);

    private final SellerOrderRepository sellerOrderRepository;
    private final InvoiceRepository invoiceRepository;
    private final S3Service s3Service;

    @Override
    @Transactional
    public InvoiceResponseDTO generateInvoice(String sellerOrderId) {
        SellerOrder sellerOrder = sellerOrderRepository.findBySellerOrderId(sellerOrderId)
                .orElseThrow(() -> new ResourceNotFoundException("Seller order not found: " + sellerOrderId));

        if (sellerOrder.getInvoice() != null) {
            return toDto(sellerOrder.getInvoice());
        }

        if (!INVOICE_ELIGIBLE_STATUSES.contains(sellerOrder.getStatus())) {
            throw new BadRequestException(
                    "Seller order " + sellerOrderId + " must have shipped before an invoice can be generated"
                            + " (current status: " + sellerOrder.getStatus() + ")");
        }

        String sellerId = sellerOrder.getSeller().getSellerId();
        String fy = currentFinancialYearSuffix();
        String prefix = "INV-" + sellerId + "-" + fy + "-";
        long nextSeq = invoiceRepository.countByInvoiceNumberPrefix(prefix) + 1;
        String invoiceNumber = prefix + String.format("%05d", nextSeq);

        String pdfPath = generateInvoicePdf(sellerOrder, invoiceNumber);
        String fileUrl;
        try {
            String key = INVOICE_PDF_DIR + invoiceNumber.replace("/", "-") + ".pdf";
            fileUrl = s3Service.uploadFileFromResource(key, new FileSystemResource(pdfPath), "application/pdf");
        } finally {
            new File(pdfPath).delete();
        }

        Invoice invoice = new Invoice();
        invoice.setSellerOrder(sellerOrder);
        invoice.setInvoiceNumber(invoiceNumber);
        invoice.setInvoiceFileUrl(fileUrl);
        invoice.setGeneratedAt(LocalDateTime.now());
        invoice = invoiceRepository.save(invoice);

        sellerOrder.setInvoice(invoice);
        sellerOrderRepository.save(sellerOrder);

        return toDto(invoice);
    }

    @Override
    public InvoiceResponseDTO getInvoice(Long invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found: " + invoiceId));
        return toDto(invoice);
    }

    private String currentFinancialYearSuffix() {
        LocalDate now = LocalDate.now();
        int startYear = now.getMonthValue() >= 4 ? now.getYear() : now.getYear() - 1;
        int endYear = startYear + 1;
        return String.format("%02d%02d", startYear % 100, endYear % 100);
    }

    private String generateInvoicePdf(SellerOrder sellerOrder, String invoiceNumber) {
        try {
            File dir = new File(INVOICE_PDF_DIR);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            String path = INVOICE_PDF_DIR + invoiceNumber.replace("/", "-") + ".pdf";

            PdfWriter writer = new PdfWriter(path);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            document.add(new Paragraph("TAX INVOICE").setBold().setFontSize(18).setTextAlignment(TextAlignment.CENTER));
            document.add(new Paragraph("\n"));

            Table headerTable = new Table(UnitValue.createPercentArray(new float[]{30, 70})).useAllAvailableWidth();
            headerTable.addCell(labelCell("Invoice Number"));
            headerTable.addCell(valueCell(invoiceNumber));
            headerTable.addCell(labelCell("Seller Order"));
            headerTable.addCell(valueCell(sellerOrder.getSellerOrderId()));
            headerTable.addCell(labelCell("Order"));
            headerTable.addCell(valueCell(sellerOrder.getOrder().getOrderId()));
            headerTable.addCell(labelCell("Seller"));
            headerTable.addCell(valueCell(sellerOrder.getSeller().getSellerName()));
            headerTable.addCell(labelCell("Date"));
            headerTable.addCell(valueCell(LocalDate.now().toString()));
            document.add(headerTable);

            document.add(new Paragraph("\n"));

            Table itemsTable = new Table(UnitValue.createPercentArray(new float[]{35, 15, 15, 15, 20})).useAllAvailableWidth();
            itemsTable.addHeaderCell(labelCell("Item"));
            itemsTable.addHeaderCell(labelCell("Qty"));
            itemsTable.addHeaderCell(labelCell("Unit Price"));
            itemsTable.addHeaderCell(labelCell("Tax"));
            itemsTable.addHeaderCell(labelCell("Line Total"));

            for (OrderItem item : sellerOrder.getOrderItems()) {
                itemsTable.addCell(valueCell(item.getProductNameSnapshot()));
                itemsTable.addCell(valueCell(String.valueOf(item.getQuantity())));
                itemsTable.addCell(valueCell(String.valueOf(item.getUnitPriceSnapshot())));
                itemsTable.addCell(valueCell(String.valueOf(item.getTaxAmount())));
                itemsTable.addCell(valueCell(String.valueOf(item.getLineTotal())));
            }
            document.add(itemsTable);

            document.add(new Paragraph("\n"));
            document.add(new Paragraph("Grand Total: " + sellerOrder.getGrandTotal()).setBold());

            document.close();
            return path;
        } catch (Exception e) {
            throw new RuntimeException("Invoice PDF generation failed: " + e.getMessage(), e);
        }
    }

    private Cell labelCell(String text) {
        return new Cell().add(new Paragraph(text).setBold()).setPadding(4);
    }

    private Cell valueCell(String text) {
        return new Cell().add(new Paragraph(text == null ? "" : text)).setPadding(4);
    }

    private InvoiceResponseDTO toDto(Invoice invoice) {
        return InvoiceResponseDTO.builder()
                .invoiceId(invoice.getInvoiceId())
                .sellerOrderId(invoice.getSellerOrder() != null ? invoice.getSellerOrder().getSellerOrderId() : null)
                .invoiceNumber(invoice.getInvoiceNumber())
                .invoiceFileUrl(invoice.getInvoiceFileUrl())
                .generatedAt(invoice.getGeneratedAt())
                .build();
    }
}
