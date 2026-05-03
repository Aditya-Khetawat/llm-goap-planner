const apiEndpoint = "/api/plans";
let latestDiagram = "";
let latestGantt = "";
let latestAssignments = [];

document.addEventListener("DOMContentLoaded", () => {
  const goalForm = document.getElementById("goalForm");
  const goalInput = document.getElementById("goalInput");
  const copyDiagramBtn = document.getElementById("copyDiagramBtn");
  const copyGanttBtn = document.getElementById("copyGanttBtn");
  const copyAssignmentsBtn = document.getElementById("copyAssignmentsBtn");

  if (window.mermaid) {
    window.mermaid.initialize({
      startOnLoad: false,
      securityLevel: "loose",
      theme: "base",
      themeVariables: {
        primaryColor: "#e8eefc",
        primaryBorderColor: "#6b7fd7",
        primaryTextColor: "#18243a",
        lineColor: "#64748b",
      },
    });
  }

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
});

async function generatePlan() {
  const goalInput = document.getElementById("goalInput");
  const goal = goalInput.value.trim();

  if (!goal) {
    showError("Please enter a goal before generating a plan.");
    return;
  }

  clearStates();
  setLoading(true);

  try {
    const response = await fetch(apiEndpoint, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify({ goal }),
    });

    const responseText = await response.text();
    const payload = responseText ? JSON.parse(responseText) : null;

    if (!response.ok) {
      throw new Error(
        payload?.message ||
          payload?.error ||
          "The backend rejected the request.",
      );
    }

    renderPlan(payload);
    setStatus("Plan generated successfully.");
  } catch (error) {
    showError(
      error.message ||
        "An unexpected error occurred while generating the plan.",
    );
  } finally {
    setLoading(false);
  }
}

function renderPlan(plan) {
  const emptyState = document.getElementById("emptyState");
  const planContent = document.getElementById("planContent");
  const planGoal = document.getElementById("planGoal");
  const planStatus = document.getElementById("planStatus");
  const planGeneratedAt = document.getElementById("planGeneratedAt");
  const planSummary = document.getElementById("planSummary");
  const planSteps = document.getElementById("planSteps");
  const assignmentRows = document.getElementById("assignmentRows");
  const diagramContainer = document.getElementById("diagramContainer");
  const ganttContainer = document.getElementById("ganttContainer");

  emptyState.classList.add("hidden");
  planContent.classList.remove("hidden");

  planGoal.textContent = plan.goal || "Untitled goal";
  planStatus.textContent = plan.status || "Ready";
  planGeneratedAt.textContent = formatTimestamp(plan.generatedAt);
  planSummary.textContent =
    plan.summary || "No summary was returned by the backend.";

  planSteps.innerHTML = "";
  (plan.steps || []).forEach((step) => {
    const item = document.createElement("li");
    item.className = "step-item";
    item.innerHTML = `
      <div class="step-index">${escapeHtml(String(step.order))}</div>
      <div class="step-copy">
        <h4>${escapeHtml(step.title || "Step")}</h4>
        <p>${escapeHtml(step.details || "")}</p>
      </div>
    `;
    planSteps.appendChild(item);
  });

  latestAssignments = plan.assignments || [];
  renderAssignments(assignmentRows, latestAssignments);

  latestDiagram = plan.mermaidDiagram || "";
  renderDiagram(diagramContainer, latestDiagram);

  latestGantt = plan.ganttDiagram || "";
  renderDiagram(ganttContainer, latestGantt);
}

function renderAssignments(container, assignments) {
  container.innerHTML = "";

  if (!assignments.length) {
    const row = document.createElement("tr");
    row.innerHTML = `<td colspan="2" class="empty-cell">No assignments returned.</td>`;
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
      "<p class='diagram-fallback'>No Mermaid diagram was returned.</p>";
    return;
  }

  if (!window.mermaid) {
    container.innerHTML = `<pre class="diagram-fallback">${escapeHtml(diagramCode)}</pre>`;
    return;
  }

  try {
    const { svg } = await window.mermaid.render(
      `diagram-${Date.now()}`,
      diagramCode,
    );
    container.innerHTML = svg;
  } catch (error) {
    container.innerHTML = `<pre class="diagram-fallback">${escapeHtml(diagramCode)}</pre>`;
  }
}

function clearStates() {
  const emptyState = document.getElementById("emptyState");
  const errorState = document.getElementById("errorState");
  const loadingState = document.getElementById("loadingState");

  errorState.classList.add("hidden");
  loadingState.classList.add("hidden");
  emptyState.classList.add("hidden");
}

function setLoading(isLoading) {
  const loadingState = document.getElementById("loadingState");
  const generateBtn = document.getElementById("generateBtn");

  loadingState.classList.toggle("hidden", !isLoading);
  generateBtn.disabled = isLoading;
  generateBtn.textContent = isLoading ? "Generating..." : "Generate plan";
}

function showError(message) {
  const errorState = document.getElementById("errorState");
  const errorMessage = document.getElementById("errorMessage");
  const emptyState = document.getElementById("emptyState");
  const planContent = document.getElementById("planContent");

  emptyState.classList.add("hidden");
  planContent.classList.add("hidden");
  errorMessage.textContent = message;
  errorState.classList.remove("hidden");
  setStatus(message);
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
  if (!value) {
    return "Just now";
  }

  const parsed = new Date(value);
  if (Number.isNaN(parsed.getTime())) {
    return "Just now";
  }

  return parsed.toLocaleString();
}

function toSlug(value) {
  return String(value)
    .toLowerCase()
    .replace(/[^a-z0-9]+/g, "-")
    .replace(/^-+|-+$/g, "");
}
