# GOAP Planner

## Project Overview

GOAP Planner is an academic prototype that demonstrates Goal-Oriented Action Planning (GOAP) integrated with Large Language Models (LLMs). The project combines traditional AI planning algorithms with modern LLM capabilities to generate intelligent action plans for complex goals.

This is a Spring Boot-based web application designed to prototype and visualize GOAP planning strategies in an accessible, interactive interface.

## Goals

- Develop a functional GOAP planning system that bridges traditional AI planning with LLM reasoning
- Create an intuitive interface for users to input goals and visualize generated action plans
- Demonstrate how LLMs can enhance traditional planning algorithms
- Build a foundation for research and experimentation in AI planning
- Provide a clean, maintainable codebase for academic purposes

## Features

### Current Features ✅

- **REST API Backend** - Spring Boot-based backend handling goal-to-plan conversion via POST `/api/plans`
- **Dynamic Plan Generation** - Goal-sensitive plan generation with three distinct blueprints:
  - Hackathon planning (event-focused with coordinated tasks)
  - Mobile app launch (development-focused with code review phases)
  - Birthday party (celebration-focused with vendor coordination)
  - Generic fallback for undefined goals
- **Dual Visualizations** - Mermaid flowchart (TD layout, compact sizing) and Gantt timeline (date-based scheduling)
- **Agent Assignment Orchestration** - Mock agent orchestration table with:
  - Step-to-agent mapping
  - Capability tracking (coding, design, testing, management, etc.)
  - Status indicators (ready, queued, in-progress, waiting)
  - Handoff chain visualization
  - Rationale for each assignment
- **Multi-Step Loading State** - Animated progress indicator cycling through "Generating plan...", "Analyzing goal...", "Assigning agents..."
- **Enhanced Error Handling** - Categorized error types:
  - Validation errors (missing/invalid input)
  - Network errors (connection failures)
  - Server errors (5xx responses)
  - Parse errors (malformed responses)
  - With error codes, recovery buttons, and color-coded UI
- **Responsive Design** - Mobile-first layout with breakpoints at 640px, 768px, and 1024px+
- **Smooth Animations** - CSS keyframes for:
  - slideInUp, slideInDown, fadeIn, bounce, scaleIn
  - Applied to plan reveal, loading states, and error cards
- **Copy-to-Clipboard** - Export Mermaid diagrams, Gantt charts, and agent tables as text
- **Web Interface** - Clean, gradient-based HTML5/CSS3 frontend with accessibility features

### Planned Features

- **LLM Integration** - Ollama backend for natural language goal analysis (currently blueprint-based keyword matching)
- **Extended Test Coverage** - Integration tests for factory methods and edge cases
- **Plan Persistence** - Database storage for generated plans and history
- **Interactive Plan Exploration** - Drag-and-drop step reordering, agent reassignment UI

## Tech Stack

| Component         | Technology               |
| ----------------- | ------------------------ |
| Backend Framework | Spring Boot              |
| Build Tool        | Maven                    |
| Language          | Java 17                  |
| LLM Engine        | Ollama (local inference) |
| Visualization     | Mermaid.js               |
| Frontend          | HTML5/CSS3/JavaScript    |
| Server            | Apache Tomcat (embedded) |

## Architecture Overview

```
GOAP Planner
├── src/main/java/com/ip3b/goap_planner/
│   ├── controller/          # REST API endpoints
│   ├── service/             # Business logic layer
│   ├── planner/             # GOAP planning algorithm
│   ├── agents/              # Agent definitions and behaviors
│   ├── model/               # Data models and entities
│   ├── visualization/       # Plan visualization utilities
│   └── config/              # Spring configuration
├── src/main/resources/
│   ├── static/              # Frontend files (HTML/CSS/JS)
│   └── application.properties
└── pom.xml                  # Maven configuration
```

### Component Descriptions

- **Controller Layer**: Handles HTTP requests and API endpoints
- **Service Layer**: Implements business logic and orchestrates planning operations
- **Planner Module**: Contains core GOAP algorithm implementation
- **Agents Module**: Defines agent behaviors and capabilities
- **Model Package**: Data structures for goals, actions, and states
- **Visualization**: Utilities for rendering plans as Mermaid diagrams

## Team Members

- Aditya Khetawat
- Aryan Thakur

## Current Progress

### Completed ✅

