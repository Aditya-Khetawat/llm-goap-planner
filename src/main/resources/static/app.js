const apiEndpoint = "/api/plans";
let latestDiagram = "";
let latestGantt = "";
let latestAssignments = [];
let loadingInterval = null;
let lastGoal = "";
const loadingSteps = [
  { step: "generating", text: "Generating plan..." },
  { step: "analyzing", text: "Analyzing goal..." },
  { step: "assigning", text: "Assigning agents..." },
];
let currentLoadingStep = 0;

const ErrorTypes = {
  VALIDATION: "validation",
  NETWORK: "network",
  SERVER: "server",
  PARSE: "parse",
  UNKNOWN: "unknown",
};

document.addEventListener("DOMContentLoaded", () => {
  const goalForm = document.getElementById("goalForm");
  const goalInput = document.getElementById("goalInput");
  const copyDiagramBtn = document.getElementById("copyDiagramBtn");
  const copyGanttBtn = document.getElementById("copyGanttBtn");
  const copyAssignmentsBtn = document.getElementById("copyAssignmentsBtn");
  const copyTraceBtn = document.getElementById("copyTraceBtn");

  if (window.mermaid) {
    window.mermaid.initialize({
      startOnLoad: false,
      securityLevel: "loose",
      theme: "base",
      flowchart: {
        nodeSpacing: 50,
        rankSpacing: 40,
        padding: "10",
        htmlLabels: true,
      },
      gantt: {
        fontSize: 12,
        gridLineStartPadding: 350,
      },
      themeVariables: {
        primaryColor: "#e8eefc",
        primaryBorderColor: "#6b7fd7",
        primaryTextColor: "#18243a",
        primaryBorderWidth: "2px",
        lineColor: "#64748b",
        fontSize: "13px",
        fontFamily: "system-ui, sans-serif",
      },
    });
  }

  // Handle tool options toggling
  const toolOptions = document.querySelectorAll(".tool-option");
  toolOptions.forEach(option => {
    const checkbox = option.querySelector("input");
    checkbox.addEventListener("change", () => {
      if (checkbox.checked) {
        option.classList.add("selected");
      } else {
        option.classList.remove("selected");
      }
    });
  });

  goalForm.addEventListener("submit", (event) => {
    event.preventDefault();
    generatePlan();
  });

  goalInput.addEventListener("keydown", (event) => {
    if ((event.ctrlKey || event.metaKey) && event.key === "Enter") {
      event.preventDefault();
      generatePlan();
    }
  });

  copyDiagramBtn.addEventListener("click", async () => {
    if (!latestDiagram) {
      setStatus("No diagram is available to copy yet.");
      return;
    }

    try {
      await navigator.clipboard.writeText(latestDiagram);
      setStatus("Mermaid diagram copied to clipboard.");
    } catch (error) {
      setStatus("Copy failed. Your browser may block clipboard access.");
    }
  });

  copyGanttBtn.addEventListener("click", async () => {
    if (!latestGantt) {
      setStatus("No Gantt chart is available to copy yet.");
      return;
    }

    try {
      await navigator.clipboard.writeText(latestGantt);
      setStatus("Mermaid Gantt chart copied to clipboard.");
    } catch (error) {
      setStatus("Copy failed. Your browser may block clipboard access.");
    }
  });

  copyAssignmentsBtn.addEventListener("click", async () => {
    if (!latestAssignments.length) {
      setStatus("No agent table is available to copy yet.");
      return;
    }

    const lines = ["Step\tAgent\tCapability\tStatus\tHandoff"];
    latestAssignments.forEach((assignment) => {
      lines.push(
        `${assignment.stepTitle}\t${assignment.agent}\t${assignment.capability}\t${assignment.status}\t${assignment.handoffTo}`,
      );
    });

    try {
      await navigator.clipboard.writeText(lines.join("\n"));
      setStatus("Agent assignment table copied to clipboard.");
    } catch (error) {
      setStatus("Copy failed. Your browser may block clipboard access.");
    }
  });

  if (copyTraceBtn) {
    copyTraceBtn.addEventListener("click", async () => {
        const traceLog = document.getElementById("traceLog");
        if (!traceLog || traceLog.innerText.includes("No trace data")) {
            setStatus("No trace data available to copy.");
            return;
        }
        try {
            await navigator.clipboard.writeText(traceLog.innerText);
            setStatus("Trace log copied to clipboard.");
        } catch (error) {
            setStatus("Copy failed.");
        }
    });
  }

  // Bind error action buttons
  const retryBtn = document.getElementById("retryBtn");
  const clearBtn = document.getElementById("clearBtn");
  if (retryBtn) retryBtn.addEventListener("click", generatePlan);
  if (clearBtn)
    clearBtn.addEventListener("click", () => {
      document.getElementById("goalInput").value = "";
      clearStates();
      document.getElementById("emptyState").classList.remove("hidden");
    });

  // Setup tab buttons
  setupTabButtons();
});

