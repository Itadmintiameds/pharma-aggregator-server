package com.example.pharmaaggregatorserver.service;

import com.example.pharmaaggregatorserver.entity.temp.seller.TempSeller;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class PdfService {

    private static final String PDF_DIR = "/agreements/";

    public String generateTempSellerAgreementPdf(TempSeller seller) {
        try {
            String fileName = "seller_" + seller.getTempSellerId() + ".pdf";
            String path = PDF_DIR + fileName;

            PdfWriter writer = new PdfWriter(path);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            document.add(new Paragraph("SELLER AGREEMENT"));
            document.add(new Paragraph("Name: " + seller.getSellerName()));
            document.add(new Paragraph("Email: " + seller.getEmail()));
            document.add(new Paragraph("Phone: " + seller.getPhone()));
            document.add(new Paragraph("Approved Date: " + LocalDate.now()));

            document.close();
            return path;

        } catch (Exception e) {
            throw new RuntimeException("PDF generation failed", e);
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
