package com.cps.mcp.tool;

import com.embabel.agent.core.ToolGroup;
import com.embabel.agent.core.ToolGroupMetadata;
import com.embabel.agent.api.tool.Tool;
import com.cps.mcp.budget.model.BudgetBreakdown;
import com.cps.mcp.budget.model.BudgetEstimate;
import com.cps.mcp.budget.model.BudgetItem;
import com.cps.mcp.budget.service.BudgetService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Production Budget Tool Group implementing Embabel's ToolGroup interface.
 * Exposes calculate_total, remaining_budget, expense_breakdown, and estimate_trip_budget tools.
 */
@Component
public class BudgetToolGroup implements ToolGroup {

    private static final Logger logger = LoggerFactory.getLogger(BudgetToolGroup.class);
    private final ToolGroupMetadata metadata;
    private final BudgetService budgetService;

    public static class ItemRequest {
        public String name;
        public BigDecimal amount;

        // Default constructor for Jackson
        public ItemRequest() {}

        public ItemRequest(String name, BigDecimal amount) {
            this.name = name;
            this.amount = amount;
        }
    }

    public static class CalculateTotalRequest {
        public List<ItemRequest> items;

        public CalculateTotalRequest() {}

        public CalculateTotalRequest(List<ItemRequest> items) {
            this.items = items;
        }
    }

    public static class RemainingBudgetRequest {
        public BigDecimal budget;
        public BigDecimal spent;

        public RemainingBudgetRequest() {}

        public RemainingBudgetRequest(BigDecimal budget, BigDecimal spent) {
            this.budget = budget;
            this.spent = spent;
        }
    }

    public static class ExpenseBreakdownRequest {
        public List<ItemRequest> items;

        public ExpenseBreakdownRequest() {}

        public ExpenseBreakdownRequest(List<ItemRequest> items) {
            this.items = items;
        }
    }

    public static class EstimateTripBudgetRequest {
        public String destination;
        public Integer days;
        public BigDecimal hotel_per_day;
        public BigDecimal food_per_day;
        public BigDecimal transport_per_day;
        public BigDecimal misc_per_day;

        public EstimateTripBudgetRequest() {}

        public EstimateTripBudgetRequest(String destination, Integer days, BigDecimal hotel_per_day,
                                         BigDecimal food_per_day, BigDecimal transport_per_day, BigDecimal misc_per_day) {
            this.destination = destination;
            this.days = days;
            this.hotel_per_day = hotel_per_day;
            this.food_per_day = food_per_day;
            this.transport_per_day = transport_per_day;
            this.misc_per_day = misc_per_day;
        }
    }

    public BudgetToolGroup(BudgetService budgetService) {
        this.budgetService = budgetService;

        // We use java.lang.reflect.Proxy to implement ToolGroupMetadata dynamically 
        // to bypass Kotlin inline-class compiled name-mangling for getVersion-Id9oKnY()
        this.metadata = (ToolGroupMetadata) Proxy.newProxyInstance(
                ToolGroupMetadata.class.getClassLoader(),
                new Class<?>[]{ToolGroupMetadata.class},
                (proxy, method, args) -> {
                    switch (method.getName()) {
                        case "getRole":
                            return "budget";
                        case "getDescription":
                            return "Exposes deterministic budget and trip estimation calculations";
                        case "getName":
                            return "BudgetToolGroup";
                        case "getProvider":
                            return "local";
                        case "getPermissions":
                            return Collections.emptySet();
                        case "infoString":
                            return "BudgetToolGroup 1.0.0";
                        case "toString":
                            return "BudgetToolGroup";
                        default:
                            if (method.getName().contains("getVersion")) {
                                return "1.0.0";
                            }
                            return null;
                    }
                }
        );
    }

    @Override
    public ToolGroupMetadata getMetadata() {
        return metadata;
    }

