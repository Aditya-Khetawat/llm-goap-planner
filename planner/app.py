import streamlit as st
import requests

st.set_page_config(page_title="GOAP Planner", layout="wide")

st.title("🧠 GOAP Planner (Local LLM + MCP)")

# Input
goal = st.text_input("Enter your goal:",
                     placeholder="e.g. Plan a startup presentation")

# Button
if st.button("Generate Plan"):

    if not goal:
        st.warning("Please enter a goal")
    else:
        try:
            with st.spinner("Generating plan..."):
                res = requests.post(
                    "http://localhost:8000/plan",
                    json={"goal": goal}
                )

            st.write("Status:", res.status_code)

            data = res.json()

            if "error" in data:
                st.error(data["error"])
            else:
                st.subheader("📋 Tasks")
                st.json(data["tasks"])

                st.subheader("⚙️ Execution")
                for step in data["execution"]:
                    st.write(step)

                st.subheader("🔄 Flowchart")
                st.components.v1.html(f"""
                <script src="https://cdn.jsdelivr.net/npm/mermaid/dist/mermaid.min.js"></script>
                <div class="mermaid">
                {data['flowchart']}
                </div>
                <script>mermaid.initialize({{startOnLoad:true}});</script>
                """, height=500)

                st.subheader("📊 Gantt Chart")
                st.components.v1.html(f"""
                <script src="https://cdn.jsdelivr.net/npm/mermaid/dist/mermaid.min.js"></script>
                <div class="mermaid">
                {data['gantt']}
                </div>
                <script>mermaid.initialize({{startOnLoad:true}});</script>
                """, height=500)

        except Exception as e:
            st.error(f"Error: {e}")