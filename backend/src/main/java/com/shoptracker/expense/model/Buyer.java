package com.shoptracker.expense.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A buyer / customer. Each buyer owns a dedicated worksheet named after them.
 * The {@code currentBalance} is the outstanding amount (positive means the
 * buyer owes the shop money).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Buyer {

    private String name;
    private String mobile;
    private String address;
    private double openingBalance;

    /** Derived: opening balance +/- all buyer transactions. */
    private double currentBalance;
}
