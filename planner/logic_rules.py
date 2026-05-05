def enforce_logic(plan: dict) -> dict:
    tasks = plan["tasks"]

    for task in tasks:
        desc = task["description"].lower()

        for other in tasks:
            other_desc = other["description"].lower()

            # 🔹 Resource dependency (budget-like tasks)
            if "budget" in other_desc:
                if any(word in desc for word in ["order", "buy", "arrange", "purchase"]):
                    if other["id"] not in task["dependencies"]:
                        task["dependencies"].append(other["id"])

            # 🔹 Information dependency (list → invite)
            if "list" in other_desc:
                if any(word in desc for word in ["invite", "send"]):
                    if other["id"] not in task["dependencies"]:
                        task["dependencies"].append(other["id"])

            # 🔹 Temporal dependency (schedule before execution)
            if any(word in other_desc for word in ["date", "time", "schedule"]):
                if any(word in desc for word in ["invite", "event", "setup"]):
                    if other["id"] not in task["dependencies"]:
                        task["dependencies"].append(other["id"])

    return plan
