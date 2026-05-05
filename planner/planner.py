from .prompt_builder import build_prompt
from .llm_client import call_llm
from .parser import extract_json
from .validator import validate_plan, clean_dependencies
from .agent_mapper import fix_agents
from .graph_utils import topological_sort
from .normalizer import normalize_plan
from .logic_rules import enforce_logic
from mcp_servers.mcp_manager import execute_plan
from .mermaid_generator import generate_flowchart, generate_gantt


def generate_plan(goal: str, tools: list = None):
    MAX_RETRIES = 3

    prompt = build_prompt(goal, tools)

    for attempt in range(MAX_RETRIES):
        # 1. Call LLM
        raw_output = call_llm(prompt)

        # 2. Extract JSON
        plan = extract_json(raw_output)
        if not plan:
            continue

        # 3. Normalize
        plan = normalize_plan(plan)

        # 4. Fix agents
        plan = fix_agents(plan)

        # 5. Clean dependencies
        plan = clean_dependencies(plan)

        # 6. Validate + logic
        plan = enforce_logic(plan)
        if not validate_plan(plan):
            continue

        # 7. Try ordering (detect cycles)
        ordered_tasks = topological_sort(plan["tasks"])

        if ordered_tasks:
            plan["tasks"] = ordered_tasks
            break  # ✅ SUCCESS

    else:
        # ❌ After retries failed → fallback
        if not plan:
            return {"error": "Failed to generate plan"}

        # 🔥 Remove dependencies to break cycles
        for t in plan.get("tasks", []):
            t["dependencies"] = []

        plan["tasks"] = plan.get("tasks", [])

    # 8. Execute
    execution_results = execute_plan(plan, tools)

    # 9. Generate Mermaid outputs
    flowchart = generate_flowchart(plan)
    gantt = generate_gantt(plan)

    # ✅ FINAL RESPONSE
    return {
        "goal": plan.get("goal", goal),
        "tasks": plan["tasks"],
        "execution": execution_results,
        "flowchart": flowchart,
        "gantt": gantt
    }
