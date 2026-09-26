package com.shoptracker.expense.controller;

import com.shoptracker.expense.dto.ApiResponse;
import com.shoptracker.expense.service.excel.ExcelStorageService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/** Backup, import, and full-workbook export. */
@RestController
@RequestMapping("/api/data")
public class DataController {

    private final ExcelStorageService storage;

    public DataController(ExcelStorageService storage) {
        this.storage = storage;
    }

    @PostMapping("/backup")
    public ApiResponse<String> backup() {
        String path = storage.backup();
        return path == null
                ? ApiResponse.ok("Nothing to back up or auto-backup disabled", null)
                : ApiResponse.ok("Backup created", path);
    }

    @PostMapping("/import")
    public ApiResponse<Void> importWorkbook(@RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            return ApiResponse.fail("No file provided");
        }
        storage.importWorkbook(file.getInputStream());
        return ApiResponse.ok("Workbook imported. Existing data was backed up and replaced.", null);
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> export() {
        byte[] body = storage.exportWorkbookBytes();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=expense-tracker.xlsx")
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(body);
    }
}
