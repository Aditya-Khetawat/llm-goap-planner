import { Alert, Box, Stack, Typography } from "@mui/material";
import { useTheme } from "@mui/material/styles";
import type { MermaidConfig } from "mermaid";
import { useEffect, useId, useMemo, useRef, useState } from "react";

import { AppSkeleton } from "@shared/ui/components/skeleton";
import { sanitizeText } from "@shared/lib/sanitize";

const BLOCKED_SVG_TAGS = new Set(["script", "foreignobject", "iframe", "object", "embed", "link"]);

export interface MermaidDiagramViewerProps {
  diagram: string;
  title: string;
  description?: string;
  config?: MermaidConfig;
}

const defaultConfig: MermaidConfig = {
  startOnLoad: false,
  securityLevel: "strict",
  theme: "base",
  fontFamily: "Inter, Segoe UI, Arial, sans-serif",
};

function sanitizeSvg(svgElement: Element): SVGElement {
  if (!(svgElement instanceof SVGElement)) {
    throw new Error("Mermaid output was not a valid SVG document.");
  }

  const elements = Array.from(svgElement.querySelectorAll("*")).reverse();

  for (const element of elements) {
    const tagName = element.tagName.toLowerCase();

    if (BLOCKED_SVG_TAGS.has(tagName)) {
      element.remove();
      continue;
    }

    for (const attribute of Array.from(element.attributes)) {
      const attributeName = attribute.name.toLowerCase();
      const attributeValue = attribute.value.trim().toLowerCase();

      if (attributeName.startsWith("on")) {
        element.removeAttribute(attribute.name);
      }

      if (
        (attributeName === "href" || attributeName === "xlink:href" || attributeName === "src") &&
        attributeValue.startsWith("javascript:")
      ) {
        element.removeAttribute(attribute.name);
      }
    }
  }

  return svgElement;
}

export function MermaidDiagramViewer({
  diagram,
  title,
  description,
  config,
}: MermaidDiagramViewerProps) {
  const containerRef = useRef<HTMLDivElement | null>(null);
  const [loading, setLoading] = useState(true);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const id = useId();
  const theme = useTheme();

  const resolvedConfig: MermaidConfig = useMemo(
    () => ({
      ...defaultConfig,
      ...config,
      theme: theme.palette.mode === "dark" ? "dark" : "default",
      themeVariables: {
        primaryColor: theme.palette.primary.main,
        primaryTextColor: theme.palette.text.primary,
        primaryBorderColor: theme.palette.primary.dark,
        lineColor: theme.palette.divider,
        secondaryColor: theme.palette.background.paper,
        tertiaryColor: theme.palette.background.default,
        fontFamily: "Inter, Segoe UI, Arial, sans-serif",
        ...(config?.themeVariables ?? {}),
      },
    }),
    [
      config,
      theme.palette.background.default,
      theme.palette.background.paper,
      theme.palette.divider,
      theme.palette.mode,
      theme.palette.primary.dark,
      theme.palette.primary.main,
      theme.palette.text.primary,
    ],
  );

  useEffect(() => {
    let isActive = true;

    async function renderDiagram() {
      if (!containerRef.current) {
        return;
      }

      containerRef.current.replaceChildren();

      if (!diagram.trim()) {
        setLoading(false);
        setErrorMessage("No Mermaid diagram was returned by the backend.");
        return;
      }

      setLoading(true);
      setErrorMessage(null);

      try {
        const mermaidModule = await import("mermaid");
        mermaidModule.default.initialize(resolvedConfig);
        const { svg } = await mermaidModule.default.render(`mermaid-${sanitizeText(id)}`, diagram);

        if (!isActive || !containerRef.current) {
          return;
        }

        const parsed = new DOMParser().parseFromString(svg, "image/svg+xml");
        const svgElement = parsed.documentElement;
        const importedSvg = document.importNode(sanitizeSvg(svgElement), true);

        containerRef.current.replaceChildren(importedSvg);
        setLoading(false);
      } catch (error) {
        if (!isActive) {
          return;
        }

        containerRef.current.replaceChildren();
        setLoading(false);
        setErrorMessage(
          error instanceof Error ? error.message : "Failed to render Mermaid diagram.",
        );
      }
    }

    void renderDiagram();

    return () => {
      isActive = false;
    };
  }, [diagram, id, resolvedConfig]);

  return (
    <Stack spacing={2}>
      <Box>
        <Typography variant="h6" component="h3" fontWeight={700} gutterBottom>
          {title}
        </Typography>
        {description ? (
          <Typography variant="body2" color="text.secondary">
            {description}
          </Typography>
        ) : null}
      </Box>

      {loading ? <AppSkeleton variant="rounded" height={320} /> : null}

      {errorMessage ? (
        <Alert severity="warning" variant="outlined">
          {errorMessage}
        </Alert>
      ) : null}

      <Box
        ref={containerRef}
        component="div"
        aria-label={title}
        role="img"
        aria-busy={loading}
        sx={{
          overflowX: "auto",
          "& svg": {
            width: "100%",
            height: "auto",
          },
        }}
      />
    </Stack>
  );
}
