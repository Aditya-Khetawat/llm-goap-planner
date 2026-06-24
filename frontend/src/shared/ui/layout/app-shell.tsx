import { AppBar, Box, Drawer, Toolbar } from "@mui/material";
import type { PropsWithChildren } from "react";

import { AppHeader } from "@shared/ui/navigation/header";
import { AppSidebar, type SidebarItem } from "@shared/ui/navigation/sidebar";

export interface AppShellProps extends PropsWithChildren {
  sidebarOpen?: boolean;
  onSidebarClose?: () => void;
  drawerWidth?: number;
  navigationItems?: SidebarItem[];
  sidebarTitle?: string;
}

export function AppShell({
  children,
  sidebarOpen = false,
  onSidebarClose,
  drawerWidth = 200,
  navigationItems,
  sidebarTitle,
}: AppShellProps) {
  return (
    <Box sx={{ display: "flex", minHeight: "100dvh" }}>
      <AppBar 
        position="fixed" 
        elevation={0}
        sx={{
          width: { md: `calc(100% - ${drawerWidth}px)` },
          ml: { md: `${drawerWidth}px` },
          height: { xs: "56px", md: "64px" },
          backgroundColor: (theme) =>
            theme.palette.mode === "dark" ? "rgba(5, 8, 22, 0.45)" : "rgba(249, 250, 251, 0.45)",
          backdropFilter: "blur(20px)",
          WebkitBackdropFilter: "blur(20px)",
          borderBottom: "1px solid var(--color-border)",
          boxShadow: (theme) =>
            theme.palette.mode === "dark"
              ? "inset 0 1px 0 0 rgba(255, 255, 255, 0.05), 0 1px 2px 0 rgba(0, 0, 0, 0.2)"
              : "inset 0 1px 0 0 rgba(255, 255, 255, 0.6), 0 1px 2px 0 rgba(0, 0, 0, 0.03)",
          display: "flex",
          justifyContent: "center",
          transition: "all var(--transition-normal)",
        }}
      >
        <Toolbar disableGutters sx={{ minHeight: "100%", height: "100%" }}>
          <AppHeader onMenuClick={onSidebarClose} />
        </Toolbar>
      </AppBar>

      <Drawer
        variant="temporary"
        open={sidebarOpen}
        onClose={onSidebarClose}
        ModalProps={{ keepMounted: true }}
        sx={{
          display: { xs: "block", md: "none" },
          width: drawerWidth,
          flexShrink: 0,
          "& .MuiDrawer-paper": { 
            width: drawerWidth, 
            boxSizing: "border-box",
            backgroundColor: "var(--color-surface-elevated)",
            backdropFilter: "blur(24px)",
            WebkitBackdropFilter: "blur(24px)",
            borderRight: "1px solid var(--color-border)",
          },
        }}
      >
        <AppSidebar items={navigationItems} title={sidebarTitle} />
      </Drawer>

      <Drawer
        variant="permanent"
        sx={{
          display: { xs: "none", md: "block" },
          width: drawerWidth,
          flexShrink: 0,
          "& .MuiDrawer-paper": { width: drawerWidth, boxSizing: "border-box" },
        }}
        open
      >
        <AppSidebar items={navigationItems} title={sidebarTitle} />
      </Drawer>

      <Box
        component="main"
        sx={{
          flexGrow: 1,
          width: { md: `calc(100% - ${drawerWidth}px)` },
          display: "flex",
          justifyContent: "center",
          minHeight: "100vh",
        }}
      >
        <Box
          sx={{
            width: "100%",
            maxWidth: "1200px",
            px: { xs: 2.5, sm: 5, md: 8 },
            pt: 0, // content aligns perfectly below the sticky header
            pb: { xs: 4, md: 6 },
            display: "flex",
            flexDirection: "column",
          }}
        >
          <Toolbar sx={{ minHeight: { xs: "56px", md: "64px" } }} />
          {children}
        </Box>
      </Box>
    </Box>
  );
}
