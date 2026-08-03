package io.github.kisaragieffective.recomposedlogic.logic;

import java.util.LinkedHashMap;
import java.util.Map;

public final class CircuitNode {
    private final String id;
    private final NodeKind kind;
    private final Map<String, PortRef> inputs = new LinkedHashMap<>();
    private double scalarValue;
    private ComparatorOperator comparatorOperator = ComparatorOperator.EQUALS;
    private RomProgram romProgram;

    public CircuitNode(String id, NodeKind kind) {
        this.id = id;
        this.kind = kind;
    }

    public String id() {
        return id;
    }

    public NodeKind kind() {
        return kind;
    }

    public Map<String, PortRef> inputs() {
        return inputs;
    }

    public void connect(String input, PortRef source) {
        inputs.put(input, source);
    }

    public double scalarValue() {
        return scalarValue;
    }

    public void scalarValue(double scalarValue) {
        this.scalarValue = scalarValue;
    }

    public ComparatorOperator comparatorOperator() {
        return comparatorOperator;
    }

    public void comparatorOperator(ComparatorOperator comparatorOperator) {
        this.comparatorOperator = comparatorOperator;
    }

    public RomProgram romProgram() {
        return romProgram;
    }

    public void romProgram(RomProgram romProgram) {
        this.romProgram = romProgram;
    }
}
