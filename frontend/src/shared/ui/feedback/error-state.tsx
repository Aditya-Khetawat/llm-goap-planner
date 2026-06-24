import { Alert, Stack, Typography } from "@mui/material";
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
    <Stack spacing={3} alignItems="center" justifyContent="center" sx={{ py: 8, width: "100%" }}>
      <Alert severity="error" variant="outlined" sx={{ width: "100%", maxWidth: 720 }}>
        <Typography variant="subtitle1" component="div" fontWeight={700} gutterBottom>
          {title}
        </Typography>
        <Typography variant="body2">{description}</Typography>
      </Alert>
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

