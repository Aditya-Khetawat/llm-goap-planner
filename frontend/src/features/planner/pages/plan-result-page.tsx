import { EmptyState } from "@shared/ui/feedback/empty-state";
import { PageContainer } from "@shared/ui/layout/page-container";
import { SectionContainer } from "@shared/ui/layout/section-container";
import { ErrorState } from "@shared/ui/feedback/error-state";
import { LoadingState } from "@shared/ui/feedback/loading-state";
import { SkeletonLoader } from "@shared/ui/components/skeleton-loader";
import { MemoizedPlannerDashboard } from "@features/planner/dashboard/planner-dashboard";
import { PageHeader } from "@shared/ui/components/page-header";
import { usePlanResultPageController } from "@features/planner/hooks/use-plan-result-page-controller";
import { APP_NAME } from "@shared/constants/app";

export function PlanResultPage() {
  const { result, status, errorMessage, isLoading, handleRetry, handleStartOver } =
    usePlanResultPageController();

  return (
    <PageContainer maxWidth="xl">
      <SectionContainer sx={{ py: { xs: 6, md: 10 } }}>
        {status !== "success" ? (
          <PageHeader
            eyebrow="Planner"
            title="Plan result"
            description="Results are read from the backend response and rendered without unsafe HTML."
          />
        ) : null}

        {isLoading ? (
          <LoadingState
            title="Generating plan"
            description="The backend is processing the goal and preparing the execution plan."
          >
            <SkeletonLoader variant="card" count={3} />
          </LoadingState>
        ) : null}

        {!isLoading && status === "error" ? (
          <ErrorState
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
