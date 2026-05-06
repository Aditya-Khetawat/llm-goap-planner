package com.ip3b.goap_planner.model;

import java.util.List;

public record PlanRequest(String goal, List<String> tools) {
}