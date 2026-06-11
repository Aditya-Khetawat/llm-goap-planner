package com.cps.mcp.embabel_mcp;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.embabel.agent.core.AgentPlatform;
import com.embabel.agent.core.ProcessOptions;
import com.embabel.agent.api.common.autonomy.Autonomy;
import com.embabel.agent.api.common.autonomy.AgentProcessExecution;
import com.cps.mcp.agent.DemoAgent;

@SpringBootTest
public class EmbabelAgentTests {

    @Autowired
    private AgentPlatform agentPlatform;

    @Autowired
    private Autonomy autonomy;

    @Autowired
    private DemoAgent demoAgent;

    @Test
    public void testAgentRegistration() {
        assertNotNull(demoAgent, "DemoAgent bean should be registered in the Spring Context");
        
        boolean agentRegistered = agentPlatform.agents().stream()
            .anyMatch(agent -> agent.getDescription().contains("Demo agent"));
        
        assertTrue(agentRegistered, "DemoAgent should be registered in the Embabel AgentPlatform");
    }

    @Test
    public void testActionCallable() throws Exception {
        AgentProcessExecution execution = autonomy.chooseAndRunAgent("Hello Goal", new ProcessOptions());
        
        assertNotNull(execution, "Execution should not be null");
        assertEquals("Hello, Embabel!", execution.getOutput(), "Action output should match the expected string");
    }
}