- ✅ Spring Boot project initialization and Maven configuration
- ✅ Package structure with controller, service, model, and visualization layers
- ✅ REST API endpoint: `POST /api/plans` with structured JSON request/response
- ✅ Goal-sensitive plan generation via `PlanService` with blueprint pattern:
  - Keyword-based blueprint selection (hackathon/app/party/generic)
  - Step generation with titles and details
  - Agent assignment generation with capabilities and status
- ✅ Mermaid diagram factory extraction (`MermaidPlanDiagramFactory`):
  - Compact flowchart rendering (nodeSpacing: 50, rankSpacing: 40)
  - Gantt timeline with date-based scheduling
  - Identifier sanitization and escape handling
- ✅ Multi-step loading indicator with 800ms state transitions and visual progress dots
- ✅ Enhanced error handling:
  - Error categorization (validation, network, server, parse, unknown)
  - Error codes and recovery UI (Retry/Clear buttons)
  - Color-coded error cards based on error type
- ✅ Responsive CSS layout:
  - Mobile breakpoint (640px) with stacked layout
  - Tablet breakpoint (768px) with optimized spacing
  - Desktop breakpoint (1024px+) with two-column layout
  - Fluid typography using CSS clamp()
- ✅ Smooth animations and transitions:
  - slideInUp, slideInDown, fadeIn, bounce, scaleIn keyframes
  - Enhanced button hover/active states with cubic-bezier easing
  - Plan content reveal animations
- ✅ Copy-to-clipboard functionality for all artifacts (diagrams, Gantt, agent table)
- ✅ Unit test for plan service (PlanServiceTest) verifying output divergence
- ✅ Full integration test suite passing (2/2 tests)

### In Progress 🔄

- 🔄 LLM integration planning (Ollama setup and prompt engineering)
- 🔄 Additional unit tests for edge cases and factory methods

### Planned 📋

- 📋 Ollama LLM backend for semantic goal analysis (replace keyword matching)
- 📋 Plan history and persistence (database layer)
- 📋 Interactive plan editor UI
- 📋 API documentation and OpenAPI spec
- 📋 Deployment guide (Docker, production configuration)

## Setup Instructions

### Prerequisites

