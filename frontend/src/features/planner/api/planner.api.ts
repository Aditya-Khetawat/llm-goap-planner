import { generatePlan } from "@shared/api/plans";

import type {
  PlannerGeneratePlanRequest,
  PlannerGeneratePlanResponse,
} from "@features/planner/model/planner.models";

export async function submitPlannerGoal(
  request: PlannerGeneratePlanRequest,
): Promise<PlannerGeneratePlanResponse> {
  const startedAt = performance.now();
  const response = await generatePlan(request);
  // Measured round-trip time of the request, shown as the planning time in the dashboard
  return { ...response, elapsedMs: Math.round(performance.now() - startedAt) };
}