async function generatePlan() {
  const goalInput = document.getElementById("goalInput");
  const goal = goalInput.value.trim();

  // Gather selected tools
  const selectedTools = Array.from(document.querySelectorAll('input[name="tool"]:checked'))
    .map(cb => cb.value);

  if (!goal) {
    showError(
      "Please enter a goal before generating a plan.",
      ErrorTypes.VALIDATION,
      "VALIDATION_ERROR",
    );
    return;
  }

  lastGoal = goal;
  clearStates();
  setLoading(true);

  try {
    const response = await fetch(apiEndpoint, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify({ goal, tools: selectedTools }),
    });

    const responseText = await response.text();
    let payload;
    try {
      payload = responseText ? JSON.parse(responseText) : null;
    } catch (parseError) {
      throw {
        type: ErrorTypes.PARSE,
        message: "Invalid server response format.",
      };
    }

    // Capture server-provided source header (LLM or BLUEPRINT)
    const sourceHeader = response.headers.get("X-Plan-Source");
    if (payload) payload.source = sourceHeader || "UNKNOWN";

    if (!response.ok) {
      const errorType =
        response.status >= 500 ? ErrorTypes.SERVER : ErrorTypes.NETWORK;
      throw {
        type: errorType,
        message:
          payload?.message ||
          payload?.error ||
          `Server error (${response.status})`,
        code: `HTTP_${response.status}`,
      };
    }

    renderPlan(payload);
    const src =
      payload && payload.source
        ? (payload.source || "UNKNOWN").toUpperCase()
        : "UNKNOWN";
    const srcLabel =
      src === "PLANNER"
        ? "Dynamic Planner"
        : src === "LLM"
          ? "LLM (Llama)"
          : src === "BLUEPRINT"
            ? "Blueprint (fallback)"
            : src;
    setStatus(`Plan generated successfully. (Generated by: ${srcLabel})`);
  } catch (error) {
    let errorType = ErrorTypes.UNKNOWN;
    let errorCode = "UNKNOWN_ERROR";
    let message = error.message || "An unexpected error occurred.";

    if (error.type) {
      errorType = error.type;
      errorCode = error.code || "ERROR";
    } else if (error instanceof TypeError) {
      errorType = ErrorTypes.NETWORK;
      errorCode = "NETWORK_ERROR";
      message = "Network connection failed. Check your internet connection.";
    }

    showError(message, errorType, errorCode);
  } finally {
    setLoading(false);
  }
}

