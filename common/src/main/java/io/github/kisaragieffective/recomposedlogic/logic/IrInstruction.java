package io.github.kisaragieffective.recomposedlogic.logic;

import java.util.List;

public record IrInstruction(String id, NodeKind kind, SignalType outputType, List<String> inputs, double scalarValue, ComparatorOperator comparatorOperator, RomProgram romProgram) {
}
