package com.shoptracker.expense.controller;

import com.shoptracker.expense.dto.ApiResponse;
import com.shoptracker.expense.dto.BalanceSheet;
import com.shoptracker.expense.service.BalanceSheetService;
import com.shoptracker.expense.service.PdfExportService;
import com.shoptracker.expense.service.excel.ExcelStorageService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/balance-sheet")
public class BalanceSheetController {

    private final BalanceSheetService balanceSheetService;
    private final PdfExportService pdfExportService;
    private final ExcelStorageService storage;

    public BalanceSheetController(BalanceSheetService balanceSheetService,
                                  PdfExportService pdfExportService,
                                  ExcelStorageService storage) {
        this.balanceSheetService = balanceSheetService;
        this.pdfExportService = pdfExportService;
        this.storage = storage;
    }

    @GetMapping
    public ApiResponse<BalanceSheet> get() {
        return ApiResponse.ok(balanceSheetService.generate());
    }

    /** Downloads the full workbook (includes a Dashboard sheet with totals). */
    @GetMapping("/export/excel")
    public ResponseEntity<byte[]> exportExcel() {
        byte[] body = storage.exportWorkbookBytes();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=balance-sheet.xlsx")
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(body);
    }

    @GetMapping("/export/pdf")
    public ResponseEntity<byte[]> exportPdf() {
        byte[] body = pdfExportService.balanceSheetPdf(balanceSheetService.generate());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=balance-sheet.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(body);
    }
}
