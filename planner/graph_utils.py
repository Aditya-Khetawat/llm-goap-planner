from collections import defaultdict, deque


def topological_sort(tasks):
    graph = defaultdict(list)
    indegree = defaultdict(int)

    for task in tasks:
        indegree[task["id"]] = 0

    for task in tasks:
        for dep in task["dependencies"]:
            graph[dep].append(task["id"])
            indegree[task["id"]] += 1

    queue = deque([t for t in tasks if indegree[t["id"]] == 0])
    ordered = []

    while queue:
        current = queue.popleft()
        ordered.append(current)

        for neighbor in graph[current["id"]]:
            indegree[neighbor] -= 1
            if indegree[neighbor] == 0:
                next_task = next(t for t in tasks if t["id"] == neighbor)
                queue.append(next_task)

    if len(ordered) != len(tasks):
        return None  # cycle detected

    return ordered
