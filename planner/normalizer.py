def normalize_plan(plan: dict) -> dict:
    if "tasks" not in plan:
        plan["tasks"] = []

    for task in plan["tasks"]:
        # Ensure all required fields exist
        if "id" not in task:
            task["id"] = "UNKNOWN"

        if "description" not in task:
            task["description"] = ""

        if "agent" not in task:
            task["agent"] = "SearchAgent"

        # 🔥 Critical fix
        if "dependencies" not in task:
            task["dependencies"] = []

    return plan
