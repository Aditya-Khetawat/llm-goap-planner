import { Box, Grid, Stack, Typography } from "@mui/material";

import { PageContainer } from "@shared/ui/layout/page-container";
import { SectionContainer } from "@shared/ui/layout/section-container";
import { GoalSubmissionForm } from "@features/planner/components/goal-submission-form";
import { PlannerPageHeader } from "@features/planner/components/planner-page-header";
import { useGoalInputPageController } from "@features/planner/hooks/use-goal-input-page-controller";

export function GoalInputPage() {
  const { form, submit, isSubmitting } = useGoalInputPageController();

  return (
    <PageContainer maxWidth="lg">
      <SectionContainer sx={{ py: { xs: 3, md: 6 } }}>
        <PlannerPageHeader
          eyebrow="Planner"
          title="Goal input"
          description="Enter a goal for the backend planner to execute under the Embabel GOAP runtime."
        />

        <Grid container spacing={3}>
          <Grid item xs={12} lg={8}>
            <GoalSubmissionForm
              control={form.control}
              register={form.register}
              errors={form.formState.errors}
              isSubmitting={isSubmitting}
              onSubmit={submit}
            />
          </Grid>
          <Grid item xs={12} lg={4}>
            <Box
              component="aside"
              aria-label="Goal guidance"
              sx={{ p: 3, borderRadius: 3, border: 1, borderColor: "divider" }}
            >
              <Stack spacing={2}>
                <Typography variant="h6" component="h2" fontWeight={700}>
                  Validation rules
                </Typography>
                <Typography variant="body2" color="text.secondary">
                  The form validates that the goal is entered correctly and has at least 3 characters before any request reaches the backend.
                </Typography>
              </Stack>
            </Box>
          </Grid>
        </Grid>
      </SectionContainer>
    </PageContainer>
  );
}

