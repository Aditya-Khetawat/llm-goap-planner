package com.cps.mcp.model;

import java.util.ArrayList;
import java.util.List;

public class PlanningTask {
    private int id;
    private String title;
    private String description;
    private String agent;
    private String reason;
    private List<String> preconditions = new ArrayList<>();
    private List<String> effects = new ArrayList<>();

    public PlanningTask() {}

    public PlanningTask(int id, String title, String description, String agent, String reason) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.agent = agent;
        this.reason = reason;
    }

    public PlanningTask(int id, String title, String description, String agent, String reason, List<String> preconditions, List<String> effects) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.agent = agent;
        this.reason = reason;
        this.preconditions = preconditions != null ? preconditions : new ArrayList<>();
        this.effects = effects != null ? effects : new ArrayList<>();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAgent() {
        return agent;
    }

    public void setAgent(String agent) {
        this.agent = agent;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public List<String> getPreconditions() {
        return preconditions;
    }

    public void setPreconditions(List<String> preconditions) {
        this.preconditions = preconditions != null ? preconditions : new ArrayList<>();
    }

    public List<String> getEffects() {
        return effects;
    }

    public void setEffects(List<String> effects) {
        this.effects = effects != null ? effects : new ArrayList<>();
    }

    @Override
    public String toString() {
        return "PlanningTask{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", agent='" + agent + '\'' +
                ", reason='" + reason + '\'' +
                ", preconditions=" + preconditions +
                ", effects=" + effects +
                '}';
    }
}
