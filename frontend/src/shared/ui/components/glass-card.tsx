import { Box, type BoxProps } from "@mui/material";

export type GlassCardProps = BoxProps;

export function GlassCard({ children, sx, ...props }: GlassCardProps) {
  return (
    <Box
      sx={{
        backgroundColor: "var(--color-glass-bg)",
        backdropFilter: "blur(16px)",
        WebkitBackdropFilter: "blur(16px)",
        border: "1px solid var(--color-border)",
        borderRadius: "var(--radius-md)",
        p: 4, // increased padding slightly for breathing room
        transition: "all var(--transition-normal)",
        boxShadow: "var(--shadow-subtle)",
        "&:hover": {
          borderColor: "var(--color-border-hover)",
        },
        ...sx,
      }}
      {...props}
    >
      {children}
    </Box>
  );
}
