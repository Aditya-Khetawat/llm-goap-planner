import { Stack } from "@mui/material";
import { AppSkeleton } from "./skeleton";

export interface SkeletonLoaderProps {
  variant?: "card" | "list" | "text";
  count?: number;
}

export function SkeletonLoader({ variant = "text", count = 3 }: SkeletonLoaderProps) {
  return (
    <Stack spacing={2} sx={{ width: "100%" }}>
      {Array.from({ length: count }).map((_, idx) => {
        if (variant === "card") {
          return (
            <AppSkeleton
              key={idx}
              variant="rounded"
              height={100}
              sx={{ borderRadius: "var(--radius-md)" }}
            />
          );
        }
        if (variant === "list") {
          return (
            <Stack key={idx} direction="row" spacing={2} alignItems="center">
              <AppSkeleton variant="circular" width={40} height={40} />
              <Stack spacing={1} sx={{ flex: 1 }}>
                <AppSkeleton variant="text" width="60%" height={20} />
                <AppSkeleton variant="text" width="40%" height={16} />
              </Stack>
            </Stack>
          );
        }
        return (
          <AppSkeleton
            key={idx}
            variant="text"
            height={24}
            sx={{ width: idx === count - 1 ? "60%" : "100%" }}
          />
        );
      })}
    </Stack>
  );
}
