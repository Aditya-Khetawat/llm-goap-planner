import { List, ListItem, ListItemText } from "@mui/material";

import type { PlanStep } from "@shared/types/api";
import { sanitizeText } from "@shared/lib/sanitize";

interface PlanStepsProps {
  steps: PlanStep[];
}

export function PlanSteps({ steps }: PlanStepsProps) {
  return (
    <List disablePadding>
      {steps.map((step) => (
        <ListItem
          key={`${step.order}-${step.title}`}
          divider
          alignItems="flex-start"
          sx={{ px: 0 }}
        >
          <ListItemText
            primary={`${step.order}. ${sanitizeText(step.title)}`}
            secondary={sanitizeText(step.details || step.agent)}
            secondaryTypographyProps={{ component: "div" }}
          />
        </ListItem>
      ))}
    </List>
  );
}
