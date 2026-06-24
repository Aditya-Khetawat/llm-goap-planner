import { Alert, Box, Stack, Typography } from "@mui/material";
import { useTheme } from "@mui/material/styles";
import type { MermaidConfig } from "mermaid";
import { useEffect, useId, useMemo, useRef, useState } from "react";

import { AppSkeleton } from "@shared/ui/components/skeleton";

const BLOCKED_SVG_TAGS = new Set(["script", "foreignobject", "iframe", "object", "embed", "link"]);

export interface MermaidDiagramViewerProps {
  diagram: string;
  title?: string;
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
      theme: "base",
      themeVariables: {
        background: "transparent",
        mainBkg: theme.palette.mode === "dark" ? "#1a1b23" : "#f4f5f8",
        nodeBorder: theme.palette.mode === "dark" ? "#3f4257" : "#d1d5db",
        textColor: theme.palette.text.primary,
        lineColor: theme.palette.mode === "dark" ? "#8e91a8" : "#4b5563",
        edgeLabelBackground: theme.palette.mode === "dark" ? "#111218" : "#ffffff",
        fontFamily: "Inter, Segoe UI, Arial, sans-serif",
        ...(config?.themeVariables ?? {}),
      },
    }),
    [
      config,
      theme.palette.mode,
      theme.palette.text.primary,
      theme.palette.text.secondary,
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
        const cleanId = id.replace(/:/g, "").replace(/[^a-zA-Z0-9-]/g, "");
        const uniqueRenderId = `mermaid-${cleanId}-${Math.random().toString(36).substring(2, 9)}`;
        const { svg } = await mermaidModule.default.render(uniqueRenderId, diagram);

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
      {title || description ? (
        <Box>
          {title ? (
            <Typography variant="h6" component="h3" fontWeight={700} gutterBottom>
              {title}
            </Typography>
          ) : null}
          {description ? (
            <Typography variant="body2" color="text.secondary">
              {description}
            </Typography>
          ) : null}
        </Box>
      ) : null}

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
