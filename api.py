from fastapi import FastAPI
from pydantic import BaseModel
from planner.planner import generate_plan
from typing import List

app = FastAPI()


class PlanRequest(BaseModel):
    goal: str
    tools: List[str] = []


@app.post("/plan")
def create_plan(request: PlanRequest):
    result = generate_plan(request.goal, request.tools)
    return result
