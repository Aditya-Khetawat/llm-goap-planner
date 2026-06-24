import { Box, Grid, Stack, Typography, Chip } from "@mui/material";
import { memo, useMemo } from "react";

import { MermaidDiagramViewer } from "@shared/ui/visualization/mermaid-diagram-viewer";
import { PlanSteps } from "@features/planner/components/plan-steps";
import type { PlannerGeneratePlanResponse } from "@features/planner/model/planner.models";
import {
  buildPlannerMetrics,
  buildPlannerTimeline,
} from "@features/planner/dashboard/planner-dashboard.utils";
import { sanitizeText } from "@shared/lib/sanitize";
import { SectionHeader } from "@shared/ui/components/section-header";
import { StatCard } from "@shared/ui/components/stat-card";
import { StatusBadge } from "@shared/ui/components/status-badge";
import { AgentBadge } from "@shared/ui/components/agent-badge";
import { TimelineCard } from "@shared/ui/components/timeline-card";
import { GlassCard } from "@shared/ui/components/glass-card";

interface PlannerDashboardProps {
  result: PlannerGeneratePlanResponse;
}


export function PlannerDashboard({ result }: PlannerDashboardProps) {
  const metrics = useMemo(() => buildPlannerMetrics(result), [result]);
  const timeline = useMemo(() => buildPlannerTimeline(result), [result]);

  return (
    <Stack spacing={6} sx={{ width: "100%", py: 2 }}>
      {/* Hero Section */}
      <Box>
        <Stack direction="row" spacing={2} alignItems="center" sx={{ mb: 1.5 }}>
          <StatusBadge status={result.status} />
          {result.classification ? (
            <Chip
              label={sanitizeText(result.classification)}
              size="small"
              variant="outlined"
              sx={{ opacity: 0.8, textTransform: "uppercase", fontWeight: 600, fontSize: "0.72rem" }}
            />
          ) : null}
          <Typography variant="caption" color="text.secondary">
            Source: {sanitizeText(result.source)}
          </Typography>
        </Stack>

        <Typography
          variant="h1"
          component="h1"
          sx={{ mb: 2 }}
        >
          {result.goal}
        </Typography>

        <Typography
          variant="body1"
          color="text.secondary"
          sx={{
            maxWidth: "800px",
            mt: 1.5,
            lineHeight: 1.6,
          }}
        >
          {sanitizeText(result.summary)}
        </Typography>
      </Box>

      {/* Overview Metrics */}
      <Box>
        <SectionHeader
          title="Overview"
          description="High-level plan execution indicators from the GOAP planner."
        />
        <Grid container spacing={3}>
          <Grid item xs={6} md={4}>
            <StatCard label="Total Steps" value={metrics.totalSteps} />
          </Grid>
          <Grid item xs={6} md={4}>
            <StatCard label="Active Agents" value={metrics.activeAgents} />
          </Grid>
          <Grid item xs={12} md={4}>
            <StatCard label="Completed Steps" value={metrics.completedSteps} />
          </Grid>
        </Grid>
      </Box>

      {/* Execution Graph */}
      <Box>
        <SectionHeader
          title="Execution Graph"
          description="Mermaid visualization of the execution dependency graph."
        />
        <GlassCard sx={{ overflow: "auto", p: 3 }}>
          <MermaidDiagramViewer
            diagram={result.mermaidDiagram}
          />
        </GlassCard>
      </Box>

      {/* Timeline Section */}
      <Box>
        <SectionHeader
          title="Timeline"
          description="Sequence of tasks executed by agents in chronological order."
        />
        <Stack spacing={2} sx={{ mt: 1 }}>
          {timeline.map((item, index) => {
            const isLast = index === timeline.length - 1;
            return (
              <TimelineCard
                key={item.id}
                index={index}
                title={item.title}
                isLast={isLast}
                subtitle={
                  <>
                    <AgentBadge agent={item.agent} />
                    <StatusBadge status={item.status} />
                  </>
                }
                details={item.details}
              />
            );
          })}
        </Stack>
      </Box>

      {/* Action Steps Section */}
      <Box>
        <SectionHeader
          title="Action Steps"
          description="Readable list of raw plan steps with pre/post status."
        />
        <GlassCard sx={{ p: 3 }}>
          <PlanSteps steps={result.steps} />
        </GlassCard>
      </Box>

      {/* Trace Snapshot Section */}
      {result.trace?.length ? (
        <Box>
          <SectionHeader
            title="Planner Trace"
            description="Details of preconditions, effects, and missing states checked by the GOAP planner."
          />
          <Stack spacing={2}>
            {result.trace.map((entry, index) => (
              <GlassCard key={`${entry.action}-${index}`} sx={{ p: 2.5 }}>
                <Typography
                  variant="subtitle2"
                  fontWeight={600}
                  color="primary"
                  sx={{ mb: 2, textTransform: "uppercase", letterSpacing: "0.02em" }}
                >
                  Action: {entry.action}
                </Typography>

                <Grid container spacing={3}>
                  <Grid item xs={12} md={6}>
                    <Typography variant="body2" fontWeight={600} sx={{ mb: 1 }}>
                      Preconditions Checked
                    </Typography>
                    <Stack direction="row" spacing={1} flexWrap="wrap" useFlexGap sx={{ mb: 3 }}>
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
                        <Stack direction="row" spacing={1} flexWrap="wrap" useFlexGap sx={{ mb: 3 }}>
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
                    <Stack direction="row" spacing={1} flexWrap="wrap" useFlexGap sx={{ mb: 3 }}>
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
              </GlassCard>
            ))}
          </Stack>
        </Box>
      ) : null}
    </Stack>
  );
}

export const MemoizedPlannerDashboard = memo(PlannerDashboard);


