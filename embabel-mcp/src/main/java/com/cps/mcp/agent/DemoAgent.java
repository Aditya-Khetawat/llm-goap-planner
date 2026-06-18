package com.cps.mcp.agent;

import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.AchievesGoal;

@Agent(name = "DemoAgent", description = "Demo agent for verification")
public class DemoAgent {

    @Action(description = "Say Hello")
    @AchievesGoal(description = "Hello Goal")
    public String hello() {
        return "Hello, Embabel!";
    }
}
