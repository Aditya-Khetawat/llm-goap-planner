package com.cps.mcp.tool;

import com.embabel.agent.core.ToolGroup;
import com.embabel.agent.core.ToolGroupMetadata;
import com.embabel.agent.api.tool.Tool;
import org.springframework.stereotype.Component;

import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.List;

/**
 * Production implementation of the native MCP Echo Tool Group.
 * This class exposes exactly one trivial tool named 'echo' that returns its input.
 */
@Component
public class EchoToolGroup implements ToolGroup {

    private final ToolGroupMetadata metadata;

    public EchoToolGroup() {
        // We use java.lang.reflect.Proxy here because the Kotlin interface ToolGroupMetadata 
        // contains a compiler-generated method name with a hyphen (getVersion-Id9oKnY) 
        // which represents the Kotlin inline class Semver. This name-mangling prevents 
        // compiling a direct Java class implementing the interface. The dynamic proxy 
        // intercepts method calls at runtime and bypasses this compiler restriction.
        this.metadata = (ToolGroupMetadata) Proxy.newProxyInstance(
                ToolGroupMetadata.class.getClassLoader(),
                new Class<?>[]{ToolGroupMetadata.class},
                (proxy, method, args) -> {
                    switch (method.getName()) {
                        case "getRole":
                            return "echo";
                        case "getDescription":
                            return "Echoes back the input";
                        case "getName":
                            return "EchoToolGroup";
                        case "getProvider":
                            return "local";
                        case "getPermissions":
                            return Collections.emptySet();
                        case "infoString":
                            return "EchoToolGroup 1.0.0";
                        case "toString":
                            return "EchoToolGroup";
                        default:
                            if (method.getName().contains("getVersion")) {
                                return "1.0.0";
                            }
                            return null;
                    }
                }
        );
    }

    @Override
    public ToolGroupMetadata getMetadata() {
        return metadata;
    }

    @Override
    public List<Tool> getTools() {
        Tool echoTool = Tool.fromFunction(
                "echo",
                "Echoes back the input",
                String.class,
                String.class,
                input -> {
                    if (input == null) {
                        throw new IllegalArgumentException("Input cannot be null");
                    }
                    if ("fail".equalsIgnoreCase(input)) {
                        throw new RuntimeException("Simulated tool error");
                    }
                    return input;
                }
        );
        return List.of(echoTool);
    }
}
