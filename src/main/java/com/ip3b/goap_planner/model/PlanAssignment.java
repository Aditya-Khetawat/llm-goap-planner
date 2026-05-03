package com.ip3b.goap_planner.model;

public record PlanAssignment(
	int order,
	String stepTitle,
	String agent,
	String capability,
	String status,
	String handoffTo,
	String rationale) {
}