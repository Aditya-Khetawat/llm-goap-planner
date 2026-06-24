import { EmptyState } from "@shared/ui/feedback/empty-state";
import { PageContainer } from "@shared/ui/layout/page-container";
import { SectionContainer } from "@shared/ui/layout/section-container";
import { PlannerErrorState } from "@features/planner/components/planner-error-state";
import { PlannerLoadingState } from "@features/planner/components/planner-loading-state";
import { MemoizedPlannerDashboard } from "@features/planner/dashboard/planner-dashboard";
import { PlannerPageHeader } from "@features/planner/components/planner-page-header";
import { usePlanResultPageController } from "@features/planner/hooks/use-plan-result-page-controller";
import { APP_NAME } from "@shared/constants/app";

export function PlanResultPage() {
  const { result, status, errorMessage, isLoading, handleRetry, handleStartOver } =
    usePlanResultPageController();

  return (
    <PageContainer maxWidth="xl">
      <SectionContainer sx={{ py: { xs: 3, md: 6 } }}>
        {status !== "success" ? (
          <PlannerPageHeader
            eyebrow="Planner"
            title="Plan result"
            description="Results are read from the backend response and rendered without unsafe HTML."
          />
        ) : null}

        {isLoading ? <PlannerLoadingState /> : null}

        {!isLoading && status === "error" ? (
          <PlannerErrorState
            title={`${APP_NAME} could not generate a plan`}
            description={
              errorMessage ?? "A network or backend failure occurred while generating the plan."
            }
            onRetry={handleRetry}
            onStartOver={handleStartOver}
          />
        ) : null}

        {!isLoading && status === "idle" ? (
          <EmptyState
            title="No plan available yet"
            description="Submit a goal from the input page to generate a plan result."
          />
        ) : null}

        {!isLoading && status === "success" && result ? (
          <MemoizedPlannerDashboard result={result} />
        ) : null}
      </SectionContainer>
    </PageContainer>
  );
}
