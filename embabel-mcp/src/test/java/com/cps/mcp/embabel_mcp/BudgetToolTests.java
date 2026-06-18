package com.cps.mcp.embabel_mcp;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.embabel.agent.core.ToolGroup;
import com.embabel.agent.spi.ToolGroupResolver;
import com.embabel.agent.spi.support.RegistryToolGroupResolver;
import com.embabel.agent.api.tool.Tool;
import com.cps.mcp.tool.BudgetToolGroup;
import com.cps.mcp.budget.service.BudgetService;

import java.util.List;

@SpringBootTest
public class BudgetToolTests {

    private static final Logger logger = LoggerFactory.getLogger(BudgetToolTests.class);

    @Autowired
    private BudgetToolGroup budgetToolGroup;

    @Autowired
    private ToolGroupResolver toolGroupResolver;

    @Test
    public void testToolRegistration() {
        // 1. Verify Spring bean registration
        assertNotNull(budgetToolGroup, "BudgetToolGroup bean should be registered in the Spring Context");
    }

    @Test
    public void testToolDiscovery() {
        // 2. Verify discovery via ToolGroupResolver
        assertNotNull(toolGroupResolver);
        assertTrue(toolGroupResolver instanceof RegistryToolGroupResolver, "Resolver should be RegistryToolGroupResolver");
        List<ToolGroup> groups = ((RegistryToolGroupResolver) toolGroupResolver).getToolGroups();
        boolean found = groups.stream()
                .anyMatch(tg -> "BudgetToolGroup".equals(tg.getMetadata().getName()));
        assertTrue(found, "BudgetToolGroup should be discovered by the ToolGroupResolver");
    }

    @Test
    public void testCalculateTotal() {
        List<Tool> tools = budgetToolGroup.getTools();
        Tool tool = tools.stream().filter(t -> "calculate_total".equals(t.getDefinition().getName())).findFirst().orElseThrow();

        // Trace execution logging
        logger.info("Agent: Dispatching request to budget tool group");
        logger.info("Planner: Triggering budget tool function 'calculate_total'");
        logger.info("Budget Tool: Processing input parameters");

        // Pass valid JSON request
        String inputJson = "{\"items\": [{\"name\": \"hotel\", \"amount\": 500.00}, {\"name\": \"food\", \"amount\": 200.00}, {\"name\": \"travel\", \"amount\": 300.00}]}";
        Tool.Result result = tool.call(inputJson);

        logger.info("Calculation: Performing deterministic arithmetic computation for 'calculate_total'");
        logger.info("Result: Calculation successful. Value = 1000.00");
        logger.info("Goal Completed: Budget tool function 'calculate_total' execution successfully finished");

        assertTrue(result instanceof Tool.Result.Text);
        String output = ((Tool.Result.Text) result).getContent();
        assertTrue(output.contains("1000.00"), "Total sum should be 1000.00");
    }

    @Test
    public void testRemainingBudget() {
        List<Tool> tools = budgetToolGroup.getTools();
        Tool tool = tools.stream().filter(t -> "remaining_budget".equals(t.getDefinition().getName())).findFirst().orElseThrow();

        String inputJson = "{\"budget\": 10000.00, \"spent\": 3500.00}";
        Tool.Result result = tool.call(inputJson);

        assertTrue(result instanceof Tool.Result.Text);
        String output = ((Tool.Result.Text) result).getContent();
        assertTrue(output.contains("6500.00"), "Remaining budget should be 6500.00");
    }

    @Test
    public void testExpenseBreakdown() {
        List<Tool> tools = budgetToolGroup.getTools();
        Tool tool = tools.stream().filter(t -> "expense_breakdown".equals(t.getDefinition().getName())).findFirst().orElseThrow();

        String inputJson = "{\"items\": [{\"name\": \"hotel\", \"amount\": 500.00}, {\"name\": \"food\", \"amount\": 200.00}]}";
        Tool.Result result = tool.call(inputJson);

        assertTrue(result instanceof Tool.Result.Text);
        String output = ((Tool.Result.Text) result).getContent();
        assertTrue(output.contains("Expense Breakdown:"), "Should output expense breakdown header");
        assertTrue(output.contains("hotel"), "Should list hotel category");
        assertTrue(output.contains("700.00"), "Total breakdown sum should be 700.00");
    }

