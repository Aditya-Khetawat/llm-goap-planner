import { Grid, Stack, Typography } from "@mui/material";
import { AppCard, AppCardContent, AppCardHeader } from "./card";

export interface MetadataItem {
  label: string;
  value: string | number;
}

export interface MetadataCardProps {
  title: string;
  subtitle?: string;
  items: MetadataItem[];
}

export function MetadataCard({ title, subtitle, items }: MetadataCardProps) {
  return (
    <AppCard variant="outlined">
      <AppCardHeader title={title} subheader={subtitle} />
      <AppCardContent>
        <Grid container spacing={3}>
          {items.map((item) => (
            <Grid item xs={12} sm={6} md={4} key={item.label}>
              <Stack spacing={0.5}>
                <Typography
                  variant="overline"
                  color="text.secondary"
                  fontWeight={600}
                  letterSpacing="0.05em"
                  sx={{ textTransform: "uppercase" }}
                >
                  {item.label}
                </Typography>
                <Typography variant="body1" fontWeight={600} color="text.primary">
                  {item.value}
                </Typography>
              </Stack>
            </Grid>
          ))}
        </Grid>
      </AppCardContent>
    </AppCard>
  );
}
