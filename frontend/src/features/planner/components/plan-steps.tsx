import { List, ListItem, ListItemText } from "@mui/material";

import type { PlanStep } from "@shared/types/api";
import { sanitizeText } from "@shared/lib/sanitize";

interface PlanStepsProps {
  steps: PlanStep[];
}

export function PlanSteps({ steps }: PlanStepsProps) {
  return (
    <List disablePadding>
      {steps.map((step, index) => {
        const isLast = index === steps.length - 1;
        return (
          <ListItem
            key={`${step.order}-${step.title}`}
            divider={!isLast}
            alignItems="flex-start"
            sx={{ 
              px: 1,
              py: 1.25,
              transition: "all var(--transition-fast)",
              "&:hover": {
                backgroundColor: "var(--color-item-hover)",
              },
              borderRadius: "var(--radius-xs)",
            }}
          >
            <ListItemText
              primary={`${step.order}. ${sanitizeText(step.title)}`}
              primaryTypographyProps={{
                variant: "body2",
                fontWeight: 600,
                color: "text.primary",
              }}
              secondary={sanitizeText(step.details || step.agent)}
              secondaryTypographyProps={{ 
                component: "div",
                variant: "caption",
                color: "text.secondary",
                sx: { mt: 0.5 }
              }}
            />
          </ListItem>
        );
      })}
    </List>
  );
}