function renderPlan(plan) {
  const emptyState = document.getElementById("emptyState");
  const planContent = document.getElementById("planContent");
  const visualizationDashboard = document.getElementById(
    "visualizationDashboard",
  );
  const planGoal = document.getElementById("planGoal");
  const planStatus = document.getElementById("planStatus");
  const planGeneratedAt = document.getElementById("planGeneratedAt");
  const planSummary = document.getElementById("planSummary");
  const planSteps = document.getElementById("planSteps");
  const assignmentRows = document.getElementById("assignmentRows");
  const diagramContainer = document.getElementById("diagramContainer");
  const ganttContainer = document.getElementById("ganttContainer");
  const dashboardSummary = document.getElementById("dashboardSummary");

  emptyState.classList.add("hidden");
  planContent.classList.remove("hidden");
  visualizationDashboard.classList.remove("hidden");

  planGoal.textContent = plan.goal || "Untitled goal";
  planStatus.textContent = plan.status || "Ready";
  planGeneratedAt.textContent = formatTimestamp(plan.generatedAt);
  planSummary.textContent =
    plan.summary || "No summary was returned by the backend.";
  dashboardSummary.textContent = plan.summary || "No summary available.";

  // Show plan source if available (friendly label)
  const planSourceEl = document.getElementById("planSource");
  const friendly = (plan.source || "UNKNOWN").toUpperCase();
  const friendlyLabel =
    friendly === "PLANNER"
      ? "Dynamic Planner"
      : friendly === "LLM"
        ? "LLM (Llama)"
        : friendly === "BLUEPRINT"
          ? "Blueprint (fallback)"
          : friendly;
  if (planSourceEl) planSourceEl.textContent = friendlyLabel;

  planSteps.innerHTML = "";
  (plan.steps || []).forEach((step) => {
    const item = document.createElement("li");
    item.className = "step-item";
    
    let outputHtml = "";
    if (step.output) {
        outputHtml = `
            <div class="step-output">
                <strong>Simulated Result (${escapeHtml(step.agent || "Agent")})</strong>
                ${escapeHtml(step.output)}
            </div>
        `;
    }

    item.innerHTML = `
      <div class="step-index">${escapeHtml(String(step.order))}</div>
      <div class="step-copy">
        <h4>${escapeHtml(step.title || "Step")}</h4>
        <p>${escapeHtml(step.details || "")}</p>
        ${outputHtml}
      </div>
    `;
    planSteps.appendChild(item);
  });

  latestAssignments = plan.assignments || [];
  // If assignments are empty but steps have agents, synthesize assignments
  if (!latestAssignments.length && plan.steps) {
      latestAssignments = plan.steps.map(s => ({
          stepTitle: s.title,
          agent: s.agent || "SearchAgent",
          capability: "Action Execution",
          status: s.output ? "Complete" : "Ready",
          handoffTo: "Next Task",
          rationale: "Assigned by dynamic planner"
      }));
  }
  renderAssignments(assignmentRows, latestAssignments);

  // Update metrics
  updateMetrics(plan);

  latestDiagram = plan.mermaidDiagram || "";
  renderDiagram(diagramContainer, latestDiagram);

  latestGantt = plan.ganttDiagram || "";
  renderDiagram(ganttContainer, latestGantt);

  // Render trace if available
  renderTrace(plan.trace);
}

function renderTrace(trace) {
    const traceLog = document.getElementById("traceLog");
    if (!traceLog) return;

    if (!trace || !trace.length) {
        traceLog.innerHTML = '<div class="empty-cell">No trace data available for this plan type.</div>';
        return;
    }

    traceLog.innerHTML = "";
    trace.forEach(entry => {
        const item = document.createElement("div");
        item.className = "trace-entry";
        
        const stateBefore = entry.state_before ? JSON.stringify(entry.state_before, null, 2) : "[]";
        const stateAfter = entry.state_after ? JSON.stringify(entry.state_after, null, 2) : "[]";

        item.innerHTML = `
            <span class="trace-action">> ${escapeHtml(entry.action || "Execute Action")}</span>
            <div class="trace-states">
                <div>
                    <div class="trace-state-label">Before</div>
                    <pre class="trace-state-value">${escapeHtml(stateBefore)}</pre>
                </div>
                <div>
                    <div class="trace-state-label">After</div>
                    <pre class="trace-state-value">${escapeHtml(stateAfter)}</pre>
                </div>
            </div>
        `;
        traceLog.appendChild(item);
    });
}

