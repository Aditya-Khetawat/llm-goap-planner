export type ApiStatus = "success" | "error";

export interface ApiErrorResponse {
  message?: string;
  error?: string;
  code?: string;
}

export interface PlanGenerationRequest {
  goal: string;
}

export interface PlanStep {
  order: number;
  title: string;
  details: string;
  agent: string;
  output: string | null;
}

export interface PlanTraceEntry {
  action: string;
  state_before: string[];
  preconditions_checked: string[];
  missing_preconditions: string[];
  effects_applied: string[];
  state_after: string[];
}

export interface PlanResponse {
  goal: string;
  classification: string;
  status: string;
  steps: PlanStep[];
  trace: PlanTraceEntry[];
  mermaidDiagram: string;
  ganttDiagram?: string;
  summary: string;
  source: string;
}

