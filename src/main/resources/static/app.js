// GOAP Planner Frontend - Main Application

/**
 * Generate a plan based on the user's goal
 */
function generatePlan() {
  const goalInput = document.getElementById("goalInput");
  const goal = goalInput.value.trim();

  // Validate input
  if (!goal) {
    alert("Please enter a goal to generate a plan");
    return;
  }

  console.log("Generating plan for goal:", goal);

  // Show result section
  const resultSection = document.getElementById("resultSection");
  const planResult = document.getElementById("planResult");

  resultSection.classList.remove("hidden");
  planResult.innerHTML = "<p>Loading plan...</p>";

  // TODO: Send request to backend API
  // For now, just show a placeholder
  setTimeout(() => {
    planResult.innerHTML = `
            <div class="plan-placeholder">
                <p><strong>Goal:</strong> ${escapeHtml(goal)}</p>
                <p><strong>Status:</strong> Plan generation coming soon...</p>
            </div>
        `;
  }, 500);
}

/**
 * Escape HTML to prevent XSS attacks
 */
function escapeHtml(text) {
  const map = {
    "&": "&amp;",
    "<": "&lt;",
    ">": "&gt;",
    '"': "&quot;",
    "'": "&#039;",
  };
  return text.replace(/[&<>"']/g, (m) => map[m]);
}

/**
 * Handle Enter key in textarea to generate plan
 */
document.addEventListener("DOMContentLoaded", function () {
  const goalInput = document.getElementById("goalInput");
  const generateBtn = document.getElementById("generateBtn");

  // Allow Ctrl+Enter or Cmd+Enter to generate plan
  goalInput.addEventListener("keydown", function (event) {
    if ((event.ctrlKey || event.metaKey) && event.key === "Enter") {
      generatePlan();
    }
  });

  console.log("GOAP Planner initialized");
});
