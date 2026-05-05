def generate_flowchart(plan: dict) -> str:
    lines = ["graph TD"]

    tasks = plan["tasks"]

    # START / END nodes
    lines.append('START(("Start"))')
    lines.append('END(("End"))')

    # collect ids
    task_ids = {t["id"] for t in tasks}

    # track which nodes are depended upon (to find leaves)
    depended_on = set()

    for task in tasks:
        tid = task["id"]
        label = task["description"].replace('"', '')

        # node
        lines.append(f'{tid}["{label}"]')

        if not task["dependencies"]:
            # root tasks connect from START
            lines.append(f"START --> {tid}")

        for dep in task["dependencies"]:
            lines.append(f"{dep} --> {tid}")
            depended_on.add(dep)

    # leaf nodes (no one depends on them)
    leaf_nodes = task_ids - depended_on
    for leaf in leaf_nodes:
        lines.append(f"{leaf} --> END")

    # optional: nicer spacing
    lines.append("classDef default fill:#eef,stroke:#88a,stroke-width:1px;")

    return "\n".join(lines)


def generate_gantt(plan: dict) -> str:
    lines = [
        "gantt",
        "title Project Plan",
        "dateFormat  YYYY-MM-DD",
        "section Tasks"
    ]

    start_day = 1

    for task in plan["tasks"]:
        tid = task["id"]
        desc = task["description"].replace(":", "")

        # simple sequential schedule (1 day each)
        lines.append(f"{tid} {desc} : {start_day}, 1d")
        start_day += 1

    return "\n".join(lines)
