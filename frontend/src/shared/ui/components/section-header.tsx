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
            variant="h4"
            component="h2"
            fontWeight="var(--font-weight-semibold)"
            letterSpacing="-0.02em"
          >
            {title}
          </Typography>
          {description && (
            <Typography variant="body2" color="text.secondary">
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
