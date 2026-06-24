import { Box, Typography } from "@mui/material";

export interface StatCardProps {
  label: string;
  value: string | number;
  description?: string;
}

export function StatCard({ label, value, description }: StatCardProps) {
  return (
    <Box
      sx={{
        p: 3.5, // slightly more padding
        backgroundColor: "var(--color-surface)",
        backdropFilter: "blur(16px)",
        WebkitBackdropFilter: "blur(16px)",
        border: "1px solid var(--color-border)",
        borderRadius: "var(--radius-md)",
        transition: "all var(--transition-normal)",
        boxShadow: "var(--shadow-subtle)",
        height: "100%",
        display: "flex",
        flexDirection: "column",
        justifyContent: "center",
        "&:hover": {
          borderColor: "var(--color-border-hover)",
        },
      }}
    >
      <Typography
        variant="caption"
        color="text.secondary"
        fontWeight={600}
        letterSpacing="0.05em"
        sx={{ textTransform: "uppercase", display: "block" }}
      >
        {label}
      </Typography>
      <Typography
        variant="h3"
        fontWeight={700}
        sx={{ mt: 1, color: "var(--color-text-primary)", letterSpacing: "-0.02em" }}
      >
        {value}
      </Typography>
      {description && (
        <Typography variant="caption" color="text.secondary" sx={{ mt: 1, display: "block" }}>
          {description}
        </Typography>
      )}
    </Box>
  );
}
