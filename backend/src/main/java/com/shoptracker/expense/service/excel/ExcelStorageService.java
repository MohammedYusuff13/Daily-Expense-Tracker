package com.shoptracker.expense.service.excel;

import com.shoptracker.expense.config.ExcelProperties;
import com.shoptracker.expense.exception.ExcelStorageException;
import com.shoptracker.expense.model.Buyer;
import com.shoptracker.expense.model.PaymentMode;
import com.shoptracker.expense.model.Transaction;
import com.shoptracker.expense.model.TransactionType;
import com.shoptracker.expense.util.PoiUtils;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

/**
 * The single source of truth for persistence. Loads the workbook into memory
 * at startup, and rewrites the full workbook after each mutation. Thread-safe
 * via a read/write lock.
 *
 * Workbook layout:
 *   Dashboard      - human readable summary (regenerated on every save)
 *   Buyers         - master list of buyers
 *   Transactions   - every transaction (global ledger)
 *   <Buyer Name>    - one tab per buyer: Date | Credit | Debit | Balance | Notes
 */
@Slf4j
@Service
public class ExcelStorageService {

    private final ExcelProperties props;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    // In-memory working set, kept in sync with the file.
    private final Map<String, Buyer> buyers = new LinkedHashMap<>();   // key = lowercase name
    private final List<Transaction> transactions = new ArrayList<>();

    public ExcelStorageService(ExcelProperties props) {
        this.props = props;
    }

    // ------------------------------------------------------------------
    // Startup
    // ------------------------------------------------------------------
    @PostConstruct
    public void init() {
        File file = new File(props.getFilePath());
        try {
            Files.createDirectories(Path.of(props.getFilePath()).toAbsolutePath().getParent());
            Files.createDirectories(Path.of(props.getBackupDir()));
        } catch (IOException e) {
            throw new ExcelStorageException("Could not create data directories", e);
        }

        if (file.exists()) {
            loadFromFile(file);
            log.info("Loaded {} buyers and {} transactions from {}",
                    buyers.size(), transactions.size(), file.getAbsolutePath());
        } else {
            log.info("No workbook found. Creating a fresh one at {}", file.getAbsolutePath());
            persist();
        }
    }

    // ------------------------------------------------------------------
    // Public read API (returns copies to keep callers from mutating state)
    // ------------------------------------------------------------------
    public List<Buyer> getAllBuyers() {
        lock.readLock().lock();
        try {
            recomputeBuyerBalances();
            return buyers.values().stream()
                    .map(this::copy)
                    .collect(Collectors.toList());
        } finally {
            lock.readLock().unlock();
        }
    }

    public Optional<Buyer> findBuyer(String name) {
        lock.readLock().lock();
        try {
            recomputeBuyerBalances();
            Buyer b = buyers.get(key(name));
            return Optional.ofNullable(b).map(this::copy);
        } finally {
            lock.readLock().unlock();
        }
    }

    public boolean buyerExists(String name) {
        lock.readLock().lock();
        try {
            return buyers.containsKey(key(name));
        } finally {
            lock.readLock().unlock();
        }
    }

    public List<Transaction> getAllTransactions() {
        lock.readLock().lock();
        try {
            return transactions.stream().map(this::copy).collect(Collectors.toList());
        } finally {
            lock.readLock().unlock();
        }
    }

    public Optional<Transaction> findTransaction(String id) {
        lock.readLock().lock();
        try {
            return transactions.stream()
                    .filter(t -> t.getId().equals(id))
                    .findFirst()
                    .map(this::copy);
        } finally {
            lock.readLock().unlock();
        }
    }

    // ------------------------------------------------------------------
    // Public write API
    // ------------------------------------------------------------------
    public Buyer addBuyer(Buyer buyer) {
        lock.writeLock().lock();
        try {
            buyers.put(key(buyer.getName()), buyer);
            persist();
            recomputeBuyerBalances();
            return copy(buyers.get(key(buyer.getName())));
        } finally {
            lock.writeLock().unlock();
        }
    }

