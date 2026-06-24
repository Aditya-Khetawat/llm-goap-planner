import { createTheme, responsiveFontSizes, type Theme } from "@mui/material/styles";
import { THEME_MODE_DARK, type ThemeMode } from "@shared/constants/theme";

function createPalette(mode: ThemeMode) {
  const isDarkMode = mode === THEME_MODE_DARK;
  return {
    mode,
    primary: {
      main: "#7C5CFF",
    },
    secondary: {
      main: "#7C5CFF", // single accent color only
    },
    background: {
      default: isDarkMode ? "#050816" : "#F9FAFB",
      paper: isDarkMode ? "#0B1020" : "#FFFFFF",
    },
    text: {
      primary: isDarkMode ? "#FFFFFF" : "#111827",
      secondary: isDarkMode ? "#9CA3AF" : "#6B7280",
    },
    divider: isDarkMode ? "rgba(255, 255, 255, 0.08)" : "rgba(0, 0, 0, 0.06)",
    success: {
      main: "#10B981",
    },
    warning: {
      main: "#F59E0B",
    },
    error: {
      main: "#EF4444",
    },
  };
}

export function createAppTheme(mode: ThemeMode): Theme {
  const isDarkMode = mode === THEME_MODE_DARK;
  const theme = createTheme({
    palette: createPalette(mode),
    typography: {
      fontFamily: "var(--font-family)",
      h1: {
        fontWeight: 700,
        fontSize: "2.25rem", // 36px mobile
        letterSpacing: "-0.04em",
        lineHeight: 1.1,
        "@media (min-width:600px)": {
          fontSize: "2.75rem", // 44px tablet
        },
        "@media (min-width:960px)": {
          fontSize: "3.5rem", // 56px desktop
        },
      },
      h2: {
        fontWeight: 600,
        fontSize: "1.75rem", // 28px section titles
        letterSpacing: "-0.03em",
        lineHeight: 1.25,
      },
      h3: {
        fontWeight: 600,
        fontSize: "1.25rem", // 20px card titles
        letterSpacing: "-0.02em",
        lineHeight: 1.3,
      },
      body1: {
        fontSize: "1rem", // 16px body large
        lineHeight: 1.6,
        letterSpacing: "-0.011em",
      },
      body2: {
        fontSize: "0.875rem", // 14px body
        lineHeight: 1.57,
        letterSpacing: "-0.006em",
      },
      caption: {
        fontSize: "0.75rem", // 12px caption
        lineHeight: 1.5,
        letterSpacing: "0",
      },
      subtitle2: {
        fontSize: "0.75rem", // 12px meta text
        lineHeight: 1.4,
        letterSpacing: "0.01em",
      },
      button: {
        fontWeight: "var(--font-weight-medium)",
        textTransform: "none",
        fontSize: "0.875rem",
      },
    },
    shape: {
      borderRadius: 8,
    },
    components: {
      MuiCssBaseline: {
        styleOverrides: {
          html: {
            scrollBehavior: "smooth",
            WebkitFontSmoothing: "antialiased",
            MozOsxFontSmoothing: "grayscale",
          },
          body: {
            backgroundColor: "var(--color-background)",
            color: "var(--color-text-primary)",
            fontFamily: "var(--font-family)",
            transition: "background-color var(--transition-normal), color var(--transition-normal)",
            scrollbarWidth: "thin",
            scrollbarColor: isDarkMode 
              ? "rgba(255, 255, 255, 0.12) transparent" 
              : "rgba(0, 0, 0, 0.12) transparent",
            "&::-webkit-scrollbar": {
              width: "6px",
              height: "6px",
            },
            "&::-webkit-scrollbar-track": {
              background: "transparent",
            },
            "&::-webkit-scrollbar-thumb": {
              background: isDarkMode 
                ? "rgba(255, 255, 255, 0.12)" 
                : "rgba(0, 0, 0, 0.12)",
              borderRadius: "99px",
              "&:hover": {
                background: isDarkMode 
                  ? "rgba(255, 255, 255, 0.24)" 
                  : "rgba(0, 0, 0, 0.24)",
              },
            },
          },
          "#root": {
            minHeight: "100dvh",
          },
        },
      },
      MuiPaper: {
        styleOverrides: {
          root: {
            backgroundColor: "var(--color-surface)",
            backdropFilter: "blur(20px)",
            WebkitBackdropFilter: "blur(20px)",
            backgroundImage: "none",
            border: "1px solid var(--color-border)",
            boxShadow: isDarkMode 
              ? "var(--shadow-premium-dark)" 
              : "var(--shadow-premium)",
            borderRadius: "var(--radius-md)",
            transition: "background-color var(--transition-normal), border-color var(--transition-normal), box-shadow var(--transition-normal)",
          },
        },
      },
      MuiCard: {
        styleOverrides: {
          root: {
            backgroundColor: "var(--color-surface)",
            backdropFilter: "blur(20px)",
            WebkitBackdropFilter: "blur(20px)",
            border: "1px solid var(--color-border)",
            boxShadow: isDarkMode 
              ? "var(--shadow-premium-dark)" 
              : "var(--shadow-premium)",
            borderRadius: "var(--radius-md)",
            backgroundImage: "none",
            transition: "all var(--transition-normal)",
          },
        },
      },
      MuiCardHeader: {
        styleOverrides: {
          root: {
            padding: "24px 24px 0 24px",
          },
        },
      },
      MuiCardContent: {
        styleOverrides: {
          root: {
            padding: "24px",
            "&:last-child": {
              paddingBottom: "24px",
            },
          },
        },
      },
      MuiSkeleton: {
        styleOverrides: {
          root: {
            backgroundColor: isDarkMode 
              ? "rgba(255, 255, 255, 0.05)" 
              : "rgba(0, 0, 0, 0.04)",
            borderRadius: "var(--radius-sm)",
          },
        },
      },
      MuiButton: {
        styleOverrides: {
          root: {
            borderRadius: "var(--radius-sm)",
            padding: "7px 14px",
            boxShadow: "none",
            transition: "all var(--transition-fast)",
            "&:hover": {
              boxShadow: "none",
              transform: "translateY(-1px)",
            },
            "&:active": {
              transform: "translateY(0.5px)",
            },
            "&.Mui-disabled": {
              opacity: 0.6,
            },
            "&:focus-visible": {
              outline: "none",
              boxShadow: isDarkMode
                ? "0 0 0 2px var(--color-background), 0 0 0 4px var(--color-accent)"
                : "0 0 0 2px var(--color-background), 0 0 0 4px var(--color-accent)",
            },
          },
          containedPrimary: {
            backgroundColor: "var(--color-accent)",
            color: "#FFFFFF",
            "&:hover": {
              backgroundColor: "var(--color-accent-hover)",
            },
          },
          outlined: {
            borderColor: "var(--color-border)",
            color: "var(--color-text-primary)",
            "&:hover": {
              borderColor: "var(--color-border-hover)",
              backgroundColor: "var(--color-item-hover)",
            },
          },
        },
      },
      MuiOutlinedInput: {
        styleOverrides: {
          root: {
            borderRadius: "var(--radius-sm)",
            backgroundColor: "var(--color-surface-elevated)",
            transition: "all var(--transition-fast)",
            "& .MuiOutlinedInput-notchedOutline": {
              borderColor: "var(--color-border)",
              transition: "border-color var(--transition-fast)",
            },
            "&:hover .MuiOutlinedInput-notchedOutline": {
              borderColor: "var(--color-border-hover)",
            },
            "&.Mui-focused": {
              backgroundColor: isDarkMode ? "rgba(10, 15, 30, 0.85)" : "#FFFFFF",
              "& .MuiOutlinedInput-notchedOutline": {
                borderColor: "var(--color-accent)",
                borderWidth: "1px",
              },
            },
          },
        },
      },
      MuiDrawer: {
        styleOverrides: {
          paper: {
            backgroundColor: "transparent",
            borderRight: "1px solid transparent",
            boxShadow: "var(--shadow-flat)",
          },
        },
      },
      MuiAppBar: {
        styleOverrides: {
          root: {
            backgroundColor: "transparent",
            boxShadow: "var(--shadow-flat)",
            borderBottom: "1px solid var(--color-border)",
          },
        },
      },
      MuiListItemButton: {
        styleOverrides: {
          root: {
            borderRadius: "var(--radius-sm)",
            margin: "2px 8px",
            transition: "all var(--transition-fast)",
            "&.Mui-selected": {
              backgroundColor: "var(--color-item-active)",
              color: "var(--color-accent)",
              "& .MuiListItemIcon-root": {
                color: "var(--color-accent)",
              },
              "&:hover": {
                backgroundColor: "var(--color-item-active-hover)",
              },
            },
            "&:hover": {
              backgroundColor: "var(--color-item-hover)",
              color: "var(--color-text-primary)",
              "& .MuiListItemIcon-root": {
                color: "var(--color-text-primary)",
              },
            },
          },
        },
      },
    },
  });

  return responsiveFontSizes(theme);
}

