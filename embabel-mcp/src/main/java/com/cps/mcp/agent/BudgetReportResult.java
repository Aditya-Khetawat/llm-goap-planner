package com.cps.mcp.agent;

import com.cps.mcp.budget.model.BudgetEstimate;
import com.cps.mcp.budget.model.BudgetItem;
import java.math.BigDecimal;

/**
 * Result DTO for budget-only queries.
 * Wraps budget estimate information for user-facing output.
 */
public class BudgetReportResult {
    private final BudgetEstimate estimate;
    private final String destination;

    public BudgetReportResult(BudgetEstimate estimate, String destination) {
        this.estimate = estimate;
        this.destination = destination;
    }

    public BudgetEstimate getEstimate() {
        return estimate;
    }

    public String getDestination() {
        return destination;
    }

    public String formatContent() {
        if (estimate == null) {
            return "Unable to calculate budget estimate.";
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("==================================================\n");
        sb.append("BUDGET ESTIMATE: ").append(destination).append("\n");
        sb.append("==================================================\n\n");
        
        int days = estimate.getDays();
        BigDecimal total = estimate.getTotalEstimate();
        
        sb.append("Trip Duration: ").append(days).append(" day").append(days > 1 ? "s" : "").append("\n");
        sb.append("Total Estimated Cost: $").append(total).append("\n");
        sb.append("Cost per Day: $").append(total.divide(BigDecimal.valueOf(days), 2, java.math.RoundingMode.HALF_UP)).append("\n\n");
        
        sb.append("Budget Breakdown:\n");
        sb.append("--------------------------------------------------\n");
        for (BudgetItem item : estimate.getBreakdown().getItems()) {
            BigDecimal itemTotal = item.getAmount();
            BigDecimal perDay = itemTotal.divide(BigDecimal.valueOf(days), 2, java.math.RoundingMode.HALF_UP);
            sb.append(String.format("%-15s: $%-10s ($%.2f/day)\n", item.getName(), itemTotal, perDay));
        }
        
        return sb.toString();
    }

    @Override
    public String toString() {
        return formatContent();
    }
}
