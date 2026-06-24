import { Box, Stack, Typography } from "@mui/material";
import type { ReactNode } from "react";

export interface EmptyStateProps {
  title: string;
  description: string;
  action?: ReactNode;
}

export function EmptyState({ title, description, action }: EmptyStateProps) {
  return (
    <Stack
      alignItems="center"
      justifyContent="center"
      spacing={2}
      sx={{ py: 8, textAlign: "center" }}
    >
      <Typography variant="h6" component="h2" fontWeight={600} color="text.primary">
        {title}
      </Typography>
      <Typography variant="body2" color="text.secondary" maxWidth={520}>
        {description}
      </Typography>
      {action && <Box sx={{ mt: 1 }}>{action}</Box>}
    </Stack>
  );
}

