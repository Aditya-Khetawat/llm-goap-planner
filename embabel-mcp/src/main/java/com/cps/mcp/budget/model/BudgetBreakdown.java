package com.cps.mcp.budget.model;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

/**
 * Model representing an aggregation of budget items and their calculated total.
 */
public class BudgetBreakdown {
    private final List<BudgetItem> items;
    private final BigDecimal total;

    public BudgetBreakdown(List<BudgetItem> items, BigDecimal total) {
        if (items == null) {
            throw new IllegalArgumentException("Items list cannot be null");
        }
        if (total == null) {
            throw new IllegalArgumentException("Total amount cannot be null");
        }
        this.items = List.copyOf(items);
        this.total = total;
    }

    public List<BudgetItem> getItems() {
        return items;
    }

    public BigDecimal getTotal() {
        return total;
    }

    @Override
    public String toString() {
        return "BudgetBreakdown{items=" + items + ", total=" + total + "}";
    }
}
