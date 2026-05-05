def build_prompt(goal: str, tools: list = None) -> str:
   tool_text = ""


   if tools:
      tool_text = "\n".join([f"- {t}" for t in tools])
   else:
      tool_text = """
   - SearchAgent
   - CalendarAgent
   - FoodAgent
   - InviteAgent
   - BudgetAgent
"""
   
   return f"""
You are a deterministic planning system.

You MUST output strictly valid JSON. No extra text. No explanations.

--------------------------------------

AVAILABLE TOOLS:
{tool_text}

--------------------------------------

STRICT RULES:

1. Output MUST be valid JSON parsable by Python.
2. DO NOT include markdown, comments, or explanations.
3. DO NOT include trailing commas.
4. ALWAYS include ALL fields:
   - id
   - description
   - agent
   - dependencies

5. Task IDs MUST be:
   T1, T2, T3, ... (sequential)

6. Dependencies:
   - Can ONLY refer to previous tasks
   - MUST form a valid DAG (NO cycles)
   - Prefer a connected workflow (minimize independent tasks)

7. Each task must be atomic and clear.

--------------------------------------

OUTPUT FORMAT (STRICT):

{{
  "goal": "{goal}",
  "tasks": [
    {{
      "id": "T1",
      "description": "First step",
      "agent": "SearchAgent",
      "dependencies": []
    }},
    {{
      "id": "T2",
      "description": "Next step",
      "agent": "CalendarAgent",
      "dependencies": ["T1"]
    }}
  ]
}}

--------------------------------------

Now generate the plan.

Goal:
{goal}
"""