function renderAssignments(container, assignments) {
  container.innerHTML = "";

  if (!assignments.length) {
    const row = document.createElement("tr");
    row.innerHTML = `<td colspan="5" class="empty-cell">No assignments returned.</td>`;
    container.appendChild(row);
    return;
  }

  assignments.forEach((assignment) => {
    const row = document.createElement("tr");
    row.innerHTML = `
      <td>
        <span class="assignment-step">${escapeHtml(assignment.stepTitle || "Step")}</span>
        <p class="assignment-rationale">${escapeHtml(assignment.rationale || "")}</p>
      </td>
      <td><span class="assignment-agent">${escapeHtml(assignment.agent || "Agent")}</span></td>
      <td>${escapeHtml(assignment.capability || "-")}</td>
      <td><span class="assignment-status status-${toSlug(assignment.status || "unknown")}">${escapeHtml(assignment.status || "Unknown")}</span></td>
      <td>${escapeHtml(assignment.handoffTo || "-")}</td>
    `;
    container.appendChild(row);
  });
}

async function renderDiagram(container, diagramCode) {
  if (!diagramCode) {
    container.innerHTML =
      "<p class='diagram-fallback'>No diagram code was generated. Please try again.</p>";
    return;
  }

  if (!window.mermaid) {
    container.innerHTML = `<pre class="diagram-fallback">${escapeHtml(diagramCode)}</pre>`;
    return;
  }

  container.innerHTML =
    '<div class="diagram-loading"><div class="spinner"></div><p>Rendering diagram...</p></div>';

  try {
    const diagramId = `diagram-${Date.now()}-${Math.random().toString(36).substr(2, 9)}`;
    const { svg } = await window.mermaid.render(diagramId, diagramCode);
    container.innerHTML = `<div class="diagram-wrapper">${svg}</div>`;
  } catch (error) {
    console.error("Mermaid rendering error:", error);
    container.innerHTML = `<div class="diagram-error">
      <p><strong>Diagram rendering failed:</strong></p>
      <pre class="diagram-fallback">${escapeHtml(diagramCode)}</pre>
    </div>`;
  }
}

function clearStates() {
  const emptyState = document.getElementById("emptyState");
  const errorState = document.getElementById("errorState");
  const loadingState = document.getElementById("loadingState");

  if (loadingInterval) clearInterval(loadingInterval);

  Object.values(ErrorTypes).forEach((type) => {
    errorState.classList.remove(`error-${type}`);
  });

  errorState.classList.add("hidden");
  loadingState.classList.add("hidden");
  emptyState.classList.add("hidden");
}

function setLoading(isLoading) {
  const loadingState = document.getElementById("loadingState");
  const generateBtn = document.getElementById("generateBtn");

  if (isLoading) {
    currentLoadingStep = 0;
    loadingState.classList.remove("hidden");
    updateLoadingStep();

    if (loadingInterval) clearInterval(loadingInterval);
    loadingInterval = setInterval(updateLoadingStep, 800);
  } else {
    if (loadingInterval) clearInterval(loadingInterval);
    loadingState.classList.add("hidden");
  }

  generateBtn.disabled = isLoading;
  generateBtn.textContent = isLoading ? "Generating..." : "Generate plan";
}

function updateLoadingStep() {
  const config = loadingSteps[currentLoadingStep % loadingSteps.length];
  const stepText = document.getElementById("loadingStepText");

  if (stepText) {
    stepText.textContent = config.text;
  }

  document.querySelectorAll(".loading-step").forEach((el, idx) => {
    el.classList.remove("active", "complete");
    if (idx < currentLoadingStep) {
      el.classList.add("complete");
    } else if (idx === currentLoadingStep) {
      el.classList.add("active");
    }
  });

  currentLoadingStep++;
}

