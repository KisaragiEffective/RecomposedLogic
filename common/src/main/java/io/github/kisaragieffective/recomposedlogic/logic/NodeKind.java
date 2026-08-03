package io.github.kisaragieffective.recomposedlogic.logic;

public enum NodeKind {
    SCALAR_CONSTANT,
    SCALAR_ADD,
    SCALAR_COMPARATOR,
    BOOLEAN_AND,
    BOOLEAN_NOT,
    BOOLEAN_CONDITIONAL,
    REDSTONE_IMPORTER,
    REDSTONE_EXPORTER,
    EXTERNAL_INPUT,
    EXTERNAL_OUTPUT,
    ROM
}
