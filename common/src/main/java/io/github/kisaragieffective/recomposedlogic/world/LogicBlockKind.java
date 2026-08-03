package io.github.kisaragieffective.recomposedlogic.world;

import io.github.kisaragieffective.recomposedlogic.logic.NodeKind;

public enum LogicBlockKind {
    PIN_CABLE(null),
    JUNCTION(null),
    SCALAR_CONSTANT(NodeKind.SCALAR_CONSTANT),
    SCALAR_ADD(NodeKind.SCALAR_ADD),
    SCALAR_COMPARATOR(NodeKind.SCALAR_COMPARATOR),
    BOOLEAN_AND(NodeKind.BOOLEAN_AND),
    BOOLEAN_NOT(NodeKind.BOOLEAN_NOT),
    BOOLEAN_CONDITIONAL(NodeKind.BOOLEAN_CONDITIONAL),
    REDSTONE_IMPORTER(NodeKind.REDSTONE_IMPORTER),
    REDSTONE_EXPORTER(NodeKind.REDSTONE_EXPORTER),
    CIRCUIT_ORIGIN(null),
    CIRCUIT_SCANNER(null),
    EXTERNAL_PORT(NodeKind.EXTERNAL_INPUT),
    ROM_PROGRAMMER(null),
    ROM_POWERING_GATE(NodeKind.ROM);

    private final NodeKind nodeKind;

    LogicBlockKind(NodeKind nodeKind) {
        this.nodeKind = nodeKind;
    }

    public NodeKind nodeKind() {
        return nodeKind;
    }
}
