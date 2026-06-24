import { Box, IconButton, Stack, Typography } from "@mui/material";

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
      sx={{ width: "100%", px: 3, height: "100%" }}
    >
      {/* Left Section */}
      <Stack direction="row" spacing={1.5} alignItems="center">
        {onMenuClick ? (
          <IconButton
            aria-label="Open navigation drawer"
            onClick={onMenuClick}
            edge="start"
            sx={{ display: { md: "none" }, color: "text.primary", mr: 0.5 }}
          >
            <MenuIconSvg />
          </IconButton>
        ) : null}
        
        {/* Brand Text */}
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
            fontSize: "1rem",
            lineHeight: 1,
            userSelect: "none",
          }}
        >
          {APP_NAME}
        </Typography>

        {/* Subtle Separator */}
        <Box 
          sx={{ 
            width: "1px", 
            height: "12px", 
            backgroundColor: "var(--color-border)",
            mx: 0.5,
            opacity: 0.8,
          }} 
        />

        {/* Workspace Muted Label */}
        <Typography
          variant="body2"
          sx={{
            color: "text.secondary",
            fontWeight: 500,
            fontSize: "0.875rem",
            letterSpacing: "-0.01em",
            userSelect: "none",
          }}
        >
          Planner
        </Typography>
      </Stack>

      {/* Center Section (Intentionally Empty) */}
      <Box sx={{ flexGrow: 1 }} />

      {/* Right Section */}
      <Stack direction="row" spacing={2.5} alignItems="center">
        {/* Status Indicator */}
        <Stack direction="row" spacing={1} alignItems="center" sx={{ display: { xs: "none", sm: "flex" } }}>
          <Box
            sx={{
              width: "6px",
              height: "6px",
              borderRadius: "50%",
              backgroundColor: "#10b981", // vibrant green
              boxShadow: "0 0 8px #10b981",
              animation: "pulse 2.5s infinite ease-in-out",
              "@keyframes pulse": {
                "0%": { opacity: 0.5, transform: "scale(0.9)" },
                "50%": { opacity: 1, transform: "scale(1.1)" },
                "100%": { opacity: 0.5, transform: "scale(0.9)" },
              },
            }}
          />
          <Typography
            variant="caption"
            sx={{
              color: "text.secondary",
              fontWeight: 500,
              fontSize: "0.75rem",
              letterSpacing: "-0.01em",
            }}
          >
            Runtime Ready
          </Typography>
        </Stack>

        {/* Provider Badge */}
        <Box
          sx={{
            display: { xs: "none", sm: "inline-flex" },
            alignItems: "center",
            px: 1.2,
            py: 0.4,
            borderRadius: "6px",
            border: "1px solid var(--color-border)",
            backgroundColor: (theme) =>
              theme.palette.mode === "dark" ? "rgba(255, 255, 255, 0.03)" : "rgba(0, 0, 0, 0.02)",
            backdropFilter: "blur(8px)",
            userSelect: "none",
            transition: "all 180ms cubic-bezier(0.4, 0, 0.2, 1)",
            "&:hover": {
              borderColor: "var(--color-border-hover)",
              backgroundColor: (theme) =>
                theme.palette.mode === "dark" ? "rgba(255, 255, 255, 0.06)" : "rgba(0, 0, 0, 0.04)",
              transform: "translateY(-0.5px)",
            },
          }}
        >
          <Typography
            variant="caption"
            sx={{
              fontWeight: 600,
              fontSize: "0.68rem",
              letterSpacing: "0.05em",
              textTransform: "uppercase",
              color: "text.secondary",
            }}
          >
            GOAP Engine
          </Typography>
        </Box>

        {/* Theme Toggle */}
        <IconButton
          onClick={toggleThemeMode}
          aria-label="Toggle theme mode"
          sx={{
            color: "text.secondary",
            p: "7px",
            borderRadius: "var(--radius-sm)",
            border: "1px solid var(--color-border)",
            backgroundColor: "transparent",
            transition: "all 150ms ease",
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
    </Stack>
  );
}
