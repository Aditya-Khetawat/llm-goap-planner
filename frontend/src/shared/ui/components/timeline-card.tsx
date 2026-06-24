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
        p: 2,
        pl: 5,
        borderRadius: "var(--radius-sm)",
        backgroundColor: "transparent",
        transition: "all var(--transition-fast)",
        "&:hover": {
          backgroundColor: "var(--color-item-hover)",
          "& .stepper-dot": {
            boxShadow: "0 0 0 5px rgba(124, 92, 255, 0.22)",
            borderColor: "var(--color-accent)",
          },
          "&::before": {
            backgroundColor: "var(--color-accent)",
            opacity: 0.35,
          },
        },
        "&::before": {
          content: '""',
          position: "absolute",
          left: "21px",
          top: "34px",
          bottom: isLast ? 0 : -20,
          width: isLast ? 0 : "2px",
          backgroundColor: "var(--color-border)",
          transition: "all var(--transition-fast)",
        },
      }}
    >
      {/* Stepper Dot */}
      <Box
        className="stepper-dot"
        sx={{
          position: "absolute",
          left: "16px",
          top: "20px",
          width: "12px",
          height: "12px",
          borderRadius: "50%",
          border: "2.5px solid rgba(124, 92, 255, 0.6)",
          backgroundColor: "var(--color-background)",
          boxShadow: "0 0 0 3.5px rgba(124, 92, 255, 0.08)",
          transition: "all var(--transition-fast)",
        }}
      />
      <Stack
        direction={{ xs: "column", sm: "row" }}
        justifyContent="space-between"
        alignItems={{ xs: "flex-start", sm: "center" }}
        spacing={1}
        sx={{ mb: 1 }}
      >
        <Typography variant="body1" fontWeight={600} color="text.primary">
          {index + 1}. {title}
        </Typography>
        {subtitle && (
          <Box sx={{ display: "flex", alignItems: "center", gap: 1.5 }}>
            {subtitle}
          </Box>
        )}
      </Stack>
      {details && (
        <Typography variant="body2" color="text.secondary" sx={{ opacity: 0.85, pl: 0.5 }}>
          {details}
        </Typography>
      )}
    </Box>
  );
}