function showError(
  message,
  errorType = ErrorTypes.UNKNOWN,
  errorCode = "ERROR",
) {
  const errorState = document.getElementById("errorState");
  const errorMessage = document.getElementById("errorMessage");
  const errorCodeEl = document.getElementById("errorCode");
  const errorTitle = document.getElementById("errorTitle");
  const emptyState = document.getElementById("emptyState");
  const planContent = document.getElementById("planContent");
  const retryBtn = document.getElementById("retryBtn");

  const titleMap = {
    [ErrorTypes.VALIDATION]: "Invalid input",
    [ErrorTypes.NETWORK]: "Connection error",
    [ErrorTypes.SERVER]: "Server error",
    [ErrorTypes.PARSE]: "Response error",
    [ErrorTypes.UNKNOWN]: "Generation failed",
  };
  errorTitle.textContent = titleMap[errorType] || "Generation failed";
  errorState.classList.add(`error-${errorType}`);

  emptyState.classList.add("hidden");
  planContent.classList.add("hidden");
  errorMessage.textContent = message;
  errorCodeEl.textContent = `Error code: ${errorCode}`;
  errorState.classList.remove("hidden");

  retryBtn.onclick = () => generatePlan();
  setStatus(`Error: ${message}`);
}

function setStatus(message) {
  const statusBanner = document.getElementById("statusBanner");
  statusBanner.textContent = message;
}

function escapeHtml(text) {
  const map = {
    "&": "&amp;",
    "<": "&lt;",
    ">": "&gt;",
    '"': "&quot;",
    "'": "&#039;",
  };

  return String(text).replace(/[&<>"']/g, (m) => map[m]);
}

function formatTimestamp(value) {
  if (!value) return "Just now";
  const parsed = new Date(value);
  if (Number.isNaN(parsed.getTime())) return "Just now";
  return parsed.toLocaleString();
}

function toSlug(value) {
  return String(value)
    .toLowerCase()
    .replace(/[^a-z0-9]+/g, "-")
    .replace(/^-+|-+$/g, "");
}

function setupTabButtons() {
  const tabButtons = document.querySelectorAll(".tab-btn");
  const tabPanes = document.querySelectorAll(".tab-pane");

  tabButtons.forEach((btn) => {
    btn.addEventListener("click", () => {
      const tabName = btn.getAttribute("data-tab");

      tabButtons.forEach((b) => b.classList.remove("active"));
      tabPanes.forEach((p) => p.classList.remove("active"));

      btn.classList.add("active");
      const pane = document.getElementById(`tab-${tabName}`);
      if (pane) {
        pane.classList.add("active");

        if (tabName === "flowchart" || tabName === "timeline") {
          setTimeout(() => {
            if (window.mermaid && latestDiagram && tabName === "flowchart") {
              const container = document.getElementById("diagramContainer");
              renderDiagram(container, latestDiagram);
            } else if (
              window.mermaid &&
              latestGantt &&
              tabName === "timeline"
            ) {
              const container = document.getElementById("ganttContainer");
              renderDiagram(container, latestGantt);
            }
          }, 100);
        }
      }
    });
  });
}

function updateMetrics(plan) {
  const metricSteps = document.getElementById("metricSteps");
  const metricAgents = document.getElementById("metricAgents");
  const metricProgress = document.getElementById("metricProgress");

  const steps = plan.steps || [];
  const assignments = plan.assignments || [];
  
  // If assignments were synthesized or provided
  const finalAssignments = assignments.length ? assignments : (plan.steps || []).map(s => ({ agent: s.agent }));
  
  const inProgressCount = finalAssignments.filter(
    (a) => a.status && a.status.toLowerCase() === "in progress",
  ).length;

  if (metricSteps) metricSteps.textContent = steps.length;
  if (metricAgents) {
    const uniqueAgents = new Set(finalAssignments.map((a) => a.agent).filter(Boolean));
    metricAgents.textContent = uniqueAgents.size || 0;
  }
  if (metricProgress) metricProgress.textContent = inProgressCount;
}
