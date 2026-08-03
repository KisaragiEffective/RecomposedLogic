package io.github.kisaragieffective.recomposedlogic.logic;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class CircuitGraph {
    private final Map<String, CircuitNode> nodes = new LinkedHashMap<>();
    private final List<String> diagnostics = new ArrayList<>();

    public void add(CircuitNode node) {
        nodes.put(node.id(), node);
    }

    public Map<String, CircuitNode> nodes() {
        return nodes;
    }

    public List<String> diagnostics() {
        return diagnostics;
    }

    public CircuitNode node(String id) {
        return nodes.get(id);
    }
}
