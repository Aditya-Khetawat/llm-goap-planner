def classify_capability(task_desc: str) -> str:
    desc = task_desc.lower()

    # 🔥 1. Presentation/content ALWAYS reasoning
    if any(w in desc for w in [
        "presentation", "slides", "deck", "visual", "content",
        "design", "research", "prepare presentation", "outline"
    ]):
        return "reasoning"

    # Communication
    if any(w in desc for w in ["invite", "send", "email", "notify", "message"]):
        return "communication"

    # Scheduling
    if any(w in desc for w in ["schedule", "date", "time", "timeline", "deadline", "meeting"]):
        return "scheduling"

    # Financial
    if any(w in desc for w in ["budget", "cost", "revenue", "price", "financial", "funding"]):
        return "financial"

    # Physical resources ONLY (very strict)
    if any(w in desc for w in ["food", "drink", "catering", "venue", "equipment", "logistics"]):
        return "resource"

    return "reasoning"
