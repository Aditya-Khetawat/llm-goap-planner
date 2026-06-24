import Button, { type ButtonProps } from "@mui/material/Button";
import CircularProgress from "@mui/material/CircularProgress";

export interface AppButtonProps extends ButtonProps {
  loading?: boolean;
  loadingText?: string;
}

export function AppButton({ loading, loadingText, disabled, children, startIcon, ...props }: AppButtonProps) {
  return (
    <Button
      disabled={disabled || loading}
      startIcon={
        loading ? (
          <CircularProgress size={16} color="inherit" />
        ) : (
          startIcon
        )
      }
      {...props}
    >
      {loading && loadingText ? loadingText : children}
    </Button>
  );
}