- **Java 17+** - [Install from Oracle](https://www.oracle.com/java/technologies/downloads/) or use OpenJDK
- **Maven 3.8+** - [Install Maven](https://maven.apache.org/download.cgi)
- **Ollama** (optional, for LLM features) - [Install Ollama](https://ollama.ai)

### Local Development

1. **Clone the repository**

   ```bash
   git clone <repository-url>
   cd llm_goap
   ```

2. **Build the project** (Maven wrapper, no prior Maven install needed)

   ```bash
   # Windows
   .\mvnw.cmd clean install

   # macOS / Linux
   ./mvnw clean install
   ```

3. **Run the application**

   ```bash
   # Windows
   .\mvnw.cmd spring-boot:run

   # macOS / Linux
   ./mvnw spring-boot:run
   ```

4. **Access the application**
   - Web UI: `http://localhost:8080` (interactive form + plan output)
   - API: `POST http://localhost:8080/api/plans` (JSON request: `{"goal":"your goal here"}`)

### Sample API Request

```bash
curl -X POST http://localhost:8080/api/plans \
  -H "Content-Type: application/json" \
  -d '{"goal":"Organize a hackathon"}'
```

### Sample API Response

```json
{
  "goal": "Organize a hackathon",
  "summary": "Execute hackathon event with team coordination and venue setup.",
  "status": "Ready",
  "steps": [
    {
      "order": 1,
      "title": "Secure venue and sponsors",
      "details": "Book event location and finalize sponsorship deals."
    },
    {
      "order": 2,
      "title": "Coordinate volunteer team",
      "details": "Recruit and assign volunteer roles for the event."
    },
    {
      "order": 3,
      "title": "Execute event logistics",
      "details": "Set up registration, food, tech infrastructure, and judging."
    }
  ],
  "assignments": [
    {
      "order": 1,
      "stepTitle": "Secure venue and sponsors",
      "agent": "EventManager",
      "capability": "Event Planning",
      "status": "ready",
      "handoffTo": "VolunteerCoordinator",
      "rationale": "Manages external partnerships and venue logistics."
    }
  ],
  "mermaidDiagram": "flowchart TD\n    G[Goal: Organize a hackathon]\n    S1[Step 1: Secure venue and sponsors]\n    S2[Step 2: Coordinate volunteer team]\n    ...",
  "ganttDiagram": "gantt\n    title Organize a hackathon timeline\n    dateFormat YYYY-MM-DD\n    section Planning\n    Secure venue and sponsors : s1, 2026-05-04, 3d\n    ...",
  "generatedAt": "2026-05-04T02:10:23.456Z"
}
```

### API Contract

- **Endpoint**: `POST /api/plans`
- **Request Body**: JSON with `goal` field (string, required)
- **Response**: JSON with:
  - `goal` - Echo of input goal
  - `summary` - High-level plan overview
  - `status` - Plan status (always "Ready" for now)
  - `steps` - List of action steps (order, title, details)
  - `assignments` - Agent assignments with capabilities and status
  - `mermaidDiagram` - Flowchart in Mermaid syntax (TD layout, compact)
  - `ganttDiagram` - Timeline in Mermaid Gantt syntax (date-based scheduling)
  - `generatedAt` - ISO 8601 timestamp

### Frontend Features

- **Goal Input Panel** - Text area for entering planning goals
- **Multi-Step Loading** - Animated progress indicator (Generating → Analyzing → Assigning)
- **Plan Output** - Dynamic rendering of:
  - Goal summary grid (goal, status, timestamp)
  - Step list with numbered items and details
  - Agent orchestration table (step, agent, capability, status, handoff, rationale)
  - Mermaid flowchart (compact, animated rendering)
  - Mermaid Gantt timeline (date-based, color-coded sections)
- **Error Handling** - Color-coded error cards with error codes and recovery actions
- **Copy-to-Clipboard** - Export buttons for all artifacts (flowchart, Gantt, agent table)

### Project Structure

```
src/
├── main/
│   ├── java/com/ip3b/goap_planner/
│   │   ├── GoapPlannerApplication.java          # Spring Boot application entry point
│   │   ├── controller/
│   │   │   └── PlanningController.java          # REST endpoint: POST /api/plans
│   │   ├── service/
│   │   │   └── PlanService.java                 # Core planning logic, blueprint selection
│   │   ├── visualization/
│   │   │   └── MermaidPlanDiagramFactory.java   # Flowchart & Gantt generation
│   │   ├── model/
│   │   │   ├── PlanRequest.java                 # Request DTO (goal)
│   │   │   ├── PlanResponse.java                # Response DTO (full plan)
│   │   │   ├── PlanStep.java                    # Action step record
│   │   │   ├── PlanAssignment.java              # Agent assignment record
│   │   │   └── MermaidGanttTask.java            # Gantt task record
│   │   ├── agents/
│   │   ├── config/
│   │   └── planner/
│   └── resources/
│       ├── application.properties
│       └── static/
│           ├── index.html                       # Main UI (responsive, gradient design)
│           ├── style.css                        # Styling & animations (mobile-first)
│           └── app.js                           # Frontend logic & API calls
└── test/
    └── java/com/ip3b/goap_planner/
        ├── GoapPlannerApplicationTests.java     # Context load test
        └── service/
            └── PlanServiceTest.java             # Service logic tests (output divergence)
```

### Key Classes

- **PlanService**: Selects blueprint based on goal keywords, generates structured plan response
- **MermaidPlanDiagramFactory**: Builds Mermaid flowchart and Gantt diagram from plan data
- **PlanningController**: Exposes `/api/plans` POST endpoint
- **PlanRequest/Response**: Immutable records for type-safe DTO handling

### Building and Testing

```bash
# Windows
.\mvnw.cmd clean build
.\mvnw.cmd test
.\mvnw.cmd package

# macOS / Linux
./mvnw clean build
./mvnw test
./mvnw package

# Run specific test
.\mvnw.cmd test -Dtest=PlanServiceTest

# Run in development mode with auto-reload
.\mvnw.cmd spring-boot:run
```

### Test Results

Current test suite: **2/2 passing**

- `GoapPlannerApplicationTests` - Application context loads successfully
- `PlanServiceTest` - Verifies plan generation differs across three sample inputs (hackathon, app, party)

### Configuration

Edit `src/main/resources/application.properties` to configure:

- Server port
- Logging levels
- LLM engine settings
- Other Spring Boot properties

### Troubleshooting

- **Java not found**: Ensure Java 17+ is installed and in PATH
- **Maven not found**: Ensure Maven is installed and in PATH
- **Build fails**: Try `mvn clean install -DskipTests` to build without running tests
- **Port 8080 in use**: Change port in `application.properties`

## Development Notes

- This is an academic prototype - focus is on functionality and learning
- Keep code clean and well-commented for educational purposes
- Maintain modular design for easy testing and extension
- Follow Spring Boot best practices

## License

Academic project - designed for research and educational purposes.

---

**Last Updated**: May 2, 2026