    public Transaction addTransaction(Transaction txn) {
        lock.writeLock().lock();
        try {
            transactions.add(txn);
            persist();
            return copy(txn);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public Transaction updateTransaction(Transaction updated) {
        lock.writeLock().lock();
        try {
            for (int i = 0; i < transactions.size(); i++) {
                if (transactions.get(i).getId().equals(updated.getId())) {
                    transactions.set(i, updated);
                    persist();
                    return copy(updated);
                }
            }
            return null;
        } finally {
            lock.writeLock().unlock();
        }
    }

    public boolean deleteTransaction(String id) {
        lock.writeLock().lock();
        try {
            boolean removed = transactions.removeIf(t -> t.getId().equals(id));
            if (removed) persist();
            return removed;
        } finally {
            lock.writeLock().unlock();
        }
    }

    /** Replace the entire dataset from an uploaded workbook. */
    public void importWorkbook(InputStream in) {
        lock.writeLock().lock();
        try {
            backup();
            buyers.clear();
            transactions.clear();
            try (Workbook wb = WorkbookFactory.create(in)) {
                readWorkbook(wb);
            }
            persist();
        } catch (IOException e) {
            throw new ExcelStorageException("Failed to import workbook", e);
        } finally {
            lock.writeLock().unlock();
        }
    }

    /** Returns the current workbook as a byte array for download. */
    public byte[] exportWorkbookBytes() {
        lock.readLock().lock();
        try {
            recomputeBuyerBalances();
            try (Workbook wb = buildWorkbook();
                 ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                wb.write(out);
                return out.toByteArray();
            }
        } catch (IOException e) {
            throw new ExcelStorageException("Failed to export workbook", e);
        } finally {
            lock.readLock().unlock();
        }
    }

    public String backup() {
        if (!props.isAutoBackup()) return null;
        File source = new File(props.getFilePath());
        if (!source.exists()) return null;
        String stamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
        Path target = Path.of(props.getBackupDir(), "backup-" + stamp + ".xlsx");
        try {
            Files.copy(source.toPath(), target, StandardCopyOption.REPLACE_EXISTING);
            log.info("Backup written to {}", target);
            return target.toString();
        } catch (IOException e) {
            log.warn("Backup failed: {}", e.getMessage());
            return null;
        }
    }

    // ------------------------------------------------------------------
    // Persistence
    // ------------------------------------------------------------------
    private void persist() {
        backup();
        recomputeBuyerBalances();
        File file = new File(props.getFilePath());
        try (Workbook wb = buildWorkbook();
             FileOutputStream out = new FileOutputStream(file)) {
            wb.write(out);
        } catch (IOException e) {
            throw new ExcelStorageException("Failed to write workbook to disk", e);
        }
    }

    private Workbook buildWorkbook() {
        XSSFWorkbook wb = new XSSFWorkbook();
        CellStyle headerStyle = headerStyle(wb);
        CellStyle dateStyle = dateStyle(wb);
        CellStyle moneyStyle = moneyStyle(wb);

        writeDashboardSheet(wb, headerStyle, moneyStyle);
        writeBuyersSheet(wb, headerStyle, moneyStyle);
        writeTransactionsSheet(wb, headerStyle, dateStyle, moneyStyle);
        writeBuyerSheets(wb, headerStyle, dateStyle, moneyStyle);
        return wb;
    }

    private void writeDashboardSheet(Workbook wb, CellStyle header, CellStyle money) {
        Sheet sheet = wb.createSheet(ExcelSchema.SHEET_DASHBOARD);
        double credit = transactions.stream().filter(t -> t.getType() == TransactionType.CREDIT)
                .mapToDouble(Transaction::getAmount).sum();
        double debit = transactions.stream().filter(t -> t.getType() == TransactionType.DEBIT)
                .mapToDouble(Transaction::getAmount).sum();
        double outstanding = buyers.values().stream()
                .mapToDouble(Buyer::getCurrentBalance).filter(v -> v > 0).sum();

        String[][] rows = {
                {"Metric", "Value"},
                {"Total Income (Credit)", fmt(credit)},
                {"Total Expense (Debit)", fmt(debit)},
                {"Current Balance", fmt(credit - debit)},
                {"Outstanding Credit", fmt(outstanding)},
                {"Total Buyers", String.valueOf(buyers.size())},
                {"Total Transactions", String.valueOf(transactions.size())},
                {"Generated", LocalDateTime.now().toString()}
        };
        for (int r = 0; r < rows.length; r++) {
            Row row = sheet.createRow(r);
            for (int c = 0; c < rows[r].length; c++) {
                Cell cell = row.createCell(c);
                cell.setCellValue(rows[r][c]);
                if (r == 0) cell.setCellStyle(header);
            }
        }
        sheet.setColumnWidth(0, 8000);
        sheet.setColumnWidth(1, 6000);
    }

    private void writeBuyersSheet(Workbook wb, CellStyle header, CellStyle money) {
        Sheet sheet = wb.createSheet(ExcelSchema.SHEET_BUYERS);
        Row head = sheet.createRow(0);
        for (int c = 0; c < ExcelSchema.BUYER_HEADERS.length; c++) {
            Cell cell = head.createCell(c);
            cell.setCellValue(ExcelSchema.BUYER_HEADERS[c]);
            cell.setCellStyle(header);
        }
        int r = 1;
        for (Buyer b : buyers.values()) {
            Row row = sheet.createRow(r++);
            row.createCell(0).setCellValue(b.getName());
            row.createCell(1).setCellValue(b.getMobile() == null ? "" : b.getMobile());
            row.createCell(2).setCellValue(b.getAddress() == null ? "" : b.getAddress());
            setMoney(row.createCell(3), b.getOpeningBalance(), money);
            setMoney(row.createCell(4), b.getCurrentBalance(), money);
        }
        for (int c = 0; c < ExcelSchema.BUYER_HEADERS.length; c++) sheet.setColumnWidth(c, 5000);
    }

    private void writeTransactionsSheet(Workbook wb, CellStyle header, CellStyle date, CellStyle money) {
        Sheet sheet = wb.createSheet(ExcelSchema.SHEET_TRANSACTIONS);
        Row head = sheet.createRow(0);
        for (int c = 0; c < ExcelSchema.TXN_HEADERS.length; c++) {
            Cell cell = head.createCell(c);
            cell.setCellValue(ExcelSchema.TXN_HEADERS[c]);
            cell.setCellStyle(header);
        }
        int r = 1;
        for (Transaction t : transactions) {
            Row row = sheet.createRow(r++);
            row.createCell(0).setCellValue(t.getId());
            setDate(row.createCell(1), t.getDate(), date);
            row.createCell(2).setCellValue(t.getBuyerName() == null ? "" : t.getBuyerName());
            row.createCell(3).setCellValue(t.getType().name());
            setMoney(row.createCell(4), t.getAmount(), money);
            row.createCell(5).setCellValue(t.getDescription() == null ? "" : t.getDescription());
            row.createCell(6).setCellValue(t.getPaymentMode() == null ? "" : t.getPaymentMode().name());
        }
        for (int c = 0; c < ExcelSchema.TXN_HEADERS.length; c++) sheet.setColumnWidth(c, 5000);
    }

    /** One worksheet per buyer, running balance in "Date | Credit | Debit | Balance | Notes". */
    private void writeBuyerSheets(Workbook wb, CellStyle header, CellStyle date, CellStyle money) {
        for (Buyer b : buyers.values()) {
            String sheetName = uniqueSheetName(wb, PoiUtils.sanitizeSheetName(b.getName()));
            Sheet sheet = wb.createSheet(sheetName);

            Row head = sheet.createRow(0);
            for (int c = 0; c < ExcelSchema.BUYER_SHEET_HEADERS.length; c++) {
                Cell cell = head.createCell(c);
                cell.setCellValue(ExcelSchema.BUYER_SHEET_HEADERS[c]);
                cell.setCellStyle(header);
            }

            // Opening balance row.
            double running = b.getOpeningBalance();
            Row opening = sheet.createRow(1);
            opening.createCell(0).setCellValue("Opening");
            opening.createCell(1).setCellValue("");
            opening.createCell(2).setCellValue("");
            setMoney(opening.createCell(3), running, money);
            opening.createCell(4).setCellValue("Opening balance");

            List<Transaction> buyerTxns = transactions.stream()
                    .filter(t -> b.getName().equalsIgnoreCase(t.getBuyerName()))
                    .sorted(Comparator.comparing(Transaction::getDate,
                            Comparator.nullsLast(Comparator.naturalOrder())))
                    .toList();

            int r = 2;
            for (Transaction t : buyerTxns) {
                double credit = t.getType() == TransactionType.CREDIT ? t.getAmount() : 0;
                double debit = t.getType() == TransactionType.DEBIT ? t.getAmount() : 0;
                running += credit - debit;

                Row row = sheet.createRow(r++);
                setDate(row.createCell(0), t.getDate(), date);
                setMoney(row.createCell(1), credit, money);
                setMoney(row.createCell(2), debit, money);
                setMoney(row.createCell(3), running, money);
                row.createCell(4).setCellValue(t.getDescription() == null ? "" : t.getDescription());
            }
            for (int c = 0; c < ExcelSchema.BUYER_SHEET_HEADERS.length; c++) sheet.setColumnWidth(c, 4500);
        }
    }

    // ------------------------------------------------------------------
    // Loading
    // ------------------------------------------------------------------
    private void loadFromFile(File file) {
        try (InputStream in = new FileInputStream(file);
             Workbook wb = WorkbookFactory.create(in)) {
            readWorkbook(wb);
        } catch (IOException e) {
            throw new ExcelStorageException("Failed to read workbook at startup", e);
        }
    }

    private void readWorkbook(Workbook wb) {
        // Buyers master
        Sheet buyerSheet = wb.getSheet(ExcelSchema.SHEET_BUYERS);
        if (buyerSheet != null) {
            for (int r = 1; r <= buyerSheet.getLastRowNum(); r++) {
                Row row = buyerSheet.getRow(r);
                if (row == null) continue;
                String name = PoiUtils.getString(row, 0);
                if (name.isEmpty()) continue;
                Buyer b = Buyer.builder()
                        .name(name)
                        .mobile(PoiUtils.getString(row, 1))
                        .address(PoiUtils.getString(row, 2))
                        .openingBalance(PoiUtils.getNumber(row, 3))
                        .build();
                buyers.put(key(name), b);
            }
        }

        // Global transactions
        Sheet txnSheet = wb.getSheet(ExcelSchema.SHEET_TRANSACTIONS);
        if (txnSheet != null) {
            for (int r = 1; r <= txnSheet.getLastRowNum(); r++) {
                Row row = txnSheet.getRow(r);
                if (row == null) continue;
                String id = PoiUtils.getString(row, 0);
                if (id.isEmpty()) id = UUID.randomUUID().toString();
                LocalDate d = PoiUtils.getDate(row, 1);
                if (d == null) continue;
                Transaction t = Transaction.builder()
                        .id(id)
                        .date(d)
                        .buyerName(PoiUtils.getString(row, 2))
                        .type(parseType(PoiUtils.getString(row, 3)))
                        .amount(PoiUtils.getNumber(row, 4))
                        .description(PoiUtils.getString(row, 5))
                        .paymentMode(parseMode(PoiUtils.getString(row, 6)))
                        .build();
                transactions.add(t);
            }
        }
        recomputeBuyerBalances();
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------
    private void recomputeBuyerBalances() {
        for (Buyer b : buyers.values()) {
            double running = b.getOpeningBalance();
            for (Transaction t : transactions) {
                if (b.getName().equalsIgnoreCase(t.getBuyerName())) {
                    running += (t.getType() == TransactionType.CREDIT ? t.getAmount() : -t.getAmount());
                }
            }
            b.setCurrentBalance(running);
        }
    }

    private String uniqueSheetName(Workbook wb, String base) {
        String candidate = base;
        int i = 2;
        while (wb.getSheet(candidate) != null || ExcelSchema.isReserved(candidate)) {
            String suffix = " (" + i++ + ")";
            int max = 31 - suffix.length();
            candidate = (base.length() > max ? base.substring(0, max) : base) + suffix;
        }
        return candidate;
    }

    private TransactionType parseType(String s) {
        try {
            return TransactionType.valueOf(s.trim().toUpperCase());
        } catch (Exception e) {
            return TransactionType.DEBIT;
        }
    }

    private PaymentMode parseMode(String s) {
        try {
            return PaymentMode.valueOf(s.trim().toUpperCase());
        } catch (Exception e) {
            return PaymentMode.CASH;
        }
    }

    private String key(String name) {
        return name == null ? "" : name.trim().toLowerCase();
    }

    private String fmt(double v) {
        return String.format(Locale.US, "%.2f", v);
    }

    private void setMoney(Cell cell, double v, CellStyle style) {
        cell.setCellValue(v);
        cell.setCellStyle(style);
    }

    private void setDate(Cell cell, LocalDate d, CellStyle style) {
        if (d != null) {
            cell.setCellValue(java.sql.Date.valueOf(d));
            cell.setCellStyle(style);
        }
    }

    private CellStyle headerStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private CellStyle dateStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        style.setDataFormat(wb.createDataFormat().getFormat("yyyy-mm-dd"));
        return style;
    }

    private CellStyle moneyStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        style.setDataFormat(wb.createDataFormat().getFormat("#,##0.00"));
        return style;
    }

    private Buyer copy(Buyer b) {
        return Buyer.builder()
                .name(b.getName()).mobile(b.getMobile()).address(b.getAddress())
                .openingBalance(b.getOpeningBalance()).currentBalance(b.getCurrentBalance())
                .build();
    }

    private Transaction copy(Transaction t) {
        return Transaction.builder()
                .id(t.getId()).date(t.getDate()).buyerName(t.getBuyerName())
                .type(t.getType()).amount(t.getAmount()).description(t.getDescription())
                .paymentMode(t.getPaymentMode()).balanceAfter(t.getBalanceAfter())
                .build();
    }
}
