import { Box, Typography } from "@mui/material";
import { sanitizeText } from "@shared/lib/sanitize";

export interface AgentBadgeProps {
  agent: string;
}

export function AgentBadge({ agent }: AgentBadgeProps) {
  return (
    <Box
      sx={{
        display: "inline-flex",
        alignItems: "center",
        gap: 1,
        px: 1.5,
        py: 0.5,
        borderRadius: "var(--radius-xs)",
        backgroundColor: "var(--color-item-hover)",
        border: "1px solid var(--color-border)",
      }}
    >
      <Box
        sx={{
          width: 6,
          height: 6,
          borderRadius: "50%",
          backgroundColor: "var(--color-accent)",
        }}
      />
      <Typography variant="caption" fontWeight={600} color="text.primary">
        {sanitizeText(agent)}
      </Typography>
    </Box>
  );
}
