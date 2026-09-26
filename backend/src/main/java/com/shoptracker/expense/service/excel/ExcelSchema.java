package com.shoptracker.expense.service.excel;

/** Central place for sheet names and column layouts so readers/writers agree. */
public final class ExcelSchema {

    private ExcelSchema() {}

    // ---- Reserved sheet names ----
    public static final String SHEET_DASHBOARD = "Dashboard";
    public static final String SHEET_BUYERS = "Buyers";
    public static final String SHEET_TRANSACTIONS = "Transactions";

    /** Sheets we never treat as buyer sheets. */
    public static final String[] RESERVED_SHEETS = {
            SHEET_DASHBOARD, SHEET_BUYERS, SHEET_TRANSACTIONS
    };

    // ---- Buyers master sheet columns ----
    public static final String[] BUYER_HEADERS = {
            "Name", "Mobile", "Address", "Opening Balance", "Current Balance"
    };

    // ---- Global Transactions sheet columns ----
    public static final String[] TXN_HEADERS = {
            "Id", "Date", "Buyer Name", "Type", "Amount", "Description", "Payment Mode"
    };

    // ---- Per-buyer sheet columns (as required by the brief) ----
    public static final String[] BUYER_SHEET_HEADERS = {
            "Date", "Credit", "Debit", "Balance", "Notes"
    };

    public static boolean isReserved(String sheetName) {
        for (String s : RESERVED_SHEETS) {
            if (s.equalsIgnoreCase(sheetName)) return true;
        }
        return false;
    }
}
