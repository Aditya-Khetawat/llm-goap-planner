package com.cps.mcp.budget.model;

import java.math.BigDecimal;

/**
 * Structured response wrapping budget calculation results.
 */
public class BudgetResponse {
    private final String status;
    private final BigDecimal resultValue;
    private final String message;

    public BudgetResponse(String status, BigDecimal resultValue, String message) {
        this.status = status;
        this.resultValue = resultValue;
        this.message = message;
    }

    public String getStatus() {
        return status;
    }

    public BigDecimal getResultValue() {
        return resultValue;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "BudgetResponse{status='" + status + "', resultValue=" + resultValue + ", message='" + message + "'}";
    }
}
