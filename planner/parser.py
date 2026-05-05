import json
import re


def extract_json(text: str):
    # Try direct parse first
    try:
        return json.loads(text)
    except:
        pass

    # Extract JSON block
    match = re.search(r"\{.*", text, re.DOTALL)
    if not match:
        return None

    json_str = match.group()

    # 🔧 Fix 1: balance braces
    open_braces = json_str.count("{")
    close_braces = json_str.count("}")

    if close_braces < open_braces:
        json_str += "}" * (open_braces - close_braces)

    # 🔧 Fix 2: remove trailing commas
    json_str = re.sub(r",\s*}", "}", json_str)
    json_str = re.sub(r",\s*]", "]", json_str)

    try:
        return json.loads(json_str)
    except:
        return None
