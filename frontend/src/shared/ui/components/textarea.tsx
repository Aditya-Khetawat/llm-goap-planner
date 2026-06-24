import { Box, TextField, type TextFieldProps, Typography } from "@mui/material";
import { forwardRef, type ReactNode } from "react";

export interface AppTextareaProps extends Omit<TextFieldProps, "variant" | "multiline"> {
  errorText?: string;
  minRows?: number;
  maxRows?: number;
  children?: ReactNode;
}

export const AppTextarea = forwardRef<HTMLDivElement, AppTextareaProps>(
  ({ errorText, minRows = 4, maxRows = 10, sx, error, children, ...props }, ref) => {
    return (
      <Box sx={{ width: "100%" }}>
        <Box
          sx={{
            display: "flex",
            flexDirection: "column",
            width: "100%",
            backgroundColor: "var(--color-surface-elevated)",
            border: "1px solid var(--color-border)",
            borderRadius: "var(--radius-md)",
            p: 2.5, // increased internal padding for a spacious feel
            transition: "border-color var(--transition-fast), box-shadow var(--transition-fast)",
            "&:focus-within": {
              borderColor: "rgba(124, 92, 255, 0.4)",
              boxShadow: "0 0 0 3px rgba(124, 92, 255, 0.15)",
            },
            ...(error && {
              borderColor: "rgba(239, 68, 68, 0.4)",
              "&:focus-within": {
                borderColor: "rgba(239, 68, 68, 0.4)",
                boxShadow: "0 0 0 3px rgba(239, 68, 68, 0.15)",
              },
            }),
          }}
        >
          <TextField
            ref={ref}
            fullWidth
            multiline
            minRows={minRows}
            maxRows={maxRows}
            variant="outlined"
            error={error}
            {...props}
            sx={{
              "& .MuiOutlinedInput-root": {
                backgroundColor: "transparent",
                padding: 0,
                fontSize: "1rem",
                lineHeight: 1.55,
                "& .MuiOutlinedInput-notchedOutline": {
                  border: "none",
                },
                "&:hover .MuiOutlinedInput-notchedOutline": {
                  border: "none",
                },
                "&.Mui-focused .MuiOutlinedInput-notchedOutline": {
                  border: "none",
                },
              },
              "& .MuiInputBase-input::placeholder": {
                color: "var(--color-text-secondary)",
                opacity: 0.5,
                transition: "opacity var(--transition-fast) ease-out",
              },
              "& .MuiInputBase-input:focus::placeholder": {
                opacity: 0.3,
              },
              ...sx,
            }}
          />
          {children}
        </Box>
        {errorText && (
          <Typography
            variant="caption"
            color="error"
            sx={{ display: "block", mt: 1, ml: 1, fontWeight: 500 }}
          >
            {errorText}
          </Typography>
        )}
      </Box>
    );
  }
);

AppTextarea.displayName = "AppTextarea";
