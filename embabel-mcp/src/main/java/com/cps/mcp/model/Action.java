package com.cps.mcp.model;

import java.util.List;
import com.cps.mcp.util.MCPClient;

public class Action {
    private String id;
    private String name; // title
    private String description;
    private List<String> preconditions;
    private List<String> effects;
    private String userGoal;
    private String agent;

    public Action(String name, List<String> preconditions, List<String> effects) {
        this(null, name, "", preconditions, effects, null, "SearchAgent");
    }

    public Action(String name, List<String> preconditions, List<String> effects, String userGoal) {
        this(null, name, "", preconditions, effects, userGoal, "SearchAgent");
    }

    public Action(String name, List<String> preconditions, List<String> effects, String userGoal, String agent) {
        this(null, name, "", preconditions, effects, userGoal, agent);
    }

    public Action(String id, String name, String description, List<String> preconditions, List<String> effects, String userGoal, String agent) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.preconditions = preconditions;
        this.effects = effects;
        this.userGoal = userGoal;
        this.agent = agent;
    }

    public boolean canExecute(State state) {
        for (String pre : preconditions) {
            if (!state.has(pre)) {
                return false;
            }
        }
        return true;
    }

    public void apply(State state) {
        for (String effect : effects) {
            state.add(effect);
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public String getTitle() {
        return name;
    }

    public void setTitle(String title) {
        this.name = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getPreconditions() {
        return preconditions;
    }

    public List<String> getEffects() {
        return effects;
    }

    public String getAgent() {
        return agent;
    }
}