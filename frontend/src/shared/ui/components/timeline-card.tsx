import { Box, Stack, Typography } from "@mui/material";
import type { ReactNode } from "react";

export interface TimelineCardProps {
  index: number;
  title: string;
  subtitle?: ReactNode;
  details?: ReactNode;
  isLast?: boolean;
}

export function TimelineCard({
  index,
  title,
  subtitle,
  details,
  isLast = false,
}: TimelineCardProps) {
  return (
    <Box
      sx={{
        position: "relative",
        pl: 4,
        width: "100%",
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
          left: "2px",
          top: "6px",
          width: "12px",
          height: "12px",
          borderRadius: "50%",
          border: "2.5px solid var(--color-accent)",
          backgroundColor: "var(--color-background)",
          boxShadow: "0 0 0 3.5px rgba(124, 92, 255, 0.12)",
        }}
      />
      <Stack
        direction={{ xs: "column", sm: "row" }}
        justifyContent="space-between"
        alignItems={{ xs: "flex-start", sm: "center" }}
        spacing={1}
        sx={{ mb: 1 }}
      >
        <Typography variant="subtitle1" fontWeight={600} color="text.primary">
          {index + 1}. {title}
        </Typography>
        {subtitle && (
          <Box sx={{ display: "flex", alignItems: "center", gap: 1.5 }}>
            {subtitle}
          </Box>
        )}
      </Stack>
      {details && (
        <Typography variant="body2" color="text.secondary" sx={{ lineHeight: 1.6 }}>
          {details}
        </Typography>
      )}
    </Box>
  );
}
