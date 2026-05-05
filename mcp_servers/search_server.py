from .base_server import MCPServer


class SearchServer(MCPServer):
    def __init__(self):
        super().__init__("SearchServer")

    def execute(self, task):
        return {
            "server": self.name,
            "action": "reasoning",
            "task": task["description"],
            "status": "completed"
        }
