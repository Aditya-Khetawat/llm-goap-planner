import { Box, Divider, Grid, Stack, Typography, Chip } from "@mui/material";
import type { ReactNode, SyntheticEvent } from "react";
import { memo, useMemo, useState } from "react";

import { AppCard, AppCardContent, AppCardHeader } from "@shared/ui/components/card";
import { AppTab, AppTabs } from "@shared/ui/components/tabs";
import { MermaidDiagramViewer } from "@shared/ui/visualization/mermaid-diagram-viewer";
import { PlanCard } from "@features/planner/components/plan-card";
import { PlanMetadata } from "@features/planner/components/plan-metadata";
import { PlanSteps } from "@features/planner/components/plan-steps";
import type { PlannerGeneratePlanResponse } from "@features/planner/model/planner.models";
import {
  buildPlannerMetadata,
  buildPlannerMetrics,
  buildPlannerTimeline,
} from "@features/planner/dashboard/planner-dashboard.utils";
import { sanitizeText } from "@shared/lib/sanitize";

interface PlannerDashboardProps {
  result: PlannerGeneratePlanResponse;
}

interface DashboardTabPanelProps {
  children?: ReactNode;
  index: number;
  value: number;
}

function DashboardTabPanel({ children, value, index }: DashboardTabPanelProps) {
  const hidden = value !== index;

  return (
    <Box
      role="tabpanel"
      hidden={hidden}
      id={`planner-tabpanel-${index}`}
      aria-labelledby={`planner-tab-${index}`}
      sx={{ pt: 3 }}
    >
      {!hidden ? children : null}
    </Box>
  );
}

function metricCard(label: string, value: string | number) {
  return (
    <AppCard variant="outlined">
      <AppCardHeader title={label} />
      <AppCardContent>
        <Typography variant="h4" component="p" fontWeight={800}>
          {value}
        </Typography>
      </AppCardContent>
    </AppCard>
  );
}

