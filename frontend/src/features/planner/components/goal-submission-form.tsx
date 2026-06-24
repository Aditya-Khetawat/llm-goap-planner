import { Box, Stack, Typography } from "@mui/material";
import type { FormEventHandler } from "react";
import type { Control, FieldErrors, UseFormRegister } from "react-hook-form";

import { AppButton } from "@shared/ui/components/button";
import { AppTextarea } from "@shared/ui/components/textarea";
import type { PlannerGoalFormValues } from "@features/planner/model/planner.schema";

interface GoalSubmissionFormProps {
  control: Control<PlannerGoalFormValues>;
  register: UseFormRegister<PlannerGoalFormValues>;
  errors: FieldErrors<PlannerGoalFormValues>;
  isSubmitting: boolean;
  onSubmit: FormEventHandler<HTMLFormElement>;
}

// Inline SVG for the button icon to bypass any React 19 / SvgIcon context issue
const ArrowRightIcon = () => (
  <svg
    width="14"
    height="14"
    viewBox="0 0 24 24"
    fill="none"
    stroke="currentColor"
    strokeWidth="3"
    strokeLinecap="round"
    strokeLinejoin="round"
  >
    <line x1="5" y1="12" x2="19" y2="12" />
    <polyline points="12 5 19 12 12 19" />
  </svg>
);

export function GoalSubmissionForm({
  register,
  errors,
  isSubmitting,
  onSubmit,
}: GoalSubmissionFormProps) {
  return (
    <Box component="form" onSubmit={onSubmit} sx={{ width: "100%" }}>
      <AppTextarea
        placeholder="E.g., Plan a 3-day weekend trip to Rome with historical sightseeing"
        error={Boolean(errors.goal)}
        {...register("goal")}
      >
        <Stack
          direction="row"
          alignItems="center"
          justifyContent="space-between"
          sx={{ mt: 3 }}
        >
          {/* Helper or error message */}
          <Typography
            variant="caption"
            color={errors.goal ? "error" : "text.secondary"}
            sx={{ fontWeight: 500 }}
          >
            {errors.goal?.message ?? ""}
          </Typography>

          <AppButton
            type="submit"
            variant="contained"
            loading={isSubmitting}
            loadingText="Planning..."
            endIcon={isSubmitting ? null : <ArrowRightIcon />}
            sx={{
              borderRadius: "6px", // less rounded than current (8px)
              px: 3,
              py: 0.8, // slightly smaller height
              fontWeight: 600,
              fontSize: "0.875rem",
              background: "linear-gradient(135deg, #7C5CFF 0%, #633EF8 100%)",
              boxShadow: "0 4px 14px 0 rgba(124, 92, 255, 0.25)",
              border: "none",
              color: "#FFFFFF",
              transition: "all var(--transition-fast)",
              "&:hover": {
                background: "linear-gradient(135deg, #8B6EFF 0%, #6F4EFF 100%)",
                transform: "translateY(-1.5px)",
                boxShadow: "0 6px 20px 0 rgba(124, 92, 255, 0.35)",
              },
              "&:active": {
                transform: "translateY(0.5px)",
                boxShadow: "0 2px 8px 0 rgba(124, 92, 255, 0.2)",
              },
              "&.Mui-disabled": {
                background: "rgba(124, 92, 255, 0.4)",
                color: "rgba(255, 255, 255, 0.6)",
                boxShadow: "none",
              },
            }}
          />
        </Stack>
      </AppTextarea>
    </Box>
  );
}



