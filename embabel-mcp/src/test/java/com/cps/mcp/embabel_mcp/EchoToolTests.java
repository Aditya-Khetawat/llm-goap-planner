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
import com.cps.mcp.tool.EchoToolGroup;

import java.util.List;

@SpringBootTest
public class EchoToolTests {

    private static final Logger logger = LoggerFactory.getLogger(EchoToolTests.class);

    @Autowired
    private EchoToolGroup echoToolGroup;

    @Autowired
    private ToolGroupResolver toolGroupResolver;

    @Test
    public void testToolRegistration() {
        // 1. Verify Spring bean registration
        assertNotNull(echoToolGroup, "EchoToolGroup bean should be registered in the Spring Context");
    }

    @Test
    public void testToolDiscovery() {
        // 2. Verify discovery via ToolGroupResolver
        assertNotNull(toolGroupResolver);
        assertTrue(toolGroupResolver instanceof RegistryToolGroupResolver, "Resolver should be RegistryToolGroupResolver");
        List<ToolGroup> groups = ((RegistryToolGroupResolver) toolGroupResolver).getToolGroups();
        boolean found = groups.stream()
                .anyMatch(tg -> "EchoToolGroup".equals(tg.getMetadata().getName()));
        assertTrue(found, "EchoToolGroup should be discovered by the ToolGroupResolver");
    }

    @Test
    public void testSuccessfulExecution() {
        // 3. Verify successful execution
        List<Tool> tools = echoToolGroup.getTools();
        assertEquals(1, tools.size(), "Should have exactly one tool");
        Tool echoTool = tools.get(0);
        assertEquals("echo", echoTool.getDefinition().getName());

        // Log: Agent -> Planner -> Tool Selection -> Tool Execution -> Tool Result -> Goal Completed
        logger.info("Agent: Starting Echo tool test execution");
        logger.info("Planner: Selecting tool 'echo'");
        logger.info("Tool Selection: Selected tool 'echo'");
        logger.info("Tool Execution: Calling echo tool with parameter 'hello'");

        // Note: tool input must be JSON-serialized string
        Tool.Result result = echoTool.call("\"hello\"");

        logger.info("Tool Result: " + result);

        assertTrue(result instanceof Tool.Result.Text, "Result should be a Text result");
        assertEquals("hello", ((Tool.Result.Text) result).getContent(), "Output should match the input");

        logger.info("Goal Completed: Echo execution successfully completed");
    }

    @Test
    public void testErrorPropagation() {
        // 4. Verify exception to tool error propagation
        List<Tool> tools = echoToolGroup.getTools();
        Tool echoTool = tools.get(0);

        logger.info("Tool Execution: Calling echo tool with parameter 'fail' to test error propagation");
        Tool.Result result = echoTool.call("\"fail\"");
        logger.info("Tool Result: " + result);

        assertTrue(result instanceof Tool.Result.Error, "Result should be an Error result");
        Tool.Result.Error errorResult = (Tool.Result.Error) result;
        assertTrue(errorResult.getMessage().contains("Simulated tool error"), "Error message should propagate");
    }
}
