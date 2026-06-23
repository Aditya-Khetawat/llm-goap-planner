import { Stack, Typography } from "@mui/material";

interface PlannerPageHeaderProps {
  eyebrow: string;
  title: string;
  description: string;
}

export function PlannerPageHeader({ eyebrow, title, description }: PlannerPageHeaderProps) {
  return (
    <Stack spacing={2} sx={{ mb: 4 }}>
      <Typography variant="overline" color="text.secondary">
        {eyebrow}
      </Typography>
      <Typography variant="h3" component="h1" fontWeight={800}>
        {title}
      </Typography>
      <Typography variant="body1" color="text.secondary" maxWidth={760}>
        {description}
      </Typography>
    </Stack>
  );
}
