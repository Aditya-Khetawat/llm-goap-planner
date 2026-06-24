import { Box, Stack, Typography } from "@mui/material";
import type { PropsWithChildren, ReactNode } from "react";

const ElegantSpinner = () => (
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

export interface LoadingStateProps extends PropsWithChildren {
  title?: ReactNode;
  description?: ReactNode;
}

export function LoadingState({ title, description, children }: LoadingStateProps) {
  return (
    <Stack spacing={3.5} alignItems="center" justifyContent="center" sx={{ py: 10, width: "100%" }}>
      <ElegantSpinner />
      {title && (
        <Typography variant="h6" component="h2" fontWeight={700} color="text.primary">
          {title}
        </Typography>
      )}
      {description && (
        <Typography
          variant="body2"
          color="text.secondary"
          sx={{ textAlign: "center", maxWidth: 480, lineHeight: 1.6 }}
        >
          {description}
        </Typography>
      )}
      {children && (
        <Stack spacing={1.5} sx={{ width: "100%", maxWidth: 720, mt: 2 }}>
          {children}
        </Stack>
      )}
    </Stack>
  );
}
