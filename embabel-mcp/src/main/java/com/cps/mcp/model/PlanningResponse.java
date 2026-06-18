package com.cps.mcp.model;

import java.util.List;

public class PlanningResponse {
    private String summary;
    private List<PlanningTask> tasks;

    public PlanningResponse() {}

    public PlanningResponse(String summary, List<PlanningTask> tasks) {
        this.summary = summary;
        this.tasks = tasks;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public List<PlanningTask> getTasks() {
        return tasks;
    }

    public void setTasks(List<PlanningTask> tasks) {
        this.tasks = tasks;
    }

    @Override
    public String toString() {
        return "PlanningResponse{" +
                "summary='" + summary + '\'' +
                ", tasks=" + tasks +
                '}';
    }
}
