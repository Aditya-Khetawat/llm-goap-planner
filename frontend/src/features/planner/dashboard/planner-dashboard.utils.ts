import type {
  PlannerGeneratePlanResponse,
  PlannerMetadataItem,
} from "@features/planner/model/planner.models";
import { sanitizeText, truncateText } from "@shared/lib/sanitize";

export interface PlannerTimelineItem {
  id: string;
  title: string;
  agent: string;
  details: string;
  status: string;
}

export interface PlannerMetrics {
  totalSteps: number;
  activeAgents: number;
  completedSteps: number;
}

export function buildPlannerMetadata(result: PlannerGeneratePlanResponse): PlannerMetadataItem[] {
  return [
    { label: "Status", value: sanitizeText(result.status) },
    { label: "Source", value: sanitizeText(result.source) },
    { label: "Classification", value: sanitizeText(result.classification ?? "N/A") },
  ];
}

export function buildPlannerMetrics(result: PlannerGeneratePlanResponse): PlannerMetrics {
  const activeAgents = new Set(result.steps.map((step) => step.agent)).size;
  const completedSteps = result.steps.filter((step) => Boolean(step.output)).length;

  return {
    totalSteps: result.steps.length,
    activeAgents,
    completedSteps,
  };
}

export function buildPlannerTimeline(result: PlannerGeneratePlanResponse): PlannerTimelineItem[] {
  return result.steps.map((step) => {
    return {
      id: `${step.order}-${step.title}`,
      title: truncateText(step.title, 120),
      agent: sanitizeText(step.agent),
      details: truncateText(step.details || "No additional details available.", 500),
      status: sanitizeText(step.output ? "Complete" : "Ready"),
    };
  });
}

