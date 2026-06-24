import { Box, Divider, Stack, Typography } from "@mui/material";
import type { ReactNode } from "react";

export interface SectionHeaderProps {
  title: ReactNode;
  description?: ReactNode;
  action?: ReactNode;
  divider?: boolean;
}

export function SectionHeader({
  title,
  description,
  action,
  divider = true,
}: SectionHeaderProps) {
  return (
    <Box sx={{ mb: 5, width: "100%" }}>
      <Stack
        direction="row"
        justifyContent="space-between"
        alignItems="flex-end"
        spacing={2}
        sx={{ mb: 1 }}
      >
        <Stack spacing={0.5} sx={{ flex: 1 }}>
          <Typography
            variant="h2"
            component="h2"
          >
            {title}
          </Typography>
          {description && (
            <Typography variant="body1" color="text.secondary" sx={{ mt: 1 }}>
              {description}
            </Typography>
          )}
        </Stack>
        {action && <Box sx={{ mb: 0.5 }}>{action}</Box>}
      </Stack>
      {divider && <Divider sx={{ mt: 2, borderColor: "var(--color-border)" }} />}
    </Box>
  );
}
