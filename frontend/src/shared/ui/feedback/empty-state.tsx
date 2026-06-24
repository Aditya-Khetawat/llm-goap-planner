import { Box, Stack, Typography } from "@mui/material";
import type { ReactNode } from "react";

export interface EmptyStateProps {
  title: string;
  description: string;
  action?: ReactNode;
}

const EmptyIconSvg = () => (
  <Box
    sx={{
      mb: 2,
      display: "flex",
      alignItems: "center",
      justifyContent: "center",
      width: 56,
      height: 56,
      borderRadius: "50%",
      border: "1px dashed var(--color-border)",
      backgroundColor: (theme) =>
        theme.palette.mode === "dark" ? "rgba(124, 92, 255, 0.04)" : "rgba(124, 92, 255, 0.02)",
      color: "var(--color-accent)",
    }}
  >
    <svg
      width="24"
      height="24"
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="1.5"
      strokeLinecap="round"
      strokeLinejoin="round"
    >
      <path d="M4 19.5v-15A2.5 2.5 0 0 1 6.5 2H20v20H6.5a2.5 2.5 0 0 1-2.5-2.5Z" />
      <path d="M6 6h10" />
      <path d="M6 10h10" />
      <path d="M13 14h3" />
    </svg>
  </Box>
);

export function EmptyState({ title, description, action }: EmptyStateProps) {
  return (
    <Stack
      alignItems="center"
      justifyContent="center"
      spacing={1.5}
      sx={{ 
        py: 8, 
        px: 4,
        textAlign: "center",
        border: "1px dashed var(--color-border)",
        borderRadius: "var(--radius-md)",
        backgroundColor: (theme) =>
          theme.palette.mode === "dark" ? "rgba(255, 255, 255, 0.01)" : "rgba(0, 0, 0, 0.005)",
        maxWidth: 640,
        mx: "auto",
        width: "100%",
      }}
    >
      <EmptyIconSvg />
      <Typography variant="h3" component="h2" color="text.primary">
        {title}
      </Typography>
      <Typography variant="body2" color="text.secondary" maxWidth={440} sx={{ lineHeight: 1.6 }}>
        {description}
      </Typography>
      {action && <Box sx={{ mt: 1 }}>{action}</Box>}
    </Stack>
  );
}

