package com.cps.mcp.budget.model;

import java.math.BigDecimal;

/**
 * Model representing a trip budget estimation.
 */
public class BudgetEstimate {
    private final String destination;
    private final int days;
    private final BigDecimal totalEstimate;
    private final BudgetBreakdown breakdown;

    public BudgetEstimate(String destination, int days, BigDecimal totalEstimate, BudgetBreakdown breakdown) {
        if (destination == null || destination.trim().isEmpty()) {
            throw new IllegalArgumentException("Destination cannot be null or empty");
        }
        if (days <= 0) {
            throw new IllegalArgumentException("Days must be positive: " + days);
        }
        if (totalEstimate == null) {
            throw new IllegalArgumentException("Total estimate cannot be null");
        }
        if (breakdown == null) {
            throw new IllegalArgumentException("Breakdown cannot be null");
        }
        this.destination = destination;
        this.days = days;
        this.totalEstimate = totalEstimate;
        this.breakdown = breakdown;
    }

    public String getDestination() {
        return destination;
    }

    public int getDays() {
        return days;
    }

    public BigDecimal getTotalEstimate() {
        return totalEstimate;
    }

    public BudgetBreakdown getBreakdown() {
        return breakdown;
    }

    @Override
    public String toString() {
        return "BudgetEstimate{destination='" + destination + "', days=" + days + 
                ", totalEstimate=" + totalEstimate + ", breakdown=" + breakdown + "}";
    }
}
