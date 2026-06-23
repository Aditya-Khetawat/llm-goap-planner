import { z } from "zod";

import type { PlannerGeneratePlanRequest } from "@features/planner/model/planner.models";

export const plannerGoalSchema = z.object({
  goal: z.string().trim().min(3, "Enter a goal with at least 3 characters.").max(2000),
});

export type PlannerGoalFormValues = z.infer<typeof plannerGoalSchema>;

export const plannerGoalDefaults: PlannerGoalFormValues = {
  goal: "",
};

export function toPlannerRequest(values: PlannerGoalFormValues): PlannerGeneratePlanRequest {
  return {
    goal: values.goal.trim(),
  };
}