export function PlannerDashboard({ result }: PlannerDashboardProps) {
  const [tabValue, setTabValue] = useState(0);

  const metadata = useMemo(() => buildPlannerMetadata(result), [result]);
  const metrics = useMemo(() => buildPlannerMetrics(result), [result]);
  const timeline = useMemo(() => buildPlannerTimeline(result), [result]);

  const handleTabChange = (_event: SyntheticEvent, nextValue: number) => {
    setTabValue(nextValue);
  };

  return (
    <Stack spacing={3}>
      <AppCard variant="outlined">
        <AppCardHeader
          title="Visualization dashboard"
          subheader="Planner output and execution analysis"
        />
        <AppCardContent>
          <AppTabs
            value={tabValue}
            onChange={handleTabChange}
            variant="scrollable"
            scrollButtons="auto"
            aria-label="Planner dashboard tabs"
          >
            <AppTab label="Overview" id="planner-tab-0" aria-controls="planner-tabpanel-0" />
            <AppTab label="Diagram" id="planner-tab-1" aria-controls="planner-tabpanel-1" />
            <AppTab label="Timeline" id="planner-tab-2" aria-controls="planner-tabpanel-2" />
            <AppTab label="Execution" id="planner-tab-3" aria-controls="planner-tabpanel-3" />
          </AppTabs>
        </AppCardContent>
      </AppCard>

      <DashboardTabPanel value={tabValue} index={0}>
        <Grid container spacing={3}>
          <Grid item xs={12} md={4}>
            {metricCard("Total steps", metrics.totalSteps)}
          </Grid>
          <Grid item xs={12} md={4}>
            {metricCard("Active agents", metrics.activeAgents)}
          </Grid>
          <Grid item xs={12} md={4}>
            {metricCard("Completed steps", metrics.completedSteps)}
          </Grid>
        </Grid>

        <Grid container spacing={3} sx={{ mt: 0 }}>
          <Grid item xs={12} lg={8}>
            <PlanCard title="Plan summary" subtitle={result.goal}>
              <Typography variant="body1" color="text.secondary">
                {sanitizeText(result.summary)}
              </Typography>
            </PlanCard>
          </Grid>
          <Grid item xs={12} lg={4}>
            <PlanMetadata items={metadata} />
          </Grid>
        </Grid>
      </DashboardTabPanel>

      <DashboardTabPanel value={tabValue} index={1}>
        <AppCard variant="outlined">
          <AppCardHeader
            title="Mermaid diagram viewer"
            subheader="Rendered from the backend mermaid payload"
          />
          <AppCardContent>
            <MermaidDiagramViewer
              title="Execution graph"
              description="The backend-generated Mermaid diagram is rendered in an isolated reusable component."
              diagram={result.mermaidDiagram}
            />
          </AppCardContent>
        </AppCard>
      </DashboardTabPanel>

      <DashboardTabPanel value={tabValue} index={2}>
        <AppCard variant="outlined">
          <AppCardHeader
            title="Action timeline"
            subheader="Sequence of backend-returned steps"
          />
          <AppCardContent>
            <Stack spacing={2}>
              {timeline.map((item, index) => (
                <Stack key={item.id} spacing={1.25}>
                  <Stack
                    direction={{ xs: "column", sm: "row" }}
                    justifyContent="space-between"
                    spacing={1}
                  >
                    <Typography variant="subtitle1" fontWeight={700}>
                      {index + 1}. {item.title}
                    </Typography>
                    <Typography variant="body2" color="text.secondary">
                      {item.agent} · {item.status}
                    </Typography>
                  </Stack>
                  <Typography variant="body2" color="text.secondary">
                    {item.details}
                  </Typography>
                  {index < timeline.length - 1 ? <Divider /> : null}
                </Stack>
              ))}
            </Stack>
          </AppCardContent>
        </AppCard>
      </DashboardTabPanel>

      <DashboardTabPanel value={tabValue} index={3}>
        <Grid container spacing={3}>
          <Grid item xs={12} lg={7}>
            <PlanCard title="Execution details" subtitle="Backend payload preserved as-is">
              <Stack spacing={1.5}>
                <Typography variant="body2" color="text.secondary">
                  Status: {sanitizeText(result.status)}
                </Typography>
                <Typography variant="body2" color="text.secondary">
                  Source: {sanitizeText(result.source)}
                </Typography>
                <Typography variant="body2" color="text.secondary">
                  Classification: {sanitizeText(result.classification ?? "N/A")}
                </Typography>
              </Stack>
            </PlanCard>
          </Grid>
          <Grid item xs={12} lg={5}>
            <AppCard variant="outlined">
              <AppCardHeader title="Action steps" subheader="Readable step list from the backend" />
              <AppCardContent>
                <PlanSteps steps={result.steps} />
              </AppCardContent>
            </AppCard>
          </Grid>
        </Grid>

        {result.trace?.length ? (
          <AppCard variant="outlined" sx={{ mt: 3 }}>
            <AppCardHeader
              title="Trace snapshot"
              subheader="Execution trace data from the backend GOAP planner"
            />
            <AppCardContent>
              <Stack spacing={3}>
                {result.trace.map((entry, index) => (
                  <Box key={`${entry.action}-${index}`} sx={{ p: 2.5, borderRadius: 2, border: 1, borderColor: "divider" }}>
                    <Typography variant="subtitle1" fontWeight={700} color="primary" gutterBottom>
                      Action: {entry.action}
                    </Typography>

                    <Grid container spacing={2} sx={{ mt: 1 }}>
                      <Grid item xs={12} md={6}>
                        <Typography variant="body2" fontWeight={600} gutterBottom>
                          Preconditions Checked
                        </Typography>
                        <Stack direction="row" spacing={1} flexWrap="wrap" useFlexGap sx={{ mb: 2 }}>
                          {entry.preconditions_checked?.length > 0 ? (
                            entry.preconditions_checked.map((cond) => (
                              <Chip key={cond} label={cond} size="small" variant="outlined" color="primary" />
                            ))
                          ) : (
                            <Typography variant="caption" color="text.disabled">None</Typography>
                          )}
                        </Stack>

                        {entry.missing_preconditions?.length > 0 && (
                          <>
                            <Typography variant="body2" fontWeight={600} color="error" gutterBottom>
                              Missing Preconditions
                            </Typography>
                            <Stack direction="row" spacing={1} flexWrap="wrap" useFlexGap sx={{ mb: 2 }}>
                              {entry.missing_preconditions.map((cond) => (
                                <Chip key={cond} label={cond} size="small" color="error" variant="outlined" />
                              ))}
                            </Stack>
                          </>
                        )}

                        <Typography variant="body2" fontWeight={600} gutterBottom>
                          Effects Applied
                        </Typography>
                        <Stack direction="row" spacing={1} flexWrap="wrap" useFlexGap>
                          {entry.effects_applied?.length > 0 ? (
                            entry.effects_applied.map((eff) => (
                              <Chip key={eff} label={eff} size="small" variant="outlined" color="success" />
                            ))
                          ) : (
                            <Typography variant="caption" color="text.disabled">None</Typography>
                          )}
                        </Stack>
                      </Grid>

                      <Grid item xs={12} md={6}>
                        <Typography variant="body2" fontWeight={600} gutterBottom>
                          State Before Action
                        </Typography>
                        <Stack direction="row" spacing={1} flexWrap="wrap" useFlexGap sx={{ mb: 2 }}>
                          {entry.state_before?.length > 0 ? (
                            entry.state_before.map((state) => (
                              <Chip key={state} label={state} size="small" />
                            ))
                          ) : (
                            <Typography variant="caption" color="text.disabled">Empty</Typography>
                          )}
                        </Stack>

                        <Typography variant="body2" fontWeight={600} gutterBottom>
                          State After Action
                        </Typography>
                        <Stack direction="row" spacing={1} flexWrap="wrap" useFlexGap>
                          {entry.state_after?.length > 0 ? (
                            entry.state_after.map((state) => (
                              <Chip key={state} label={state} size="small" color="secondary" />
                            ))
                          ) : (
                            <Typography variant="caption" color="text.disabled">Empty</Typography>
                          )}
                        </Stack>
                      </Grid>
                    </Grid>
                  </Box>
                ))}
              </Stack>
            </AppCardContent>
          </AppCard>
        ) : null}
      </DashboardTabPanel>
    </Stack>
  );
}

export const MemoizedPlannerDashboard = memo(PlannerDashboard);

