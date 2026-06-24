import { Outlet, useLocation, useNavigate } from "react-router-dom";
import { useMemo } from "react";
import { Box } from "@mui/material";

import { ROUTE_PATHS } from "@shared/constants/routes";
import { useBoolean } from "@shared/hooks/use-boolean";
import { AppShell } from "@shared/ui/layout/app-shell";
import type { SidebarItem } from "@shared/ui/navigation/sidebar";
import { SoftAurora } from "@shared/ui/visualization/soft-aurora";
import { useThemeMode } from "@shared/hooks/use-theme-mode";

export function GlobalLayout() {
  const { value: sidebarOpen, toggle: toggleSidebar } = useBoolean();
  const location = useLocation();
  const navigate = useNavigate();
  const { isDarkMode } = useThemeMode();

  const navigationItems: SidebarItem[] = useMemo(
    () => [
      {
        label: "Goal Input",
        iconName: "goal",
        selected: location.pathname === ROUTE_PATHS.planner,
        onClick: () => navigate(ROUTE_PATHS.planner),
      },
      {
        label: "Plan Result",
        iconName: "result",
        selected: location.pathname === ROUTE_PATHS.plannerResult,
        onClick: () => navigate(ROUTE_PATHS.plannerResult),
      },
    ],
    [location.pathname, navigate],
  );

  return (
    <Box sx={{ position: "relative", minHeight: "100dvh" }}>
      {/* Background Aurora */}
      <Box
        sx={{
          position: "fixed",
          top: 0,
          left: 0,
          right: 0,
          bottom: 0,
          zIndex: 0,
          pointerEvents: "none",
          opacity: isDarkMode ? 0.85 : 0.45, // tuned for vibrant glow
          transition: "opacity var(--transition-normal)",
        }}
      >
        <SoftAurora
          speed={0.6}
          scale={1.5}
          brightness={1.15}
          color1="#ffffff"
          color2="#ff3fd8"
          noiseFrequency={2.2}
          noiseAmplitude={1.15}
          bandHeight={0.42}
          bandSpread={1.15}
          octaveDecay={0.18}
          layerOffset={0.08}
          colorSpeed={0.95}
          enableMouseInteraction={true}
          mouseInfluence={0.15}
        />
      </Box>

      {/* Subtle overlay to ensure premium readability and reduce dominance */}
      <Box
        sx={{
          position: "fixed",
          top: 0,
          left: 0,
          right: 0,
          bottom: 0,
          zIndex: 0,
          pointerEvents: "none",
          background: (theme) =>
            theme.palette.mode === "dark"
              ? "radial-gradient(circle at 50% 50%, rgba(5, 8, 22, 0.05) 0%, rgba(5, 8, 22, 0.75) 100%)"
              : "radial-gradient(circle at 50% 50%, rgba(249, 250, 251, 0.05) 0%, rgba(249, 250, 251, 0.75) 100%)",
        }}
      />

      {/* Main App Layout */}
      <Box sx={{ position: "relative", zIndex: 1 }}>
        <AppShell
          sidebarOpen={sidebarOpen}
          onSidebarClose={toggleSidebar}
          navigationItems={navigationItems}
          sidebarTitle="Planner"
        >
          <Outlet />
        </AppShell>
      </Box>
    </Box>
  );
}

