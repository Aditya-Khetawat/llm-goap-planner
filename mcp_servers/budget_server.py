from .base_server import MCPServer


class BudgetServer(MCPServer):
    def __init__(self):
        super().__init__("BudgetServer")

    def execute(self, task):
        return {
            "server": self.name,
            "action": "calculate_budget",
            "task": task["description"],
            "status": "estimated"
        }
