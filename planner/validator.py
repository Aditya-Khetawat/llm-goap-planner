VALID_AGENTS = {
    "SearchAgent",
    "CalendarAgent",
    "FoodAgent",
    "InviteAgent",
    "BudgetAgent"
}


def validate_plan(plan: dict) -> bool:
    if not plan or "tasks" not in plan:
        return False

    ids = set()

    for task in plan["tasks"]:
        if not all(k in task for k in ["id", "description", "agent", "dependencies"]):
            return False

        if task["agent"] not in VALID_AGENTS:
            return False

        ids.add(task["id"])

    for task in plan["tasks"]:
        for dep in task["dependencies"]:
            if dep not in ids:
                return False

    return True


def clean_dependencies(plan: dict) -> dict:
    valid_ids = {task["id"] for task in plan["tasks"]}

    for task in plan["tasks"]:
        task["dependencies"] = [
            dep for dep in task["dependencies"] if dep in valid_ids
        ]

    return plan
