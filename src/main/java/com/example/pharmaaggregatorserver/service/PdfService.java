package com.example.pharmaaggregatorserver.service;

import com.example.pharmaaggregatorserver.entity.temp.seller.SellerTerms;
import com.example.pharmaaggregatorserver.entity.temp.seller.TempSeller;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.VerticalAlignment;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDate;
import java.util.List;

@Service
public class PdfService {

    private static final String PDF_DIR = "/agreements/";

    public String generateTempSellerAgreementPdf(TempSeller seller) {
        try {
            File dir = new File(PDF_DIR);
            if (!dir.exists()) dir.mkdirs();

            String fileName = "seller_" + seller.getTempSellerRequestId() + ".pdf";
            String path = PDF_DIR + fileName;

            PdfWriter writer = new PdfWriter(path);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // 🔹 TITLE
            Paragraph title = new Paragraph("SELLER AGREEMENT")
                    .setBold()
                    .setFontSize(18)
                    .setTextAlignment(TextAlignment.CENTER);
            document.add(title);

            document.add(new Paragraph("\n"));

            // 🔹 SELLER INFO TABLE
            Table table = new Table(UnitValue.createPercentArray(new float[]{30, 70}))
                    .useAllAvailableWidth();

            table.addCell(getLabelCell("Seller Name"));
            table.addCell(getValueCell(seller.getSellerName()));

            table.addCell(getLabelCell("Email"));
            table.addCell(getValueCell(seller.getEmail()));

            table.addCell(getLabelCell("Phone"));
            table.addCell(getValueCell(seller.getPhone()));

            table.addCell(getLabelCell("Approval Date"));
            table.addCell(getValueCell(LocalDate.now().toString()));

            document.add(table);

            document.add(new Paragraph("\n"));

            // 🔹 AGREEMENT TEXT
            Paragraph body = new Paragraph("""
                    This agreement confirms that the above seller has been approved
                    to operate on the Pharma Aggregator platform.
                    
                    The seller agrees to comply with platform policies, maintain
                    product authenticity, and follow all regulatory requirements.
                    
                    Violation of terms may lead to suspension or termination.
                    """)
                    .setFontSize(12)
                    .setTextAlignment(TextAlignment.JUSTIFIED);

            document.add(body);

            document.add(new Paragraph("\n\n"));

            // 🔹 SIGNATURE SECTION
            Table signTable = new Table(UnitValue.createPercentArray(new float[]{50, 50}))
                    .useAllAvailableWidth();

            signTable.addCell(getValueCell("Authorized Signatory\n\n___________________"));
            signTable.addCell(getValueCell("Seller Signature\n\n___________________"));

            document.add(signTable);

            document.close();
            return path;

        } catch (Exception e) {
            throw new RuntimeException("PDF generation failed", e);
        }
    }

    private Cell getLabelCell(String text) {
        return new Cell()
                .add(new Paragraph(text).setBold())
                .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                .setPadding(5);
    }

    private Cell getValueCell(String text) {
        return new Cell()
                .add(new Paragraph(text))
                .setPadding(5);
    }

