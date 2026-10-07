package com.example.pharmaaggregatorserver.service.order.orderImpl;

import com.example.pharmaaggregatorserver.dto.order.InvoiceResponseDTO;
import com.example.pharmaaggregatorserver.entity.order.Invoice;
import com.example.pharmaaggregatorserver.entity.order.Order;
import com.example.pharmaaggregatorserver.entity.order.OrderItem;
import com.example.pharmaaggregatorserver.entity.order.SellerOrder;
import com.example.pharmaaggregatorserver.entity.seller.Seller;
import com.example.pharmaaggregatorserver.entity.seller.SellerAddress;
import com.example.pharmaaggregatorserver.entity.seller.SellerGST;
import com.example.pharmaaggregatorserver.exception.ResourceNotFoundException;
import com.example.pharmaaggregatorserver.repository.order.InvoiceRepository;
import com.example.pharmaaggregatorserver.repository.order.SellerOrderRepository;
import com.example.pharmaaggregatorserver.service.S3Service;
import com.example.pharmaaggregatorserver.service.order.InvoiceService;
import com.example.pharmaaggregatorserver.service.order.support.InvoicePdfResult;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Generates a professional, GST-format tax invoice per {@link SellerOrder} —
 * one invoice per seller, since each seller is its own GST-registered entity.
 * Called automatically on DELIVERY confirmation (see
 * SellerOrderFulfillmentServiceImpl#markDelivered), not at order placement —
 * a tax invoice represents goods actually supplied, which for a COD order is
 * only true once delivery is confirmed. The manual
 * {@code POST /invoices/generate/{sellerOrderId}} endpoint still works too
 * (idempotent — returns the existing invoice if one was already generated,
 * downloading its bytes back from S3 so it can still be attached to an email
 * even on that idempotent path).
 * <p>
 * Built entirely in-memory (ByteArrayOutputStream, no local temp file) with
 * iText, then uploaded to S3 via {@link S3Service#uploadFileFromResource} —
 * the same bytes are also handed back to the caller so they can be attached
 * directly to an email without re-downloading from S3 (except on the
 * idempotent/already-exists path, which downloads once to serve the same purpose).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class InvoiceServiceImpl implements InvoiceService {

    private static final String INVOICE_PDF_DIR = "invoices/";
    private static final DeviceRgb BRAND_COLOR = new DeviceRgb(150, 89, 253); // #9659FD
    private static final DeviceRgb LIGHT_GRAY = new DeviceRgb(245, 245, 248);
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy");

    private final SellerOrderRepository sellerOrderRepository;
    private final InvoiceRepository invoiceRepository;
    private final S3Service s3Service;

    @Override
    @Transactional
    public InvoiceResponseDTO generateInvoice(String sellerOrderId) {
        return generateInvoiceWithPdfBytes(sellerOrderId).invoice();
    }

    // Deliberately NOT @Transactional: OrderPlacementServiceImpl calls this
    // from inside its own @Transactional placeOrder() and catches any
    // exception here to keep invoice generation best-effort. If this method
    // carried its own @Transactional, Spring would mark the ambient (shared)
    // transaction rollback-only the moment an exception crossed THIS method's
    // proxy boundary — before the caller's try/catch even runs — which would
    // silently doom the whole order placement despite the catch appearing to
    // handle it. The PDF-build/S3-upload steps below (the realistic failure
    // modes — a transient S3/network hiccup) run before any repository write,
    // so they're plain method calls with no transactional proxy to poison;
    // only the final invoiceRepository/sellerOrderRepository saves are
    // transactional (join whatever ambient transaction is active, exactly
    // like every other repository call in placeOrder), which is correct — a
    // genuine DB failure at that point should still fail the whole order.
    @Override
    public InvoicePdfResult generateInvoiceWithPdfBytes(String sellerOrderId) {
        SellerOrder sellerOrder = sellerOrderRepository.findBySellerOrderId(sellerOrderId)
                .orElseThrow(() -> new ResourceNotFoundException("Seller order not found: " + sellerOrderId));

        if (sellerOrder.getInvoice() != null) {
            Invoice existing = sellerOrder.getInvoice();
            // Still fetch the bytes back from S3 so this invoice CAN be attached to
            // an email even though it wasn't generated in this call — e.g. it was
            // already generated once (manually, or by an earlier code path) before
            // whatever triggered this call (delivery confirmation) ran. Best-effort:
            // a download failure here just means no attachment, same as before.
            return new InvoicePdfResult(toDto(existing), downloadInvoiceBytes(existing.getInvoiceFileUrl()));
        }

        String sellerId = sellerOrder.getSeller().getSellerId();
        String fy = currentFinancialYearSuffix();
        String prefix = "INV-" + sellerId + "-" + fy + "-";
        long nextSeq = invoiceRepository.countByInvoiceNumberPrefix(prefix) + 1;
        String invoiceNumber = prefix + String.format("%05d", nextSeq);

        byte[] pdfBytes = generateInvoicePdf(sellerOrder, invoiceNumber);
        String key = INVOICE_PDF_DIR + invoiceNumber.replace("/", "-") + ".pdf";
        String fileUrl = s3Service.uploadFileFromResource(key, new ByteArrayResource(pdfBytes), "application/pdf");

        Invoice invoice = new Invoice();
        invoice.setSellerOrder(sellerOrder);
        invoice.setInvoiceNumber(invoiceNumber);
        invoice.setInvoiceFileUrl(fileUrl);
        invoice.setGeneratedAt(java.time.LocalDateTime.now());
        invoice = invoiceRepository.save(invoice);

        sellerOrder.setInvoice(invoice);
        sellerOrderRepository.save(sellerOrder);

        return new InvoicePdfResult(toDto(invoice), pdfBytes);
    }

    @Override
    public InvoiceResponseDTO getInvoice(Long invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found: " + invoiceId));
        return toDto(invoice);
    }

    // Best-effort: used only on the idempotent "invoice already exists" path,
    // where the original in-memory PDF bytes are long gone. A failure here
    // (network hiccup, bucket access issue) just means no attachment gets
    // sent — never propagated, since a missing invoice attachment must not
    // block whatever triggered this call (e.g. delivery confirmation).
    private byte[] downloadInvoiceBytes(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank()) {
            return null;
        }
        try {
            HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
            HttpRequest request = HttpRequest.newBuilder(URI.create(fileUrl)).GET().build();
            HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                return response.body();
            }
            log.warn("Failed to download existing invoice from {}: HTTP {}", fileUrl, response.statusCode());
        } catch (Exception e) {
            log.warn("Failed to download existing invoice from {}: {}", fileUrl, e.getMessage());
        }
        return null;
    }

    private String currentFinancialYearSuffix() {
        LocalDate now = LocalDate.now();
        int startYear = now.getMonthValue() >= 4 ? now.getYear() : now.getYear() - 1;
        int endYear = startYear + 1;
        return String.format("%02d%02d", startYear % 100, endYear % 100);
    }

    // ─────────────────────────────────────────────────────────
    // PDF generation
    // ─────────────────────────────────────────────────────────

    private byte[] generateInvoicePdf(SellerOrder sellerOrder, String invoiceNumber) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            PdfWriter writer = new PdfWriter(outputStream);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);
            document.setMargins(30, 36, 30, 36);

            Order order = sellerOrder.getOrder();
            Seller seller = sellerOrder.getSeller();
            SellerAddress sellerAddress = seller.getAddress();
            SellerGST sellerGST = seller.getSellerGST();

            addLetterhead(document, invoiceNumber, order, sellerOrder);
            addPartyBlocks(document, seller, sellerAddress, sellerGST, order);

            boolean sameState = isSameState(sellerAddress, order);
            addItemsTable(document, sellerOrder, sameState);
            addTotalsBlock(document, sellerOrder, sameState);
            addFooter(document);

            document.close();
            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Invoice PDF generation failed: " + e.getMessage(), e);
        }
    }

    private void addLetterhead(Document document, String invoiceNumber, Order order, SellerOrder sellerOrder) {
        Table header = new Table(UnitValue.createPercentArray(new float[]{60, 40})).useAllAvailableWidth();
        header.setBorder(Border.NO_BORDER);

        Cell brandCell = new Cell().setBorder(Border.NO_BORDER);
        brandCell.add(new Paragraph("TiaMeds Marketplace").setBold().setFontSize(18).setFontColor(BRAND_COLOR));
        brandCell.add(new Paragraph("Tax Invoice").setFontSize(11).setFontColor(ColorConstants.GRAY));
        header.addCell(brandCell);

        Cell metaCell = new Cell().setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.RIGHT);
        metaCell.add(new Paragraph("Invoice No: " + invoiceNumber).setFontSize(10).setBold());
        metaCell.add(new Paragraph("Invoice Date: " + LocalDate.now().format(DATE_FORMAT)).setFontSize(10));
        metaCell.add(new Paragraph("Order No: " + order.getOrderId()).setFontSize(10));
        metaCell.add(new Paragraph("Seller Order No: " + sellerOrder.getSellerOrderId()).setFontSize(10));
        header.addCell(metaCell);

        document.add(header);
        document.add(dividerLine());
    }

    private void addPartyBlocks(Document document, Seller seller, SellerAddress sellerAddress,
                                 SellerGST sellerGST, Order order) {
        Table parties = new Table(UnitValue.createPercentArray(new float[]{50, 50})).useAllAvailableWidth();
        parties.setMarginTop(14).setMarginBottom(14);

        Cell soldBy = new Cell().setPadding(10).setBackgroundColor(LIGHT_GRAY).setBorder(Border.NO_BORDER);
        soldBy.add(new Paragraph("SOLD BY").setBold().setFontSize(9).setFontColor(ColorConstants.GRAY));
        soldBy.add(new Paragraph(nz(seller.getSellerName())).setBold().setFontSize(11));
        if (sellerAddress != null) {
            soldBy.add(new Paragraph(addressLine(sellerAddress)).setFontSize(9.5f));
        }
        soldBy.add(new Paragraph("Phone: " + nz(seller.getPhone())).setFontSize(9.5f));
        soldBy.add(new Paragraph("Email: " + nz(seller.getEmail())).setFontSize(9.5f));
        if (sellerGST != null && sellerGST.getGstNumber() != null) {
            soldBy.add(new Paragraph("GSTIN: " + sellerGST.getGstNumber()).setFontSize(9.5f).setBold());
        }
        parties.addCell(soldBy);

        Cell billTo = new Cell().setPadding(10).setBackgroundColor(LIGHT_GRAY).setBorder(Border.NO_BORDER);
        billTo.add(new Paragraph("BILL TO / SHIP TO").setBold().setFontSize(9).setFontColor(ColorConstants.GRAY));
        billTo.add(new Paragraph(nz(order.getDeliveryName())).setBold().setFontSize(11));
        billTo.add(new Paragraph(nz(order.getDeliveryAddressLine())).setFontSize(9.5f));
        billTo.add(new Paragraph(joinNonBlank(order.getDeliveryCity(), order.getDeliveryDistrict())
                + (order.getDeliveryState() != null ? ", " + order.getDeliveryState() : "")
                + (order.getDeliveryPinCode() != null ? " - " + order.getDeliveryPinCode() : ""))
                .setFontSize(9.5f));
        billTo.add(new Paragraph("Phone: " + nz(order.getDeliveryPhone())).setFontSize(9.5f));
        if (order.getBuyer() != null && order.getBuyer().getGstNumber() != null
                && !order.getBuyer().getGstNumber().isBlank()) {
            billTo.add(new Paragraph("GSTIN: " + order.getBuyer().getGstNumber()).setFontSize(9.5f).setBold());
        }
        parties.addCell(billTo);

        document.add(parties);
    }

    private void addItemsTable(Document document, SellerOrder sellerOrder, boolean sameState) {
        String[] headers = sameState
                ? new String[]{"#", "Item", "HSN", "Batch", "Qty", "Rate", "Discount", "Taxable", "CGST", "SGST", "Amount"}
                : new String[]{"#", "Item", "HSN", "Batch", "Qty", "Rate", "Discount", "Taxable", "IGST", "Amount"};
        float[] widths = sameState
                ? new float[]{4, 22, 8, 10, 6, 9, 9, 10, 8, 8, 11}
                : new float[]{4, 24, 8, 11, 6, 10, 10, 11, 10, 11};

        Table table = new Table(UnitValue.createPercentArray(widths)).useAllAvailableWidth();
        for (String h : headers) {
            table.addHeaderCell(new Cell().add(new Paragraph(h).setBold().setFontSize(8.5f).setFontColor(ColorConstants.WHITE))
                    .setBackgroundColor(BRAND_COLOR).setPadding(5).setTextAlignment(TextAlignment.CENTER));
        }

        int index = 1;
        for (OrderItem item : sellerOrder.getOrderItems()) {
            BigDecimal unitPrice = nzBd(item.getUnitPriceSnapshot());
            BigDecimal discount = nzBd(item.getDiscountAmount());
            BigDecimal taxAmount = nzBd(item.getTaxAmount());
            BigDecimal taxable = unitPrice.multiply(BigDecimal.valueOf(item.getQuantity())).subtract(discount);
            Long hsn = item.getProductDetails() != null ? item.getProductDetails().getHsnCode() : null;

            table.addCell(dataCell(String.valueOf(index++), TextAlignment.CENTER));
            table.addCell(dataCell(nz(item.getProductNameSnapshot()), TextAlignment.LEFT));
            table.addCell(dataCell(hsn != null ? String.valueOf(hsn) : "-", TextAlignment.CENTER));
            table.addCell(dataCell(nz(item.getBatchLotNumberSnapshot()), TextAlignment.CENTER));
            table.addCell(dataCell(String.valueOf(item.getQuantity()), TextAlignment.CENTER));
            table.addCell(dataCell(money(unitPrice), TextAlignment.RIGHT));
            table.addCell(dataCell(money(discount), TextAlignment.RIGHT));
            table.addCell(dataCell(money(taxable), TextAlignment.RIGHT));

            if (sameState) {
                BigDecimal half = taxAmount.divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP);
                table.addCell(dataCell(money(half), TextAlignment.RIGHT));
                table.addCell(dataCell(money(taxAmount.subtract(half)), TextAlignment.RIGHT));
            } else {
                table.addCell(dataCell(money(taxAmount), TextAlignment.RIGHT));
            }
            table.addCell(dataCell(money(nzBd(item.getLineTotal())), TextAlignment.RIGHT));
        }

        document.add(table);
    }

    private void addTotalsBlock(Document document, SellerOrder sellerOrder, boolean sameState) {
        BigDecimal taxableTotal = nzBd(sellerOrder.getSubtotal());
        BigDecimal taxTotal = nzBd(sellerOrder.getTaxAmount());
        BigDecimal shipping = nzBd(sellerOrder.getShippingFee());
        BigDecimal grandTotal = nzBd(sellerOrder.getGrandTotal());
        BigDecimal discountTotal = sellerOrder.getOrderItems().stream()
                .map(item -> nzBd(item.getDiscountAmount()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Table totals = new Table(UnitValue.createPercentArray(new float[]{60, 40})).useAllAvailableWidth();
        totals.setMarginTop(10);
        totals.setHorizontalAlignment(HorizontalAlignment.RIGHT);

        totals.addCell(totalLabelCell("Taxable Value", false));
        totals.addCell(totalValueCell(money(taxableTotal), false));

        totals.addCell(totalLabelCell("Discount", false));
        totals.addCell(totalValueCell("- " + money(discountTotal), false));

        if (sameState) {
            BigDecimal half = taxTotal.divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP);
            totals.addCell(totalLabelCell("CGST", false));
            totals.addCell(totalValueCell(money(half), false));
            totals.addCell(totalLabelCell("SGST", false));
            totals.addCell(totalValueCell(money(taxTotal.subtract(half)), false));
        } else {
            totals.addCell(totalLabelCell("IGST", false));
            totals.addCell(totalValueCell(money(taxTotal), false));
        }

        totals.addCell(totalLabelCell("Shipping", false));
        totals.addCell(totalValueCell(money(shipping), false));

        totals.addCell(totalLabelCell("Grand Total", true));
        totals.addCell(totalValueCell("Rs. " + money(grandTotal), true));

        document.add(totals);
    }

    private void addFooter(Document document) {
        document.add(dividerLine().setMarginTop(16));
        document.add(new Paragraph("Payment Mode: Cash on Delivery")
                .setFontSize(9.5f).setBold().setMarginTop(8));
        document.add(new Paragraph(
                "This is a system-generated invoice and does not require a physical signature. "
                        + "For any queries regarding this order, please contact the seller or TiaMeds support.")
                .setFontSize(8.5f).setFontColor(ColorConstants.GRAY).setMarginTop(4));
    }

    // ─────────────────────────────────────────────────────────
    // Small helpers
    // ─────────────────────────────────────────────────────────

    private boolean isSameState(SellerAddress sellerAddress, Order order) {
        if (sellerAddress == null || sellerAddress.getState() == null || order.getDeliveryState() == null) {
            return true; // default to CGST+SGST when place-of-supply can't be determined
        }
        String sellerState = sellerAddress.getState().getStateName();
        return sellerState != null && sellerState.trim().equalsIgnoreCase(order.getDeliveryState().trim());
    }

    private String addressLine(SellerAddress address) {
        return joinNonBlank(address.getBuildingNo(), address.getStreet(), address.getLandmark())
                + (address.getCity() != null ? ", " + address.getCity() : "")
                + (address.getState() != null && address.getState().getStateName() != null
                        ? ", " + address.getState().getStateName() : "")
                + (address.getPinCode() != null ? " - " + address.getPinCode() : "");
    }

    private String joinNonBlank(String... parts) {
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (part != null && !part.isBlank()) {
                if (sb.length() > 0) sb.append(", ");
                sb.append(part);
            }
        }
        return sb.toString();
    }

    private Cell dataCell(String text, TextAlignment alignment) {
        return new Cell().add(new Paragraph(text).setFontSize(8.5f))
                .setPadding(5).setTextAlignment(alignment)
                .setBorder(new SolidBorder(ColorConstants.LIGHT_GRAY, 0.5f));
    }

    private Cell totalLabelCell(String label, boolean emphasized) {
        Paragraph p = new Paragraph(label).setFontSize(emphasized ? 11 : 9.5f);
        if (emphasized) p.setBold();
        return new Cell().add(p).setBorder(Border.NO_BORDER)
                .setBorderTop(emphasized ? new SolidBorder(ColorConstants.BLACK, 1) : Border.NO_BORDER)
                .setPaddingTop(emphasized ? 8 : 2).setPaddingBottom(2);
    }

    private Cell totalValueCell(String value, boolean emphasized) {
        Paragraph p = new Paragraph(value).setFontSize(emphasized ? 11 : 9.5f);
        if (emphasized) p.setBold();
        return new Cell().add(p).setBorder(Border.NO_BORDER)
                .setBorderTop(emphasized ? new SolidBorder(ColorConstants.BLACK, 1) : Border.NO_BORDER)
                .setTextAlignment(TextAlignment.RIGHT)
                .setPaddingTop(emphasized ? 8 : 2).setPaddingBottom(2);
    }

    private Table dividerLine() {
        Table divider = new Table(1).useAllAvailableWidth();
        divider.addCell(new Cell().setHeight(1.5f).setBackgroundColor(ColorConstants.LIGHT_GRAY).setBorder(Border.NO_BORDER));
        return divider;
    }

    private String money(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP).toString();
    }

    private BigDecimal nzBd(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private String nz(String value) {
        return value != null ? value : "";
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
