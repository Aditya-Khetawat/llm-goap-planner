from .capability_mapper import classify_capability

CAPABILITY_TO_AGENT = {
    "reasoning": "SearchAgent",
    "communication": "InviteAgent",
    "scheduling": "CalendarAgent",
    "financial": "BudgetAgent",
    "resource": "FoodAgent"
}


VALID_AGENTS = {
    "SearchAgent",
    "CalendarAgent",
    "FoodAgent",
    "InviteAgent",
    "BudgetAgent"
}


def fix_agents(plan: dict) -> dict:
    for task in plan["tasks"]:
        desc = task["description"].lower()
        agent = task.get("agent")

        # 🔥 Override clearly wrong cases (presentation/content)
        if any(w in desc for w in ["presentation", "slides", "handout", "deck"]):
            task["agent"] = "SearchAgent"
            continue

        # ✅ If LLM gave a valid agent → trust it
        if agent in VALID_AGENTS:
            continue

        # 🔧 Otherwise → fallback using capability
        capability = classify_capability(task["description"])
        task["agent"] = CAPABILITY_TO_AGENT[capability]

    return plan
