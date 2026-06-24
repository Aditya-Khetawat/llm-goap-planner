import { Box, Stack, Typography } from "@mui/material";
import type { PropsWithChildren, ReactNode } from "react";

export const ElegantSpinner = () => (
  <Box
    sx={{
      display: "inline-flex",
      position: "relative",
      alignItems: "center",
      justifyContent: "center",
      mb: 1,
      width: 56,
      height: 56,
    }}
  >
    {/* Outer pulsing ring */}
    <Box
      sx={{
        position: "absolute",
        width: 52,
        height: 52,
        borderRadius: "50%",
        border: "2px solid rgba(124, 92, 255, 0.2)",
        animation: "pulse 2s infinite ease-in-out",
        "@keyframes pulse": {
          "0%, 100%": { transform: "scale(0.9)", opacity: 0.2 },
          "50%": { transform: "scale(1.15)", opacity: 0.6 },
        },
      }}
    />
    {/* Inner rotating ring */}
    <Box
      sx={{
        position: "absolute",
        width: 32,
        height: 32,
        borderRadius: "50%",
        border: "2px solid transparent",
        borderTopColor: "var(--color-accent)",
        borderRightColor: "var(--color-accent)",
        animation: "spin 0.8s infinite linear",
        "@keyframes spin": {
          "0%": { transform: "rotate(0deg)" },
          "100%": { transform: "rotate(360deg)" },
        },
      }}
    />
  </Box>
);

import { useEffect, useState } from "react";

const LOADING_STEPS = [
  "Decomposing goal into executable subtasks...",
  "Analyzing environment state variables...",
  "Evaluating preconditions and effects dependencies...",
  "Constructing GOAP action network...",
  "Running backwards A* state search...",
  "Optimizing plan steps cost paths...",
  "Resolving conflicts and state constraints...",
  "Synthesizing final execution dependency graph..."
];

export interface LoadingStateProps extends PropsWithChildren {
  title?: ReactNode;
  description?: ReactNode;
}

export function LoadingState({ title, description, children }: LoadingStateProps) {
  const [stepIndex, setStepIndex] = useState(0);

  useEffect(() => {
    const timer = setInterval(() => {
      setStepIndex((prev) => (prev + 1) % LOADING_STEPS.length);
    }, 2500);
    return () => clearInterval(timer);
  }, []);

  return (
    <Stack spacing={3} alignItems="center" justifyContent="center" sx={{ py: 6, width: "100%" }}>
      <ElegantSpinner />
      {title && (
        <Typography variant="h3" component="h2" color="text.primary">
          {title}
        </Typography>
      )}
      
      <Typography
        className="loading-text-pulse"
        variant="body2"
        sx={{ 
          textAlign: "center", 
          maxWidth: 480, 
          lineHeight: 1.6,
          fontWeight: 600,
          color: "var(--color-accent)",
          minHeight: "22px"
        }}
      >
        {LOADING_STEPS[stepIndex]}
      </Typography>

      {description && (
        <Typography
          variant="caption"
          color="text.secondary"
          sx={{ textAlign: "center", maxWidth: 440, opacity: 0.7 }}
        >
          {description}
        </Typography>
      )}
      {children && (
        <Stack spacing={2} sx={{ width: "100%", maxWidth: 720, mt: 2.5 }}>
          {children}
        </Stack>
      )}
    </Stack>
  );
}
