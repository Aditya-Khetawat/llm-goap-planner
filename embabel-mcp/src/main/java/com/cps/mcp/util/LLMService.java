package com.cps.mcp.util;

import com.cps.mcp.model.PlanningResponse;

public interface LLMService {
    PlanningResponse generatePlan(String goal, String toolsStr) throws Exception;
    String simulateAgentExecution(String agentName, String task) throws Exception;
}
