import {
  Box,
  List,
  ListItemButton,
  ListItemIcon,
  ListItemText,
  Stack,
  Typography,
} from "@mui/material";
import type { ReactNode } from "react";

import { APP_NAME } from "@shared/constants/app";

export interface SidebarItem {
  label: string;
  icon?: ReactNode;
  iconName?: string;
  selected?: boolean;
  disabled?: boolean;
  onClick?: () => void;
}

const LogoIcon = () => (
  <svg width="24" height="24" viewBox="0 0 32 32" fill="none" xmlns="http://www.w3.org/2000/svg">
    <rect x="2" y="2" width="28" height="28" rx="8" fill="url(#sidebarLogoGradient)" />
    <path d="M11 16L16 11L21 16L16 21L11 16Z" fill="#FFFFFF" />
    <circle cx="16" cy="16" r="2" fill="#7C5CFF" />
    <defs>
      <linearGradient id="sidebarLogoGradient" x1="0" y1="0" x2="32" y2="32" gradientUnits="userSpaceOnUse">
        <stop stopColor="#7C5CFF" />
        <stop offset="1" stopColor="#967CFF" />
      </linearGradient>
    </defs>
  </svg>
);

export interface AppSidebarProps {
  items?: SidebarItem[];
  title?: string;
}

// Native inline SVGs to bypass React 19 / MUI SvgIcon context bugs
const HomeIconSvg = () => (
  <svg
    width="18"
    height="18"
    viewBox="0 0 24 24"
    fill="none"
    stroke="currentColor"
    strokeWidth="2"
    strokeLinecap="round"
    strokeLinejoin="round"
    style={{ minWidth: "18px" }}
  >
    <path d="m3 9 9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z" />
    <polyline points="9 22 9 12 15 12 15 22" />
  </svg>
);

const GoalIconSvg = () => (
  <svg
    width="18"
    height="18"
    viewBox="0 0 24 24"
    fill="none"
    stroke="currentColor"
    strokeWidth="2"
    strokeLinecap="round"
    strokeLinejoin="round"
    style={{ minWidth: "18px" }}
  >
    <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" />
    <polyline points="14 2 14 8 20 8" />
    <line x1="16" y1="13" x2="8" y2="13" />
    <line x1="16" y1="17" x2="8" y2="17" />
    <polyline points="10 9 9 9 8 9" />
  </svg>
);

const ResultIconSvg = () => (
  <svg
    width="18"
    height="18"
    viewBox="0 0 24 24"
    fill="none"
    stroke="currentColor"
    strokeWidth="2"
    strokeLinecap="round"
    strokeLinejoin="round"
    style={{ minWidth: "18px" }}
  >
    <path d="M6 3v12" />
    <path d="M18 9a3 3 0 1 0 0-6 3 3 0 0 0 0 6z" />
    <path d="M6 21a3 3 0 1 0 0-6 3 3 0 0 0 0 6z" />
    <path d="M18 21a3 3 0 1 0 0-6 3 3 0 0 0 0 6z" />
    <path d="M6 12h9a3 3 0 0 0 3-3" />
  </svg>
);

const defaultItems: SidebarItem[] = [{ label: "Dashboard", icon: <HomeIconSvg /> }];

export function AppSidebar({ items = defaultItems, title = "Navigation" }: AppSidebarProps) {
  return (
    <Stack spacing={2} sx={{ p: 2, pt: 3 }}>
      {/* Brand Logo & Name */}
      <Stack direction="row" spacing={1.2} alignItems="center" sx={{ px: 1, mb: 1.5 }}>
        <LogoIcon />
        <Typography
          variant="h6"
          fontWeight={800}
          letterSpacing="-0.03em"
          sx={{
            background: (theme) =>
              theme.palette.mode === "dark"
                ? "linear-gradient(135deg, #FFFFFF 40%, #967CFF 100%)"
                : "linear-gradient(135deg, #111827 40%, #7C5CFF 100%)",
            WebkitBackgroundClip: "text",
            WebkitTextFillColor: "transparent",
            fontSize: "1.2rem",
          }}
        >
          {APP_NAME}
        </Typography>
      </Stack>

      <Typography
        variant="caption"
        color="text.secondary"
        fontWeight={600}
        letterSpacing="0.08em"
        sx={{ textTransform: "uppercase", px: 1, opacity: 0.5 }}
      >
        {title}
      </Typography>
      <Box component="nav" aria-label={title}>
        <List disablePadding>
          {items.map((item) => (
            <ListItemButton
              key={item.label}
              selected={item.selected}
              disabled={item.disabled}
              onClick={item.onClick}
              sx={{
                mb: "4px",
                py: 1,
                px: 1.5,
                borderRadius: "6px",
                transition: "all var(--transition-fast)",
                "&.Mui-selected": {
                  backgroundColor: "rgba(124, 92, 255, 0.08)",
                  color: "var(--color-accent)",
                  "& .MuiListItemIcon-root": {
                    color: "var(--color-accent)",
                  },
                  "&:hover": {
                    backgroundColor: "rgba(124, 92, 255, 0.12)",
                  },
                },
                "&:hover": {
                  backgroundColor: (theme) =>
                    theme.palette.mode === "dark"
                      ? "rgba(255, 255, 255, 0.02)"
                      : "rgba(0, 0, 0, 0.02)",
                },
              }}
            >
              {item.iconName === "goal" ? (
                <ListItemIcon sx={{ minWidth: "28px" }}>
                  <GoalIconSvg />
                </ListItemIcon>
              ) : item.iconName === "result" ? (
                <ListItemIcon sx={{ minWidth: "28px" }}>
                  <ResultIconSvg />
                </ListItemIcon>
              ) : item.icon ? (
                <ListItemIcon sx={{ minWidth: "28px" }}>{item.icon}</ListItemIcon>
              ) : null}
              <ListItemText
                primary={item.label}
                primaryTypographyProps={{
                  fontSize: "0.9rem",
                  fontWeight: item.selected ? 600 : 500,
                  letterSpacing: "-0.01em",
                }}
              />
            </ListItemButton>
          ))}
        </List>
      </Box>
    </Stack>
  );
}
