package com.shoptracker.expense.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.shoptracker.expense.dto.BalanceSheet;
import com.shoptracker.expense.exception.ExcelStorageException;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.util.Locale;

/** Renders the balance sheet as a simple, printable PDF using OpenPDF. */
@Service
public class PdfExportService {

    private static final Color BRAND = new Color(0x1E, 0x40, 0xAF);

    public byte[] balanceSheetPdf(BalanceSheet bs) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document doc = new Document(PageSize.A4, 48, 48, 56, 48);
            PdfWriter.getInstance(doc, out);
            doc.open();

            Font titleFont = new Font(Font.HELVETICA, 20, Font.BOLD, BRAND);
            Paragraph title = new Paragraph("Balance Sheet", titleFont);
            title.setSpacingAfter(4);
            doc.add(title);

            Font meta = new Font(Font.HELVETICA, 10, Font.NORMAL, Color.GRAY);
            Paragraph date = new Paragraph("Generated on " + bs.getGeneratedOn(), meta);
            date.setSpacingAfter(18);
            doc.add(date);

            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{3, 2});

            addRow(table, "Opening Balance", bs.getOpeningBalance(), false);
            addRow(table, "Total Income", bs.getTotalIncome(), false);
            addRow(table, "Total Expense", bs.getTotalExpense(), false);
            addRow(table, "Outstanding Credit", bs.getOutstandingCredit(), false);
            addRow(table, "Current Balance", bs.getCurrentBalance(), true);

            doc.add(table);
            doc.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new ExcelStorageException("Failed to generate PDF", e);
        }
    }

    private void addRow(PdfPTable table, String label, double value, boolean highlight) {
        Font labelFont = new Font(Font.HELVETICA, 12, highlight ? Font.BOLD : Font.NORMAL);
        Font valueFont = new Font(Font.HELVETICA, 12, highlight ? Font.BOLD : Font.NORMAL,
                highlight ? BRAND : Color.BLACK);

        PdfPCell l = new PdfPCell(new Phrase(label, labelFont));
        PdfPCell v = new PdfPCell(new Phrase(String.format(Locale.US, "%,.2f", value), valueFont));
        v.setHorizontalAlignment(Element.ALIGN_RIGHT);

        int pad = 8;
        l.setPadding(pad);
        v.setPadding(pad);
        if (highlight) {
            Color fill = new Color(0xEF, 0xF6, 0xFF);
            l.setBackgroundColor(fill);
            v.setBackgroundColor(fill);
        }
        table.addCell(l);
        table.addCell(v);
    }
}
