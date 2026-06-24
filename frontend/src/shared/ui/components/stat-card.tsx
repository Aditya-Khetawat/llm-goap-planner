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
        p: 3,
        backgroundColor: "var(--color-surface)",
        backdropFilter: "blur(20px)",
        WebkitBackdropFilter: "blur(20px)",
        border: "1px solid var(--color-border)",
        borderRadius: "var(--radius-md)",
        transition: "all var(--transition-normal)",
        boxShadow: (theme) =>
          theme.palette.mode === "dark"
            ? "var(--shadow-premium-dark)"
            : "var(--shadow-premium)",
        height: "100%",
        display: "flex",
        flexDirection: "column",
        justifyContent: "center",
        "&:hover": {
          borderColor: "var(--color-border-hover)",
          transform: "translateY(-2px)",
          boxShadow: (theme) =>
            theme.palette.mode === "dark"
              ? "0 12px 30px rgba(0, 0, 0, 0.4)"
              : "0 8px 20px rgba(0, 0, 0, 0.05)",
        },
      }}
    >
      <Typography
        variant="caption"
        color="text.secondary"
        fontWeight={600}
        letterSpacing="0.06em"
        sx={{ textTransform: "uppercase", display: "block", opacity: 0.7 }}
      >
        {label}
      </Typography>
      <Typography
        fontWeight={800}
        sx={{ 
          mt: 1, 
          fontSize: { xs: "1.75rem", md: "2.25rem" }, 
          color: "var(--color-text-primary)", 
          letterSpacing: "-0.04em",
          lineHeight: 1,
          background: (theme) =>
            theme.palette.mode === "dark"
              ? "linear-gradient(135deg, #FFFFFF 70%, #967CFF 100%)"
              : "linear-gradient(135deg, #111827 70%, #7C5CFF 100%)",
          WebkitBackgroundClip: "text",
          WebkitTextFillColor: "transparent",
        }}
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
