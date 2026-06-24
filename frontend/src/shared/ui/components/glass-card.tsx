import { Box, type BoxProps } from "@mui/material";

export type GlassCardProps = BoxProps;

export function GlassCard({ children, sx, ...props }: GlassCardProps) {
  return (
    <Box
      sx={{
        backgroundColor: (theme) =>
          theme.palette.mode === "dark" ? "rgba(10, 15, 30, 0.65)" : "rgba(255, 255, 255, 0.7)",
        backgroundImage: (theme) =>
          theme.palette.mode === "dark"
            ? "linear-gradient(to bottom right, rgba(16, 22, 40, 0.6), rgba(10, 15, 30, 0.45))"
            : "linear-gradient(to bottom right, rgba(255, 255, 255, 0.75), rgba(243, 244, 246, 0.55))",
        backdropFilter: "blur(20px)",
        WebkitBackdropFilter: "blur(20px)",
        border: "1px solid var(--color-border)",
        borderRadius: "var(--radius-md)",
        p: 3.5, // slightly more unified padding
        transition: "all var(--transition-normal)",
        boxShadow: (theme) =>
          theme.palette.mode === "dark"
            ? "var(--shadow-premium-dark)"
            : "var(--shadow-premium)",
        "&:hover": {
          borderColor: "var(--color-border-hover)",
          transform: "translateY(-2px)",
          boxShadow: (theme) =>
            theme.palette.mode === "dark"
              ? "0 20px 45px rgba(0, 0, 0, 0.45)"
              : "0 12px 30px rgba(0, 0, 0, 0.06)",
        },
        ...sx,
      }}
      {...props}
    >
      {children}
    </Box>
  );
}
