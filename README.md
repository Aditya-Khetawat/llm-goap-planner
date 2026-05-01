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

### Current Features

- **REST API Backend** - Spring Boot-based backend for handling planning requests
- **Web Interface** - Clean, responsive HTML/CSS/JavaScript frontend
- **Goal Input** - Simple textarea for entering planning goals
- **Modular Architecture** - Organized package structure for scalability

### Planned Features

- GOAP planning algorithm implementation
- LLM integration via Ollama for natural language processing
- Plan visualization using Mermaid.js
- Step-by-step action decomposition
- Interactive plan exploration UI

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

## Current Progress

### Completed

- ✅ Project initialization with Spring Boot and Maven
- ✅ Package structure established
- ✅ TestController with `/hello` endpoint
- ✅ Frontend UI (HTML/CSS/JavaScript)
- ✅ Project documentation

### In Progress

- 🔄 GOAP planning algorithm implementation
- 🔄 Ollama integration setup
- 🔄 API endpoint development

### Planned

- 📋 Plan visualization with Mermaid.js
- 📋 Unit and integration tests
- 📋 Deployment documentation
- 📋 Extended API documentation

## Setup Instructions

### Prerequisites

- **Java 17+** - [Install from Oracle](https://www.oracle.com/java/technologies/downloads/) or use OpenJDK
- **Maven 3.8+** - [Install Maven](https://maven.apache.org/download.cgi)
- **Ollama** (optional, for LLM features) - [Install Ollama](https://ollama.ai)

### Local Development

1. **Clone the repository**

   ```bash
   git clone <repository-url>
   cd goap-planner
   ```

2. **Build the project**

   ```bash
   mvn clean install
   ```

3. **Run the application**

   ```bash
   mvn spring-boot:run
   ```

4. **Access the application**
   - Web UI: `http://localhost:8080`
   - API Test: `http://localhost:8080/hello`

### Project Structure

```
src/
├── main/
│   ├── java/com/ip3b/goap_planner/
│   │   ├── GoapPlannerApplication.java    # Main Spring Boot app
│   │   ├── controller/
│   │   ├── service/
│   │   ├── planner/
│   │   ├── agents/
│   │   ├── model/
│   │   ├── visualization/
│   │   └── config/
│   └── resources/
│       ├── application.properties
│       └── static/
│           ├── index.html
│           ├── style.css
│           └── app.js
└── test/
    └── java/com/ip3b/goap_planner/
```

### Building and Testing

```bash
# Build the project
mvn clean build

# Run tests
mvn test

# Run specific test class
mvn test -Dtest=GoapPlannerApplicationTests

# Package as JAR
mvn package
```

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
