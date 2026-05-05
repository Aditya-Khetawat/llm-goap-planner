# GOAP Planner
 
> **Goal-Oriented Action Planning** powered by a locally hosted LLM and a modular MCP agent architecture.
 
---
 
## Overview
 
GOAP Planner takes a high-level plaintext goal and automatically decomposes it into an executable plan. It assigns each atomic task to a specialized MCP agent, builds a dependency graph, and runs the plan — falling back to the LLM when an agent is unavailable.
 
```
User Goal → LLM Planner → Task Graph (DAG) → MCP Execution Layer → JSON + Mermaid diagrams
```
 
**Each plan produces:**
- A structured task list with agent assignments and dependencies
- Execution results per task
- A Mermaid flowchart (task dependencies)
- A Mermaid Gantt chart (execution timeline)
---
 
## Features
 
| Feature | Status |
|---|---|
| Local LLM planning via Ollama | ✅ |
| GOAP-style task decomposition | ✅ |
| Modular MCP agent architecture | ✅ |
| Tool-constrained planning | ✅ |
| Execution engine with LLM fallback | ✅ |
| Mermaid visualization (Flowchart + Gantt) | ✅ |
| FastAPI backend | ✅ |
 
---
 
## Getting Started
 
### 1. Clone the repository
 
```bash
git clone <your-repo-url>
cd llm-goap-planner
```
 
### 2. Create a virtual environment
 
```bash
python -m venv venv
```
 
Activate it:
 
```bash
# Windows
venv\Scripts\activate
 
# macOS / Linux
source venv/bin/activate
```
 
### 3. Install dependencies
 
```bash
pip install -r requirements.txt
```
 
### 4. Start a local LLM with Ollama
 
Make sure [Ollama](https://ollama.com) is installed, then pull and run a model:
 
```bash
ollama run llama3
```
 
Any model supported by Ollama will work.
 
### 5. Start the API server
 
```bash
uvicorn api:app --reload
```
 
### 6. Open the interactive API docs
 
```
http://127.0.0.1:8000/docs
```
 
---
 
## API Reference
 
### `POST /plan`
 
**Request body:**
 
```json
{
  "goal": "Plan a startup presentation",
  "tools": ["SearchAgent", "InviteAgent", "FoodAgent"]
}
```
 
| Field | Type | Required | Description |
|---|---|---|---|
| `goal` | `string` | Yes | High-level task description |
| `tools` | `list[string]` | No | MCP agents to restrict planning to |
 
**Response:**
 
```json
{
  "goal": "...",
  "tasks": [
    {
      "id": "T1",
      "description": "...",
      "agent": "...",
      "dependencies": []
    }
  ],
  "execution": [
    {
      "server": "...",
      "action": "...",
      "task": "...",
      "status": "..."
    }
  ],
  "flowchart": "graph TD ...",
  "gantt": "gantt ..."
}
```
 
---
 
## MCP Agents
 
| Agent | Responsibility |
|---|---|
| `SearchAgent` | Research, reasoning, and planning |
| `CalendarAgent` | Scheduling and timelines |
| `FoodAgent` | Materials and resource coordination |
| `InviteAgent` | Communication and invitations |
| `BudgetAgent` | Cost estimation |
 
---
 
## Execution Logic
 
Tasks are dispatched using the following priority:
 
1. **MCP agent available** → handled by the corresponding MCP server
2. **MCP agent unavailable** → handled by LLM fallback
This ensures robustness even when only a subset of agents is configured.
 
---
 
## Visualization
 
The `/plan` endpoint returns Mermaid diagram strings that can be rendered with:
 
- [Mermaid Live Editor](https://mermaid.live)
- [Streamlit](https://streamlit.io)
- Any web frontend with Mermaid support
---
 
## Notes
 
- LLM output is validated and corrected before execution
- A retry mechanism improves stability on malformed responses
- The system is designed to run fully **offline** with a local LLM
---
 
## Example Use Cases
 
- Startup planning and pitch prep
- Event organization
- Task automation workflows
- AI planning demonstrations and research
---