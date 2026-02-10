package com.example.pharmaaggregatorserver.service;

import com.example.pharmaaggregatorserver.entity.temp.seller.TempSeller;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDate;

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
