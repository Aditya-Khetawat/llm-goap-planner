import { Box, Stack, Typography } from "@mui/material";
import type { ReactNode } from "react";
import { AppButton } from "@shared/ui/components/button";

export interface ErrorStateProps {
  title: string;
  description: string;
  onRetry?: () => void;
  onStartOver?: () => void;
  retryText?: string;
  startOverText?: string;
  action?: ReactNode;
}

const ErrorIconSvg = () => (
  <Box
    sx={{
      display: "flex",
      alignItems: "center",
      justifyContent: "center",
      width: 44,
      height: 44,
      borderRadius: "50%",
      backgroundColor: "rgba(239, 68, 68, 0.1)",
      color: "var(--color-danger)",
      flexShrink: 0,
    }}
  >
    <svg
      width="20"
      height="20"
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="2"
      strokeLinecap="round"
      strokeLinejoin="round"
    >
      <circle cx="12" cy="12" r="10" />
      <line x1="12" y1="8" x2="12" y2="12" />
      <line x1="12" y1="16" x2="12.01" y2="16" />
    </svg>
  </Box>
);

export function ErrorState({
  title,
  description,
  onRetry,
  onStartOver,
  retryText = "Retry",
  startOverText = "Start over",
  action,
}: ErrorStateProps) {
  return (
    <Stack spacing={4} alignItems="center" justifyContent="center" sx={{ py: 6, width: "100%" }}>
      <Box
        sx={{
          width: "100%",
          maxWidth: 720,
          p: 3.5,
          backgroundColor: (theme) =>
            theme.palette.mode === "dark"
              ? "rgba(239, 68, 68, 0.02)"
              : "rgba(239, 68, 68, 0.01)",
          backgroundImage: (theme) =>
            theme.palette.mode === "dark"
              ? "linear-gradient(to bottom right, rgba(239, 68, 68, 0.03), rgba(10, 15, 30, 0.2))"
              : "linear-gradient(to bottom right, rgba(239, 68, 68, 0.01), rgba(255, 255, 255, 0.8))",
          border: "1px solid rgba(239, 68, 68, 0.15)",
          borderRadius: "var(--radius-md)",
          backdropFilter: "blur(20px)",
          position: "relative",
          overflow: "hidden",
          "&::before": {
            content: '""',
            position: "absolute",
            left: 0,
            top: 0,
            bottom: 0,
            width: "3px",
            backgroundColor: "var(--color-danger)",
          },
        }}
      >
        <Stack direction={{ xs: "column", sm: "row" }} spacing={2.5} alignItems={{ xs: "flex-start", sm: "center" }}>
          <ErrorIconSvg />
          <Box sx={{ flex: 1 }}>
            <Typography variant="h3" component="div" color="text.primary" sx={{ mb: 0.5 }}>
              {title}
            </Typography>
            <Typography variant="body2" color="text.secondary" sx={{ lineHeight: 1.6 }}>
              {description}
            </Typography>
          </Box>
        </Stack>
      </Box>
      {(onRetry || onStartOver || action) && (
        <Stack direction={{ xs: "column", sm: "row" }} spacing={2}>
          {onRetry && (
            <AppButton variant="contained" onClick={onRetry}>
              {retryText}
            </AppButton>
          )}
          {onStartOver && (
            <AppButton variant="outlined" onClick={onStartOver}>
              {startOverText}
            </AppButton>
          )}
          {action}
        </Stack>
      )}
    </Stack>
  );
}

