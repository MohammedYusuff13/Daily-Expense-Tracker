package com.shoptracker.expense.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/** One line of the outstanding balance report. */
@Data
@AllArgsConstructor
public class OutstandingRow {
    private String buyerName;
    private String mobile;
    private double outstanding;
}
