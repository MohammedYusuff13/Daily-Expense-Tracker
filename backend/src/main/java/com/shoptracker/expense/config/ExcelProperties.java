package com.shoptracker.expense.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/** Binds the app.excel.* settings from application.properties. */
@Data
@Component
@ConfigurationProperties(prefix = "app.excel")
public class ExcelProperties {
    private String filePath;
    private String backupDir;
    private boolean autoBackup = true;
}
