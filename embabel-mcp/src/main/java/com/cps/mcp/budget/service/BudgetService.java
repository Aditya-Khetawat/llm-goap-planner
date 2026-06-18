package com.cps.mcp.budget.service;

import com.cps.mcp.budget.model.BudgetBreakdown;
import com.cps.mcp.budget.model.BudgetEstimate;
import com.cps.mcp.budget.model.BudgetItem;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for performing deterministic budget and cost calculations.
 * Completely decoupled from LLM or MCP infrastructure.
 */
@Service
public class BudgetService {

    public static final BigDecimal MAX_AMOUNT = new BigDecimal("1000000000000000"); // 1 Quadrillion limit for overflow checks

    public void validateAmount(BigDecimal amount, String name) {
        if (amount == null) {
            throw new IllegalArgumentException(name + " cannot be null");
        }
        
        try {
            double d = amount.doubleValue();
            if (Double.isNaN(d)) {
                throw new IllegalArgumentException(name + " cannot be NaN");
            }
            if (Double.isInfinite(d)) {
                throw new IllegalArgumentException(name + " cannot be Infinity");
            }
        } catch (NumberFormatException | ArithmeticException e) {
            throw new IllegalArgumentException(name + " is invalid or has overflowed", e);
        }

        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(name + " cannot be negative: " + amount);
        }
        
        if (amount.compareTo(MAX_AMOUNT) > 0) {
            throw new IllegalArgumentException(name + " exceeds maximum limit (overflow): " + amount);
        }
    }

    public BigDecimal calculateTotal(List<BudgetItem> items) {
        if (items == null) {
            throw new IllegalArgumentException("Items list cannot be null");
        }
        if (items.isEmpty()) {
            throw new IllegalArgumentException("Items list cannot be empty");
        }

        BigDecimal sum = BigDecimal.ZERO;
        for (BudgetItem item : items) {
            if (item == null) {
                throw new IllegalArgumentException("Budget item cannot be null");
            }
            validateAmount(item.getAmount(), "Item amount ('" + item.getName() + "')");
            sum = sum.add(item.getAmount());
            if (sum.compareTo(MAX_AMOUNT) > 0) {
                throw new IllegalArgumentException("Total sum exceeds maximum limit (overflow)");
            }
        }
        return sum;
    }

    public BigDecimal remainingBudget(BigDecimal budget, BigDecimal spent) {
        validateAmount(budget, "Initial budget");
        validateAmount(spent, "Spent amount");
        
        BigDecimal remaining = budget.subtract(spent);
        // remaining budget can be negative (overspent), which is valid,
        // but we should check if its absolute value exceeds MAX_AMOUNT (overflow).
        if (remaining.abs().compareTo(MAX_AMOUNT) > 0) {
            throw new IllegalArgumentException("Remaining budget calculation overflow");
        }
        return remaining;
    }

    public BudgetBreakdown getExpenseBreakdown(List<BudgetItem> items) {
        if (items == null) {
            throw new IllegalArgumentException("Items list cannot be null");
        }
        if (items.isEmpty()) {
            throw new IllegalArgumentException("Items list cannot be empty");
        }
        BigDecimal total = calculateTotal(items);
        return new BudgetBreakdown(items, total);
    }

    public BudgetEstimate estimateTripBudget(String destination, int days, 
                                             BigDecimal hotelPerDay, BigDecimal foodPerDay, 
                                             BigDecimal transportPerDay, BigDecimal miscPerDay) {
        if (destination == null || destination.trim().isEmpty()) {
            throw new IllegalArgumentException("Destination cannot be null or empty");
        }
        if (days <= 0) {
            throw new IllegalArgumentException("Number of days must be positive: " + days);
        }
        if (days > 100000) {
            throw new IllegalArgumentException("Number of days is too large (overflow): " + days);
        }

        validateAmount(hotelPerDay, "Hotel cost per day");
        validateAmount(foodPerDay, "Food cost per day");
        validateAmount(transportPerDay, "Transport cost per day");
        validateAmount(miscPerDay, "Misc cost per day");

        BigDecimal daysBD = BigDecimal.valueOf(days);
        BigDecimal hotelTotal = hotelPerDay.multiply(daysBD);
        BigDecimal foodTotal = foodPerDay.multiply(daysBD);
        BigDecimal transportTotal = transportPerDay.multiply(daysBD);
        BigDecimal miscTotal = miscPerDay.multiply(daysBD);

        validateAmount(hotelTotal, "Total hotel cost");
        validateAmount(foodTotal, "Total food cost");
        validateAmount(transportTotal, "Total transport cost");
        validateAmount(miscTotal, "Total misc cost");

        List<BudgetItem> items = new ArrayList<>();
        items.add(new BudgetItem("Hotel", hotelTotal));
        items.add(new BudgetItem("Food", foodTotal));
        items.add(new BudgetItem("Transport", transportTotal));
        items.add(new BudgetItem("Misc", miscTotal));

        BigDecimal total = calculateTotal(items);
        BudgetBreakdown breakdown = new BudgetBreakdown(items, total);

        return new BudgetEstimate(destination, days, total, breakdown);
    }
}
