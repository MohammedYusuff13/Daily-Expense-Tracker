package com.shoptracker.expense.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/** Incoming payload for creating a new buyer. */
@Data
public class BuyerRequest {

    @NotBlank(message = "Buyer name is required")
    private String name;

    @Pattern(regexp = "^$|^[0-9+\\-\\s]{6,15}$", message = "Mobile number is not valid")
    private String mobile;

    private String address;

    private double openingBalance;
}