    @Override
    public List<Tool> getTools() {
        Tool calculateTotalTool = Tool.fromFunction(
                "calculate_total",
                "Calculates the total sum of multiple budget items",
                CalculateTotalRequest.class,
                String.class,
                req -> {
                    logTraceStart("calculate_total");
                    try {
                        validateItemsRequest(req);
                        List<BudgetItem> items = mapItems(req.items);
                        BigDecimal total = budgetService.calculateTotal(items);
                        logTraceCalc("calculate_total", total.toString());
                        String res = "Total sum: " + total.toString();
                        logTraceCompleted("calculate_total");
                        return res;
                    } catch (Exception e) {
                        logger.error("Budget Tool: calculate_total failed: {}", e.getMessage());
                        throw new RuntimeException("calculate_total failed: " + e.getMessage(), e);
                    }
                }
        );

        Tool remainingBudgetTool = Tool.fromFunction(
                "remaining_budget",
                "Calculates the remaining budget after expenses",
                RemainingBudgetRequest.class,
                String.class,
                req -> {
                    logTraceStart("remaining_budget");
                    try {
                        if (req == null) {
                            throw new IllegalArgumentException("Request body cannot be null");
                        }
                        BigDecimal remaining = budgetService.remainingBudget(req.budget, req.spent);
                        logTraceCalc("remaining_budget", remaining.toString());
                        String res = "Remaining budget: " + remaining.toString();
                        logTraceCompleted("remaining_budget");
                        return res;
                    } catch (Exception e) {
                        logger.error("Budget Tool: remaining_budget failed: {}", e.getMessage());
                        throw new RuntimeException("remaining_budget failed: " + e.getMessage(), e);
                    }
                }
        );

        Tool expenseBreakdownTool = Tool.fromFunction(
                "expense_breakdown",
                "Returns a structured breakdown of categories and totals",
                ExpenseBreakdownRequest.class,
                String.class,
                req -> {
                    logTraceStart("expense_breakdown");
                    try {
                        validateItemsRequest(req);
                        List<BudgetItem> items = mapItems(req.items);
                        BudgetBreakdown breakdown = budgetService.getExpenseBreakdown(items);
                        logTraceCalc("expense_breakdown", breakdown.getTotal().toString());
                        String res = formatBreakdown(breakdown);
                        logTraceCompleted("expense_breakdown");
                        return res;
                    } catch (Exception e) {
                        logger.error("Budget Tool: expense_breakdown failed: {}", e.getMessage());
                        throw new RuntimeException("expense_breakdown failed: " + e.getMessage(), e);
                    }
                }
        );

        Tool estimateTripBudgetTool = Tool.fromFunction(
                "estimate_trip_budget",
                "Estimates the budget for a trip based on daily costs",
                EstimateTripBudgetRequest.class,
                String.class,
                req -> {
                    logTraceStart("estimate_trip_budget");
                    try {
                        validateEstimateRequest(req);
                        BudgetEstimate estimate = budgetService.estimateTripBudget(
                                req.destination, req.days, req.hotel_per_day,
                                req.food_per_day, req.transport_per_day, req.misc_per_day
                        );
                        logTraceCalc("estimate_trip_budget", estimate.getTotalEstimate().toString());
                        String res = formatEstimate(estimate);
                        logTraceCompleted("estimate_trip_budget");
                        return res;
                    } catch (Exception e) {
                        logger.error("Budget Tool: estimate_trip_budget failed: {}", e.getMessage());
                        throw new RuntimeException("estimate_trip_budget failed: " + e.getMessage(), e);
                    }
                }
        );

        return List.of(calculateTotalTool, remainingBudgetTool, expenseBreakdownTool, estimateTripBudgetTool);
    }

    private void logTraceStart(String toolName) {
        logger.info("Agent: Dispatching request to budget tool group");
        logger.info("Planner: Triggering budget tool function '{}'", toolName);
        logger.info("Budget Tool: Processing input parameters");
    }

    private void logTraceCalc(String toolName, String calculationResult) {
        logger.info("Calculation: Performing deterministic arithmetic computation for '{}'", toolName);
        logger.info("Result: Calculation successful. Value = {}", calculationResult);
    }

    private void logTraceCompleted(String toolName) {
        logger.info("Goal Completed: Budget tool function '{}' execution successfully finished", toolName);
    }

    private void validateItemsRequest(CalculateTotalRequest req) {
        if (req == null || req.items == null) {
            throw new IllegalArgumentException("Items list is missing");
        }
    }

    private void validateItemsRequest(ExpenseBreakdownRequest req) {
        if (req == null || req.items == null) {
            throw new IllegalArgumentException("Items list is missing");
        }
    }

    private void validateEstimateRequest(EstimateTripBudgetRequest req) {
        if (req == null) {
            throw new IllegalArgumentException("Request body is missing");
        }
        if (req.days == null) {
            throw new IllegalArgumentException("Number of days is missing");
        }
        if (req.hotel_per_day == null || req.food_per_day == null || req.transport_per_day == null || req.misc_per_day == null) {
            throw new IllegalArgumentException("Cost parameter is missing");
        }
    }

    private List<BudgetItem> mapItems(List<ItemRequest> reqItems) {
        List<BudgetItem> items = new ArrayList<>();
        for (ItemRequest item : reqItems) {
            if (item == null) {
                throw new IllegalArgumentException("Budget item cannot be null");
            }
            items.add(new BudgetItem(item.name, item.amount));
        }
        return items;
    }

    private String formatBreakdown(BudgetBreakdown breakdown) {
        String itemsStr = breakdown.getItems().stream()
                .map(i -> String.format("- Category: %s, Amount: %s", i.getName(), i.getAmount().toString()))
                .collect(Collectors.joining("\n"));
        return String.format("Expense Breakdown:\n%s\nTotal Sum: %s", itemsStr, breakdown.getTotal().toString());
    }

    private String formatEstimate(BudgetEstimate estimate) {
        String breakdownStr = estimate.getBreakdown().getItems().stream()
                .map(i -> String.format("  * %s = %s", i.getName(), i.getAmount().toString()))
                .collect(Collectors.joining("\n"));
        return String.format("Trip Budget Estimate for %s (%d days):\nTotal Estimate: %s\nCategory Breakdown:\n%s",
                estimate.getDestination(), estimate.getDays(), estimate.getTotalEstimate().toString(), breakdownStr);
    }
}
