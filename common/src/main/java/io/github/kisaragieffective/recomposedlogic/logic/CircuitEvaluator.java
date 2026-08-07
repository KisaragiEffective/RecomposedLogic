package io.github.kisaragieffective.recomposedlogic.logic;

import java.util.HashMap;
import java.util.Map;

public final class CircuitEvaluator {
    private CircuitEvaluator() {
    }

    public static Map<String, SignalValue> evaluate(RomProgram program, Map<String, SignalValue> externalInputs) {
        Map<String, SignalValue> values = new HashMap<>(externalInputs);
        for (IrInstruction instruction : program.instructions()) {
            SignalValue value = switch (instruction.kind()) {
                case SCALAR_CONSTANT -> new ScalarSignal(instruction.scalarValue());
                case SCALAR_ADD ->
                        new ScalarSignal(scalar(values, instruction, "left") + scalar(values, instruction, "right"));
                case SCALAR_COMPARATOR ->
                        BooleanSignal.of(instruction.comparatorOperator().test(scalar(values, instruction, "left"), scalar(values, instruction, "right")));
                case BOOLEAN_AND ->
                        BooleanSignal.of(bool(values, instruction, "left") && bool(values, instruction, "right"));
                case BOOLEAN_NOT -> BooleanSignal.of(!bool(values, instruction, "value"));
                case BOOLEAN_CONDITIONAL ->
                        new ScalarSignal(bool(values, instruction, "condition") ? scalar(values, instruction, "ifTrue") : scalar(values, instruction, "ifFalse"));
                case REDSTONE_IMPORTER, EXTERNAL_INPUT -> values.getOrDefault(instruction.id(), ScalarSignal.ZERO);
                case REDSTONE_EXPORTER, EXTERNAL_OUTPUT -> new ScalarSignal(scalar(values, instruction, "value"));
                case ROM -> evaluateRom(instruction, values);
            };
            values.put(instruction.id(), value);
        }
        return values;
    }

    private static SignalValue evaluateRom(IrInstruction instruction, Map<String, SignalValue> values) {
        if (instruction.romProgram() == null) {
            return ScalarSignal.ZERO;
        }
        Map<String, SignalValue> inputs = new HashMap<>();
        for (int i = 0; i < instruction.inputs().size() && i < instruction.romProgram().inputs().size(); i++) {
            String[] parts = instruction.inputs().get(i).split("=", 2);
            if (parts.length == 2) {
                inputs.put(instruction.romProgram().inputs().get(i), values.getOrDefault(parts[1], ScalarSignal.ZERO));
            }
        }
        Map<String, SignalValue> evaluated = evaluate(instruction.romProgram(), inputs);
        if (instruction.romProgram().outputs().isEmpty()) {
            return ScalarSignal.ZERO;
        }
        return evaluated.getOrDefault(instruction.romProgram().outputs().get(0), ScalarSignal.ZERO);
    }

    private static double scalar(Map<String, SignalValue> values, IrInstruction instruction, String input) {
        return values.getOrDefault(source(instruction, input), ScalarSignal.ZERO).asScalar();
    }

    private static boolean bool(Map<String, SignalValue> values, IrInstruction instruction, String input) {
        return values.getOrDefault(source(instruction, input), BooleanSignal.FALSE).asBoolean();
    }

    private static String source(IrInstruction instruction, String input) {
        for (String encoded : instruction.inputs()) {
            String[] parts = encoded.split("=", 2);
            if (parts.length == 2 && parts[0].equals(input)) {
                return parts[1];
            }
        }
        return "";
    }
}
