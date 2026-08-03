package io.github.kisaragieffective.recomposedlogic.world;

import io.github.kisaragieffective.recomposedlogic.logic.CircuitEvaluator;
import io.github.kisaragieffective.recomposedlogic.logic.SignalValue;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class WorldRuntimeEvaluator {
    private WorldRuntimeEvaluator() {
    }

    public static double scalarInput(Level level, BlockPos pos, String inputName) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof LogicBlockEntity logic)) {
            return 0.0;
        }
        for (PortSpec spec : Ports.specs(logic)) {
            if (!spec.output() && spec.name().equals(inputName)) {
                return valueAt(level, pos.relative(spec.direction()), new HashSet<>()).asScalar();
            }
        }
        return 0.0;
    }

    public static SignalValue valueAt(Level level, BlockPos pos, Set<BlockPos> visited) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof LogicBlockEntity logic && (logic.kind() == LogicBlockKind.PIN_CABLE || logic.kind() == LogicBlockKind.JUNCTION)) {
            if (!visited.add(pos)) {
                return SignalValue.ZERO;
            }
            for (Direction direction : Direction.values()) {
                SignalValue value = valueAt(level, pos.relative(direction), visited);
                if (value.type() == io.github.kisaragieffective.recomposedlogic.logic.SignalType.BOOLEAN && value.asBoolean()) {
                    return value;
                }
                if (value.type() == io.github.kisaragieffective.recomposedlogic.logic.SignalType.SCALAR && value.asScalar() != 0.0) {
                    return value;
                }
            }
            return SignalValue.ZERO;
        }
        if (blockEntity instanceof LogicBlockEntity logic) {
            return evaluateBlock(level, pos, logic, visited);
        }
        return SignalValue.ZERO;
    }

    private static SignalValue evaluateBlock(Level level, BlockPos pos, LogicBlockEntity logic, Set<BlockPos> visited) {
        return switch (logic.kind()) {
            case SCALAR_CONSTANT -> SignalValue.scalar(logic.scalarValue());
            case SCALAR_ADD -> SignalValue.scalar(input(level, pos, logic, "left", visited).asScalar() + input(level, pos, logic, "right", visited).asScalar());
            case SCALAR_COMPARATOR -> SignalValue.bool(logic.comparatorOperator().test(input(level, pos, logic, "left", visited).asScalar(), input(level, pos, logic, "right", visited).asScalar()));
            case BOOLEAN_AND -> SignalValue.bool(input(level, pos, logic, "left", visited).asBoolean() && input(level, pos, logic, "right", visited).asBoolean());
            case BOOLEAN_NOT -> SignalValue.bool(!input(level, pos, logic, "value", visited).asBoolean());
            case BOOLEAN_CONDITIONAL -> SignalValue.scalar(input(level, pos, logic, "condition", visited).asBoolean() ? input(level, pos, logic, "ifTrue", visited).asScalar() : input(level, pos, logic, "ifFalse", visited).asScalar());
            case REDSTONE_IMPORTER -> SignalValue.scalar(level.getBestNeighborSignal(pos));
            case ROM_POWERING_GATE -> evaluateRom(level, pos, logic, visited);
            case REDSTONE_EXPORTER -> SignalValue.scalar(input(level, pos, logic, "value", visited).asScalar());
            case EXTERNAL_PORT -> logic.externalOutput() ? input(level, pos, logic, "value", visited) : SignalValue.ZERO;
            default -> SignalValue.ZERO;
        };
    }

    private static SignalValue evaluateRom(Level level, BlockPos pos, LogicBlockEntity logic, Set<BlockPos> visited) {
        if (logic.romProgram() == null) {
            return SignalValue.ZERO;
        }
        Map<String, SignalValue> inputs = new HashMap<>();
        if (!logic.romProgram().inputs().isEmpty()) {
            inputs.put(logic.romProgram().inputs().get(0), input(level, pos, logic, "input0", visited));
        }
        Map<String, SignalValue> values = CircuitEvaluator.evaluate(logic.romProgram(), inputs);
        if (logic.romProgram().outputs().isEmpty()) {
            return SignalValue.ZERO;
        }
        return values.getOrDefault(logic.romProgram().outputs().get(0), SignalValue.ZERO);
    }

    private static SignalValue input(Level level, BlockPos pos, LogicBlockEntity logic, String name, Set<BlockPos> visited) {
        for (PortSpec spec : Ports.specs(logic)) {
            if (!spec.output() && spec.name().equals(name)) {
                return valueAt(level, pos.relative(spec.direction()), new HashSet<>(visited));
            }
        }
        if (logic.kind() == LogicBlockKind.BOOLEAN_AND || logic.kind() == LogicBlockKind.BOOLEAN_NOT || name.equals("condition")) {
            return SignalValue.FALSE;
        }
        return SignalValue.ZERO;
    }
}
