package com.cps.mcp.budget.model;

import java.math.BigDecimal;

/**
 * Model representing a single budget item with a name and a cost amount.
 */
public class BudgetItem {
    private final String name;
    private final BigDecimal amount;

    public static final BigDecimal MAX_AMOUNT = new BigDecimal("1000000000000000");

    public BudgetItem(String name, BigDecimal amount) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Item name cannot be null or empty");
        }
        if (amount == null) {
            throw new IllegalArgumentException("Item amount cannot be null");
        }
        try {
            double d = amount.doubleValue();
            if (Double.isNaN(d)) {
                throw new IllegalArgumentException("Item amount cannot be NaN");
            }
            if (Double.isInfinite(d)) {
                throw new IllegalArgumentException("Item amount cannot be Infinity");
            }
        } catch (NumberFormatException | ArithmeticException e) {
            throw new IllegalArgumentException("Item amount is invalid or has overflowed", e);
        }
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Item amount cannot be negative: " + amount);
        }
        if (amount.compareTo(MAX_AMOUNT) > 0) {
            throw new IllegalArgumentException("Item amount exceeds limit (overflow): " + amount);
        }
        this.name = name;
        this.amount = amount;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    @Override
    public String toString() {
        return "BudgetItem{name='" + name + "', amount=" + amount + "}";
    }
}
