import { Chip, type ChipProps } from "@mui/material";
import { sanitizeText } from "@shared/lib/sanitize";

export interface StatusBadgeProps extends Omit<ChipProps, "color"> {
  status: string;
}

export function StatusBadge({ status, sx, ...props }: StatusBadgeProps) {
  const normStatus = status.toLowerCase();
  const isSuccess = normStatus.includes("success") || normStatus === "ok" || normStatus === "completed" || normStatus === "done";
  const isError = normStatus.includes("error") || normStatus === "failed" || normStatus === "danger";
  const isWarning = normStatus.includes("warning") || normStatus === "pending" || normStatus === "running";

  let bgColor = "rgba(255, 255, 255, 0.05)";
  let textColor = "var(--color-text-secondary)";

  if (isSuccess) {
    bgColor = "var(--color-success-bg)";
    textColor = "var(--color-success)";
  } else if (isError) {
    bgColor = "var(--color-danger-bg)";
    textColor = "var(--color-danger)";
  } else if (isWarning) {
    bgColor = "var(--color-warning-bg)";
    textColor = "var(--color-warning)";
  }

  return (
    <Chip
      label={sanitizeText(status)}
      size="small"
      variant="outlined"
      sx={{
        fontWeight: 600,
        textTransform: "uppercase",
        letterSpacing: "0.05em",
        fontSize: "0.72rem",
        height: "22px",
        backgroundColor: bgColor,
        color: textColor,
        border: "none",
        "& .MuiChip-label": {
          px: 1.2,
        },
        ...sx,
      }}
      {...props}
    />
  );
}
