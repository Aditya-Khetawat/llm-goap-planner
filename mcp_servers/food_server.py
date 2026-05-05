from .base_server import MCPServer


class FoodServer(MCPServer):
    def __init__(self):
        super().__init__("FoodServer")

    def execute(self, task):
        return {
            "server": self.name,
            "action": "handle_resources",
            "task": task["description"],
            "status": "prepared"
        }