    public String generateSellerAgreementPdf(List<SellerTerms> sellerTerms) {

        try {
            String PDF_DIR = "agreements/";
            File dir = new File(PDF_DIR);
            if (!dir.exists()) dir.mkdirs();

            String path = PDF_DIR + "TiaMeds_Seller_Agreement.pdf";

            PdfWriter writer = new PdfWriter(path);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // Set default margins
            document.setMargins(40, 40, 40, 40);

            // ===== TITLE =====
            Paragraph title = new Paragraph("TIAMEDS MARKETPLACE");
            title.setFontSize(18);
            title.setTextAlignment(TextAlignment.CENTER);
            title.setBold();
            title.setMarginBottom(5);
            document.add(title);

            Paragraph subtitle = new Paragraph("SELLER COMPANY DECLARATIONS & ACCEPTANCE");
            subtitle.setFontSize(14);
            subtitle.setTextAlignment(TextAlignment.CENTER);
            subtitle.setBold();
            subtitle.setMarginBottom(20);
            document.add(subtitle);

            // ===== INTRO =====
            Paragraph intro = new Paragraph("These declarations are legally binding upon acceptance by the Seller Company.");
            intro.setFontSize(11);
            intro.setItalic();
            intro.setMarginBottom(15);
            document.add(intro);

            // ===== TERMS LOOP =====
            for (SellerTerms term : sellerTerms) {

                String text = term.getTermText();

                if (text == null || text.trim().isEmpty()) {
                    continue;
                }

                // Split the text block into individual lines
                String[] lines = text.split("\n");

                for (String line : lines) {
                    line = line.trim();

                    if (line.isEmpty()) {
                        continue; // Skip empty lines
                    }

                    // SECTION HEADING (1., 2., 3., etc.)
                    if (line.matches("^\\d+\\.\\s*.*")) {
                        Paragraph heading = new Paragraph(line);
                        heading.setBold();
                        heading.setFontSize(12);
                        heading.setMarginTop(12);
                        heading.setMarginBottom(6);
                        document.add(heading);
                    }

                    // SUB BULLET (**) - Must check BEFORE single *
                    else if (line.startsWith("**")) {
                        String cleanText = line.substring(2).trim();
                        Paragraph subBullet = new Paragraph("      ◦ " + cleanText);
                        subBullet.setFontSize(10);
                        subBullet.setMarginLeft(45);
                        subBullet.setMarginTop(2);
                        subBullet.setMarginBottom(2);
                        document.add(subBullet);
                    }

                    // MAIN BULLET (*) - Check AFTER **
                    else if (line.startsWith("*")) {
                        String cleanText = line.substring(1).trim();
                        Paragraph mainBullet = new Paragraph("   • " + cleanText);
                        mainBullet.setFontSize(11);
                        mainBullet.setMarginLeft(25);
                        mainBullet.setMarginTop(3);
                        mainBullet.setMarginBottom(3);
                        document.add(mainBullet);
                    }

                    // NORMAL PARAGRAPH
                    else {
                        Paragraph para = new Paragraph(line);
                        para.setFontSize(11);
                        para.setTextAlignment(TextAlignment.JUSTIFIED);
                        para.setMarginBottom(8);
                        document.add(para);
                    }
                }
            }

            // ===== ACCEPTANCE CLAUSE WITH CHECKBOX =====
            document.add(new Paragraph("\n"));

            // Checkbox table: checkbox in left cell, acceptance text in right cell
            Table checkboxTable = new Table(UnitValue.createPercentArray(new float[]{5, 95}));
            checkboxTable.useAllAvailableWidth();
            checkboxTable.setMarginTop(15);
            checkboxTable.setMarginBottom(25);

            // Left cell - Checkbox (empty square as tick box)
            Cell checkboxCell = new Cell();
            checkboxCell.add(new Paragraph("☐").setFontSize(14));
            checkboxCell.setBorder(Border.NO_BORDER);
            checkboxCell.setPaddingTop(2);
            checkboxCell.setPaddingRight(5);
            checkboxCell.setVerticalAlignment(VerticalAlignment.TOP);

            // Right cell - Acceptance text
            Cell acceptanceCell = new Cell();
            Paragraph acceptance = new Paragraph(
                    "I confirm that I have read, understood, and accepted all the above declarations " +
                            "to ensure regulatory compliance and trusted participation on the TiaMeds Marketplace platform."
            );
            acceptance.setBold();
            acceptance.setFontSize(11);
            acceptance.setTextAlignment(TextAlignment.JUSTIFIED);
            acceptanceCell.add(acceptance);
            acceptanceCell.setBorder(Border.NO_BORDER);
            acceptanceCell.setPadding(0);

            checkboxTable.addCell(checkboxCell);
            checkboxTable.addCell(acceptanceCell);

            document.add(checkboxTable);

            // ===== SIGNATURE TABLE (COMMENTED OUT) =====
        /*
        Table signTable = new Table(UnitValue.createPercentArray(new float[]{50, 50}));
        signTable.useAllAvailableWidth();
        signTable.setMarginTop(20);

        // Left Cell
        Paragraph left1 = new Paragraph("For TiaMeds Marketplace");
        left1.setBold();
        left1.setFontSize(11);

        Cell leftCell = new Cell();
        leftCell.add(left1);
        leftCell.add(new Paragraph("Authorized Signatory").setFontSize(10));
        leftCell.add(new Paragraph("\n"));
        leftCell.add(new Paragraph("Signature: ___________________").setFontSize(10));
        leftCell.add(new Paragraph("\n"));
        leftCell.add(new Paragraph("Date: ___________________").setFontSize(10));
        leftCell.setBorder(Border.NO_BORDER);
        leftCell.setPadding(10);

        // Right Cell
        Paragraph right1 = new Paragraph("For Seller Company");
        right1.setBold();
        right1.setFontSize(11);

        Cell rightCell = new Cell();
        rightCell.add(right1);
        rightCell.add(new Paragraph("Authorized Representative").setFontSize(10));
        rightCell.add(new Paragraph("\n"));
        rightCell.add(new Paragraph("Signature: ___________________").setFontSize(10));
        rightCell.add(new Paragraph("\n"));
        rightCell.add(new Paragraph("Date: ___________________").setFontSize(10));
        rightCell.setBorder(Border.NO_BORDER);
        rightCell.setPadding(10);

        signTable.addCell(leftCell);
        signTable.addCell(rightCell);

        document.add(signTable);
        */

            document.close();
//            System.out.println("PDF generated successfully at: " + path);
            return path;

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Agreement PDF generation failed: " + e.getMessage(), e);
        }
    }

//    public String generateSellerAgreementPdf(Seller seller) {
//        try {
//            String fileName = "seller_" + seller.getId() + ".pdf";
//            String path = PDF_DIR + fileName;
//
//            PdfWriter writer = new PdfWriter(path);
//            PdfDocument pdf = new PdfDocument(writer);
//            Document document = new Document(pdf);
//
//            document.add(new Paragraph("SELLER AGREEMENT"));
//            document.add(new Paragraph("Name: " + seller.getName()));
//            document.add(new Paragraph("Email: " + seller.getEmail()));
//            document.add(new Paragraph("Phone: " + seller.getPhone()));
//            document.add(new Paragraph("Approved Date: " + LocalDate.now()));
//
//            document.close();
//            return path;
//
//        } catch (Exception e) {
//            throw new RuntimeException("PDF generation failed", e);
//        }
//    }
}