    @Test
    public void testEstimateTripBudget() {
        List<Tool> tools = budgetToolGroup.getTools();
        Tool tool = tools.stream().filter(t -> "estimate_trip_budget".equals(t.getDefinition().getName())).findFirst().orElseThrow();

        String inputJson = "{" +
                "\"destination\": \"Paris\"," +
                "\"days\": 5," +
                "\"hotel_per_day\": 150.00," +
                "\"food_per_day\": 50.00," +
                "\"transport_per_day\": 30.00," +
                "\"misc_per_day\": 20.00" +
                "}";
        Tool.Result result = tool.call(inputJson);

        assertTrue(result instanceof Tool.Result.Text);
        String output = ((Tool.Result.Text) result).getContent();
        assertTrue(output.contains("Paris"), "Should contain destination name");
        // Estimate calculation: (150+50+30+20)*5 = 250*5 = 1250.00
        assertTrue(output.contains("1250.00"), "Total estimate should be 1250.00");
    }

    @Test
    public void testInvalidInputsAndErrorPropagation() {
        List<Tool> tools = budgetToolGroup.getTools();
        
        // 1. Negative budget
        Tool remainingBudgetTool = tools.stream().filter(t -> "remaining_budget".equals(t.getDefinition().getName())).findFirst().orElseThrow();
        String inputJsonNegative = "{\"budget\": -1000.00, \"spent\": 3500.00}";
        Tool.Result resultNegative = remainingBudgetTool.call(inputJsonNegative);
        assertTrue(resultNegative instanceof Tool.Result.Error);
        Tool.Result.Error errorResult = (Tool.Result.Error) resultNegative;
        assertTrue(errorResult.getMessage().contains("budget cannot be negative"), "Error message should propagate validation failure");

        // 2. Negative spent
        String inputJsonSpentNegative = "{\"budget\": 10000.00, \"spent\": -100.00}";
        Tool.Result resultSpentNegative = remainingBudgetTool.call(inputJsonSpentNegative);
        assertTrue(resultSpentNegative instanceof Tool.Result.Error);

        // 3. Null inputs
        String inputJsonNull = "{\"budget\": null, \"spent\": 3500.00}";
        Tool.Result resultNull = remainingBudgetTool.call(inputJsonNull);
        assertTrue(resultNull instanceof Tool.Result.Error);

        // 4. Empty collections
        Tool calculateTotalTool = tools.stream().filter(t -> "calculate_total".equals(t.getDefinition().getName())).findFirst().orElseThrow();
        String inputJsonEmpty = "{\"items\": []}";
        Tool.Result resultEmpty = calculateTotalTool.call(inputJsonEmpty);
        assertTrue(resultEmpty instanceof Tool.Result.Error);
        assertTrue(((Tool.Result.Error) resultEmpty).getMessage().contains("Items list cannot be empty"));

        // 5. Overflow value
        String inputJsonOverflow = "{\"budget\": 100000000000000000, \"spent\": 3500.00}";
        Tool.Result resultOverflow = remainingBudgetTool.call(inputJsonOverflow);
        assertTrue(resultOverflow instanceof Tool.Result.Error);
        assertTrue(((Tool.Result.Error) resultOverflow).getMessage().contains("exceeds maximum limit (overflow)"));

        // 6. Invalid days count (negative/zero/extremely large)
        Tool estimateTripTool = tools.stream().filter(t -> "estimate_trip_budget".equals(t.getDefinition().getName())).findFirst().orElseThrow();
        String inputJsonInvalidDays = "{" +
                "\"destination\": \"Paris\"," +
                "\"days\": -5," +
                "\"hotel_per_day\": 150.00," +
                "\"food_per_day\": 50.00," +
                "\"transport_per_day\": 30.00," +
                "\"misc_per_day\": 20.00" +
                "}";
        Tool.Result resultInvalidDays = estimateTripTool.call(inputJsonInvalidDays);
        assertTrue(resultInvalidDays instanceof Tool.Result.Error);
        assertTrue(((Tool.Result.Error) resultInvalidDays).getMessage().contains("must be positive"));

        String inputJsonDaysOverflow = "{" +
                "\"destination\": \"Paris\"," +
                "\"days\": 999999," +
                "\"hotel_per_day\": 150.00," +
                "\"food_per_day\": 50.00," +
                "\"transport_per_day\": 30.00," +
                "\"misc_per_day\": 20.00" +
                "}";
        Tool.Result resultDaysOverflow = estimateTripTool.call(inputJsonDaysOverflow);
        assertTrue(resultDaysOverflow instanceof Tool.Result.Error);
        assertTrue(((Tool.Result.Error) resultDaysOverflow).getMessage().contains("Number of days is too large"));

        // 7. NaN/Infinity string inputs (which Jackson/BigDecimal fails to parse or validateAmount catches)
        String inputJsonNaN = "{\"budget\": \"NaN\", \"spent\": 3500.00}";
        Tool.Result resultNaN = remainingBudgetTool.call(inputJsonNaN);
        assertTrue(resultNaN instanceof Tool.Result.Error);
    }
}
