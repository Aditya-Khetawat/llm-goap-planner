import { Box, Stack, Typography } from "@mui/material";
import type { ReactNode } from "react";

export interface PageHeaderProps {
  title: ReactNode;
  description?: ReactNode;
  eyebrow?: ReactNode;
  action?: ReactNode;
  align?: "left" | "center";
}

export function PageHeader({
  title,
  description,
  eyebrow,
  action,
  align = "left",
}: PageHeaderProps) {
  const isCentered = align === "center";

  return (
    <Stack
      direction={{ xs: "column", md: action ? "row" : "column" }}
      justifyContent={action ? "space-between" : "flex-start"}
      alignItems={action ? { xs: "flex-start", md: "center" } : isCentered ? "center" : "flex-start"}
      spacing={2.5}
      sx={{
        mb: "var(--spacing-xxl)",
        width: "100%",
        textAlign: isCentered ? "center" : "left",
      }}
    >
      <Stack spacing={1} alignItems={isCentered ? "center" : "flex-start"} sx={{ flex: 1 }}>
        {eyebrow && (
          <Typography
            variant="caption"
            color="primary"
            fontWeight={600}
            letterSpacing="0.05em"
            sx={{ textTransform: "uppercase" }}
          >
            {eyebrow}
          </Typography>
        )}
        <Typography
          variant="h1"
          component="h1"
        >
          {title}
        </Typography>
        {description && (
          <Typography
            variant="body1"
            color="text.secondary"
            maxWidth={680}
            sx={{ mt: 1.5 }}
          >
            {description}
          </Typography>
        )}
      </Stack>
      {action && (
        <Box sx={{ alignSelf: { xs: "flex-start", md: "center" }, mt: { xs: 1, md: 0 } }}>
          {action}
        </Box>
      )}
    </Stack>
  );
}
