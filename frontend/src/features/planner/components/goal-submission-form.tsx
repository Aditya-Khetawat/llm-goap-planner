import { Paper, Stack, Typography } from "@mui/material";
import type { FormEventHandler } from "react";
import type { Control, FieldErrors, UseFormRegister } from "react-hook-form";

import { AppButton } from "@shared/ui/components/button";
import { AppTextField } from "@shared/ui/components/text-field";
import type { PlannerGoalFormValues } from "@features/planner/model/planner.schema";

interface GoalSubmissionFormProps {
  control: Control<PlannerGoalFormValues>;
  register: UseFormRegister<PlannerGoalFormValues>;
  errors: FieldErrors<PlannerGoalFormValues>;
  isSubmitting: boolean;
  onSubmit: FormEventHandler<HTMLFormElement>;
}

export function GoalSubmissionForm({
  register,
  errors,
  isSubmitting,
  onSubmit,
}: GoalSubmissionFormProps) {
  return (
    <Paper component="form" onSubmit={onSubmit} variant="outlined" sx={{ p: { xs: 2.5, md: 4 } }}>
      <Stack spacing={3}>
        <div>
          <Typography variant="h5" component="h2" fontWeight={700} gutterBottom>
            Describe the goal
          </Typography>
          <Typography variant="body2" color="text.secondary">
            The backend remains the source of truth. This form only validates and forwards your
            request.
          </Typography>
        </div>

        <AppTextField
          label="Goal"
          placeholder="Plan a weekend in Rome"
          multiline
          minRows={6}
          maxRows={12}
          error={Boolean(errors.goal)}
          helperText={errors.goal?.message ?? "Enter a clear goal for the planner."}
          {...register("goal")}
        />

        <Stack direction={{ xs: "column", sm: "row" }} spacing={2} justifyContent="flex-end">
          <AppButton type="submit" variant="contained" size="large" disabled={isSubmitting}>
            {isSubmitting ? "Planning..." : "Generate plan"}
          </AppButton>
        </Stack>
      </Stack>
    </Paper>
  );
}

