class MCPServer:
    def __init__(self, name: str):
        self.name = name

    def execute(self, task: dict) -> dict:
        raise NotImplementedError
