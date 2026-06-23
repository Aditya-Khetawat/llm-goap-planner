import { createBrowserRouter, Navigate, RouterProvider } from "react-router-dom";
import { lazy } from "react";

import { ROUTE_PATHS } from "@shared/constants/routes";
import { GlobalLayout } from "@app/layouts/global-layout";

const GoalInputPage = lazy(() =>
  import("@features/planner/pages/goal-input-page").then((module) => ({
    default: module.GoalInputPage,
  })),
);

const PlanResultPage = lazy(() =>
  import("@features/planner/pages/plan-result-page").then((module) => ({
    default: module.PlanResultPage,
  })),
);

const appRouter = createBrowserRouter([
  {
    path: ROUTE_PATHS.root,
    element: <GlobalLayout />,
    children: [
      {
        index: true,
        element: <Navigate to={ROUTE_PATHS.planner} replace />,
      },
      {
        path: ROUTE_PATHS.planner.slice(1),
        element: <GoalInputPage />,
      },
      {
        path: ROUTE_PATHS.plannerResult.slice(1),
        element: <PlanResultPage />,
      },
      {
        path: "*",
        element: <Navigate to={ROUTE_PATHS.planner} replace />,
      },
    ],
  },
]);

export function AppRouter() {
  return <RouterProvider router={appRouter} />;
}
