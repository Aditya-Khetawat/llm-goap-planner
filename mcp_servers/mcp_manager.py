from .search_server import SearchServer
from .calendar_server import CalendarServer
from .invite_server import InviteServer
from .budget_server import BudgetServer
from .food_server import FoodServer

SERVER_MAP = {
    "SearchAgent": SearchServer(),
    "CalendarAgent": CalendarServer(),
    "InviteAgent": InviteServer(),
    "BudgetAgent": BudgetServer(),
    "FoodAgent": FoodServer()
}


def execute_plan(plan: dict, allowed_tools=None):
    results = []

    for task in plan["tasks"]:
        agent = task.get("agent")

        # 🔥 If tool not allowed → fallback to LLM
        if allowed_tools and agent not in allowed_tools:
            results.append({
                "type": "LLM",
                "task": task["description"],
                "status": "handled_by_llm"
            })
            continue

        server = SERVER_MAP.get(agent)

        if not server:
            # fallback to LLM
            results.append({
                "type": "LLM",
                "task": task["description"],
                "status": "no_server_found"
            })
            continue

        result = server.execute(task)
        results.append(result)

    return results
