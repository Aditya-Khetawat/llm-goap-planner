import { IconButton, Stack, Typography } from "@mui/material";

import { APP_NAME } from "@shared/constants/app";
import { useThemeMode } from "@shared/hooks/use-theme-mode";

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

const SunIconSvg = () => (
  <svg
    width="16"
    height="16"
    viewBox="0 0 24 24"
    fill="none"
    stroke="currentColor"
    strokeWidth="2"
    strokeLinecap="round"
    strokeLinejoin="round"
  >
    <circle cx="12" cy="12" r="4" />
    <path d="M12 2v2" />
    <path d="M12 20v2" />
    <path d="m4.93 4.93 1.41 1.41" />
    <path d="m17.66 17.66 1.41 1.41" />
    <path d="M2 12h2" />
    <path d="M20 12h2" />
    <path d="m6.34 17.66-1.41 1.41" />
    <path d="m19.07 4.93-1.41 1.41" />
  </svg>
);

const MoonIconSvg = () => (
  <svg
    width="16"
    height="16"
    viewBox="0 0 24 24"
    fill="none"
    stroke="currentColor"
    strokeWidth="2"
    strokeLinecap="round"
    strokeLinejoin="round"
  >
    <path d="M12 3a6 6 0 0 0 9 9 9 9 0 1 1-9-9Z" />
  </svg>
);

export function AppHeader({ onMenuClick }: AppHeaderProps) {
  const { isDarkMode, toggleThemeMode } = useThemeMode();

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
            background: (theme) =>
              theme.palette.mode === "dark"
                ? "linear-gradient(135deg, #FFFFFF 40%, #967CFF 100%)"
                : "linear-gradient(135deg, #111827 40%, #7C5CFF 100%)",
            WebkitBackgroundClip: "text",
            WebkitTextFillColor: "transparent",
            fontSize: "1.1rem",
          }}
        >
          {APP_NAME}
        </Typography>
      </Stack>

      <IconButton
        onClick={toggleThemeMode}
        aria-label="Toggle theme mode"
        sx={{
          color: "text.secondary",
          p: "7px",
          borderRadius: "var(--radius-sm)",
          border: "1px solid var(--color-border)",
          backgroundColor: "transparent",
          transition: "all var(--transition-fast)",
          "&:hover": {
            backgroundColor: "var(--color-item-hover)",
            borderColor: "var(--color-border-hover)",
            color: "text.primary",
          },
        }}
      >
        {isDarkMode ? <SunIconSvg /> : <MoonIconSvg />}
      </IconButton>
    </Stack>
  );
}
