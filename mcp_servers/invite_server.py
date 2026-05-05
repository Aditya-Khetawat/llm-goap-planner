from .base_server import MCPServer


class InviteServer(MCPServer):
    def __init__(self):
        super().__init__("InviteServer")

    def execute(self, task):
        return {
            "server": self.name,
            "action": "send_invites",
            "task": task["description"],
            "status": "sent"
        }
