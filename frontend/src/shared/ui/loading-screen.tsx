import { Stack, Typography } from "@mui/material";

import { APP_NAME } from "@shared/constants/app";
import { ElegantSpinner } from "@shared/ui/feedback/loading-state";

export function LoadingScreen() {
  return (
    <Stack alignItems="center" justifyContent="center" spacing={2.5} sx={{ minHeight: "100dvh" }}>
      <ElegantSpinner />
      <Typography variant="body2" color="text.secondary" sx={{ fontWeight: 500, letterSpacing: "-0.01em" }}>
        Loading {APP_NAME}...
      </Typography>
    </Stack>
  );
}
