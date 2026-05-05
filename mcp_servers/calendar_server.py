from .base_server import MCPServer


class CalendarServer(MCPServer):
    def __init__(self):
        super().__init__("CalendarServer")

    def execute(self, task):
        return {
            "server": self.name,
            "action": "schedule",
            "task": task["description"],
            "status": "scheduled"
        }
