import { Box, Divider, Grid, Stack, Typography, Chip } from "@mui/material";
import { memo, useMemo } from "react";

import { MermaidDiagramViewer } from "@shared/ui/visualization/mermaid-diagram-viewer";
import { PlanSteps } from "@features/planner/components/plan-steps";
import type { PlannerGeneratePlanResponse } from "@features/planner/model/planner.models";
import {
  buildPlannerMetrics,
  buildPlannerTimeline,
} from "@features/planner/dashboard/planner-dashboard.utils";
import { sanitizeText } from "@shared/lib/sanitize";

interface PlannerDashboardProps {
  result: PlannerGeneratePlanResponse;
}

function EditorialSectionHeader({ title, description }: { title: string; description?: string }) {
  return (
    <Box sx={{ mb: 4 }}>
      <Typography
        variant="h4"
        component="h2"
        fontWeight="var(--font-weight-semibold)"
        letterSpacing="-0.02em"
        gutterBottom
      >
        {title}
      </Typography>
      {description ? (
        <Typography variant="body2" color="text.secondary">
          {description}
        </Typography>
      ) : null}
      <Divider sx={{ mt: 2, borderColor: "var(--color-border)" }} />
    </Box>
  );
}

export function PlannerDashboard({ result }: PlannerDashboardProps) {
  const metrics = useMemo(() => buildPlannerMetrics(result), [result]);
  const timeline = useMemo(() => buildPlannerTimeline(result), [result]);

  const isSuccess = result.status.toLowerCase().includes("success") || result.status.toLowerCase() === "ok";

  return (
    <Stack spacing={8} sx={{ width: "100%", py: 2 }}>
      {/* Hero Section */}
      <Box>
        <Stack direction="row" spacing={1.5} alignItems="center" sx={{ mb: 2 }}>
          <Chip
            label={sanitizeText(result.status)}
            size="small"
            color={isSuccess ? "success" : "warning"}
            variant="outlined"
            sx={{ fontWeight: 600, textTransform: "uppercase", letterSpacing: "0.03em" }}
          />
          {result.classification ? (
            <Chip
              label={sanitizeText(result.classification)}
              size="small"
              variant="outlined"
              sx={{ opacity: 0.8, textTransform: "uppercase" }}
            />
          ) : null}
          <Typography variant="caption" color="text.secondary">
            Source: {sanitizeText(result.source)}
          </Typography>
        </Stack>

        <Typography
          variant="h2"
          component="h1"
          fontWeight="var(--font-weight-bold)"
          letterSpacing="-0.03em"
          sx={{ mb: 3 }}
        >
          {result.goal}
        </Typography>

        <Typography
          variant="body1"
          color="text.primary"
          sx={{
            fontSize: "1.1rem",
            lineHeight: 1.7,
            opacity: 0.9,
            maxWidth: "800px",
          }}
        >
          {sanitizeText(result.summary)}
        </Typography>
      </Box>

      {/* Overview Metrics */}
      <Box>
        <EditorialSectionHeader
          title="Overview"
          description="High-level plan execution indicators from the GOAP planner."
        />
        <Grid container spacing={4}>
          <Grid item xs={6} md={4}>
            <Typography
              variant="caption"
              color="text.secondary"
              fontWeight={600}
              letterSpacing="0.05em"
              sx={{ textTransform: "uppercase" }}
            >
              Total Steps
            </Typography>
            <Typography variant="h3" fontWeight={700} sx={{ mt: 1 }}>
              {metrics.totalSteps}
            </Typography>
          </Grid>
          <Grid item xs={6} md={4}>
            <Typography
              variant="caption"
              color="text.secondary"
              fontWeight={600}
              letterSpacing="0.05em"
              sx={{ textTransform: "uppercase" }}
            >
              Active Agents
            </Typography>
            <Typography variant="h3" fontWeight={700} sx={{ mt: 1 }}>
              {metrics.activeAgents}
            </Typography>
          </Grid>
          <Grid item xs={12} md={4}>
            <Typography
              variant="caption"
              color="text.secondary"
              fontWeight={600}
              letterSpacing="0.05em"
              sx={{ textTransform: "uppercase" }}
            >
              Completed Steps
            </Typography>
            <Typography variant="h3" fontWeight={700} sx={{ mt: 1 }}>
              {metrics.completedSteps}
            </Typography>
          </Grid>
        </Grid>
      </Box>

      {/* Execution Graph */}
      <Box>
        <EditorialSectionHeader
          title="Execution Graph"
          description="Mermaid visualization of the execution dependency graph."
        />
        <Box
          sx={{
            p: 3,
            backgroundColor: "var(--color-surface)",
            border: "1px solid var(--color-border)",
            borderRadius: "var(--radius-md)",
            overflow: "auto",
          }}
        >
          <MermaidDiagramViewer
            title="GOAP Execution Graph"
            diagram={result.mermaidDiagram}
          />
        </Box>
      </Box>

      {/* Timeline Section */}
      <Box>
        <EditorialSectionHeader
          title="Timeline"
          description="Sequence of tasks executed by agents in chronological order."
        />
        <Stack spacing={4} sx={{ mt: 2 }}>
          {timeline.map((item, index) => {
            const isLast = index === timeline.length - 1;
            return (
              <Box
                key={item.id}
                sx={{
                  position: "relative",
                  pl: 4,
                  "&::before": {
                    content: '""',
                    position: "absolute",
                    left: 7,
                    top: 24,
                    bottom: isLast ? 0 : -28,
                    width: isLast ? 0 : "2px",
                    backgroundColor: "var(--color-border)",
                  },
                }}
              >
                {/* Stepper Dot */}
                <Box
                  sx={{
                    position: "absolute",
                    left: 0,
                    top: 4,
                    width: "16px",
                    height: "16px",
                    borderRadius: "50%",
                    border: "2px solid var(--color-accent)",
                    backgroundColor: "var(--color-background)",
                  }}
                />
                <Stack
                  direction={{ xs: "column", sm: "row" }}
                  justifyContent="space-between"
                  spacing={1}
                  sx={{ mb: 1 }}
                >
                  <Typography variant="subtitle1" fontWeight={600} color="text.primary">
                    {index + 1}. {item.title}
                  </Typography>
                  <Typography variant="caption" color="text.secondary" sx={{ fontWeight: 500 }}>
                    Agent: {item.agent} · Status: {item.status}
                  </Typography>
                </Stack>
                <Typography variant="body2" color="text.secondary" sx={{ lineHeight: 1.6 }}>
                  {item.details}
                </Typography>
              </Box>
            );
          })}
        </Stack>
      </Box>

      {/* Action Steps Section */}
      <Box>
        <EditorialSectionHeader
          title="Action Steps"
          description="Readable list of raw plan steps with pre/post status."
        />
        <Box
          sx={{
            p: 3,
            backgroundColor: "var(--color-surface)",
            border: "1px solid var(--color-border)",
            borderRadius: "var(--radius-md)",
          }}
        >
          <PlanSteps steps={result.steps} />
        </Box>
      </Box>

      {/* Trace Snapshot Section */}
      {result.trace?.length ? (
        <Box>
          <EditorialSectionHeader
            title="Planner Trace"
            description="Details of preconditions, effects, and missing states checked by the GOAP planner."
          />
          <Stack spacing={4}>
            {result.trace.map((entry, index) => (
              <Box
                key={`${entry.action}-${index}`}
                sx={{
                  p: 3,
                  backgroundColor: "var(--color-surface)",
                  border: "1px solid var(--color-border)",
                  borderRadius: "var(--radius-md)",
                }}
              >
                <Typography
                  variant="subtitle1"
                  fontWeight={600}
                  color="primary"
                  sx={{ mb: 2.5 }}
                >
                  Action: {entry.action}
                </Typography>

                <Grid container spacing={3}>
                  <Grid item xs={12} md={6}>
                    <Typography variant="body2" fontWeight={600} sx={{ mb: 1 }}>
                      Preconditions Checked
                    </Typography>
                    <Stack direction="row" spacing={1} flexWrap="wrap" useFlexGap sx={{ mb: 2.5 }}>
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
                        <Typography variant="body2" fontWeight={600} color="error" sx={{ mb: 1 }}>
                          Missing Preconditions
                        </Typography>
                        <Stack direction="row" spacing={1} flexWrap="wrap" useFlexGap sx={{ mb: 2.5 }}>
                          {entry.missing_preconditions.map((cond) => (
                            <Chip key={cond} label={cond} size="small" color="error" variant="outlined" />
                          ))}
                        </Stack>
                      </>
                    )}

                    <Typography variant="body2" fontWeight={600} sx={{ mb: 1 }}>
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
                    <Typography variant="body2" fontWeight={600} sx={{ mb: 1 }}>
                      State Before Action
                    </Typography>
                    <Stack direction="row" spacing={1} flexWrap="wrap" useFlexGap sx={{ mb: 2.5 }}>
                      {entry.state_before?.length > 0 ? (
                        entry.state_before.map((state) => (
                          <Chip key={state} label={state} size="small" />
                        ))
                      ) : (
                        <Typography variant="caption" color="text.disabled">Empty</Typography>
                      )}
                    </Stack>

                    <Typography variant="body2" fontWeight={600} sx={{ mb: 1 }}>
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
        </Box>
      ) : null}
    </Stack>
  );
}

export const MemoizedPlannerDashboard = memo(PlannerDashboard);

