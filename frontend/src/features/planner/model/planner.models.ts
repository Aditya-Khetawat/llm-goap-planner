import type { PlanStep, PlanTraceEntry } from "@shared/types/api";

export interface PlannerGeneratePlanRequest {
  goal: string;
}

export interface PlannerGeneratePlanResponse {
  goal: string;
  classification: string;
  status: string;
  steps: PlanStep[];
  trace: PlanTraceEntry[];
  mermaidDiagram: string;
  summary: string;
  source: string;
}

export interface PlannerExecutionSnapshot {
  request: PlannerGeneratePlanRequest | null;
  result: PlannerGeneratePlanResponse | null;
  status: "idle" | "loading" | "success" | "error";
  errorMessage: string | null;
}

export interface PlannerMetadataItem {
  label: string;
  value: string;
}

