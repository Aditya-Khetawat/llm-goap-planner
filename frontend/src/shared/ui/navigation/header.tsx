import { IconButton, Stack, Typography } from "@mui/material";

import { APP_NAME } from "@shared/constants/app";

export interface AppHeaderProps {
  onMenuClick?: () => void;
}

const MenuIconSvg = () => (
  <svg
    width="20"
    height="20"
    viewBox="0 0 24 24"
    fill="none"
    stroke="currentColor"
    strokeWidth="2"
    strokeLinecap="round"
    strokeLinejoin="round"
  >
    <line x1="3" y1="12" x2="21" y2="12" />
    <line x1="3" y1="6" x2="21" y2="6" />
    <line x1="3" y1="18" x2="21" y2="18" />
  </svg>
);

export function AppHeader({ onMenuClick }: AppHeaderProps) {
  return (
    <Stack
      direction="row"
      alignItems="center"
      justifyContent="space-between"
      sx={{ width: "100%", px: 3, py: 1.5 }}
    >
      <Stack direction="row" spacing={1.5} alignItems="center">
        {onMenuClick ? (
          <IconButton
            aria-label="Open navigation drawer"
            onClick={onMenuClick}
            edge="start"
            sx={{ display: { md: "none" }, color: "text.primary" }}
          >
            <MenuIconSvg />
          </IconButton>
        ) : null}
        <Typography
          variant="h6"
          component="div"
          fontWeight={800}
          letterSpacing="-0.02em"
          sx={{
            background: "linear-gradient(135deg, #FFFFFF 40%, #967CFF 100%)",
            WebkitBackgroundClip: "text",
            WebkitTextFillColor: "transparent",
            fontSize: "1.15rem",
          }}
        >
          {APP_NAME}
        </Typography>
      </Stack>
    </Stack>
  );
}
