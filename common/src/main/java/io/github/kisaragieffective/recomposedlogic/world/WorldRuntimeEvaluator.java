package io.github.kisaragieffective.recomposedlogic.world;

import io.github.kisaragieffective.recomposedlogic.logic.CircuitEvaluator;
import io.github.kisaragieffective.recomposedlogic.logic.SignalType;
import io.github.kisaragieffective.recomposedlogic.logic.SignalValue;
import io.github.kisaragieffective.recomposedlogic.logic.RomProgram;
import io.github.kisaragieffective.recomposedlogic.world.block.PinCableBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class WorldRuntimeEvaluator {
    private WorldRuntimeEvaluator() {
    }

    public static double scalarInput(Level level, BlockPos pos, String inputName) {
        LogicBlockKind kind = LogicState.kind(level, pos);
        if (kind == null) {
            return 0.0;
        }
        for (PortSpec spec : LogicState.ports(level, pos)) {
            if (!spec.output() && spec.name().equals(inputName)) {
                SignalValue value = valueAtCableAware(level, pos.relative(spec.direction()), spec.direction().getOpposite(), null, true, new HashSet<>());
                return value.type() == SignalType.SCALAR ? value.asScalar() : 0.0;
            }
        }
        return 0.0;
    }

    public static SignalValue valueAt(Level level, BlockPos pos, Set<BlockPos> visited) {
        Set<CableVisit> cableVisited = new HashSet<>();
        for (BlockPos visitedPos : visited) {
            cableVisited.add(new CableVisit(visitedPos, null));
        }
        return valueAtCableAware(level, pos, null, null, false, cableVisited);
    }

    private static SignalValue valueAtCableAware(Level level, BlockPos pos, Direction requiredOutputDirection, DyeColor cableColor, boolean enteredFromPort, Set<CableVisit> visited) {
        LogicBlockKind kind = LogicState.kind(level, pos);
        if (kind == LogicBlockKind.PIN_CABLE || kind == LogicBlockKind.JUNCTION) {
            if (enteredFromPort) {
                PinCableBlock.TraversalConnection start = PinCableBlock.portTraversalStart(level, pos);
                if (!start.connected()) {
                    return SignalValue.ZERO;
                }
                cableColor = start.color();
            }
            if (!visited.add(new CableVisit(pos, cableColor))) {
                return SignalValue.ZERO;
            }
            for (Direction direction : Direction.values()) {
                BlockPos next = pos.relative(direction);
                LogicBlockKind nextKind = LogicState.kind(level, next);
                DyeColor nextColor = cableColor;
                if (nextKind == LogicBlockKind.PIN_CABLE || nextKind == LogicBlockKind.JUNCTION) {
                    PinCableBlock.TraversalConnection connection = PinCableBlock.traversalConnection(level, pos, next, cableColor);
                    if (!connection.connected()) {
                        continue;
                    }
                    nextColor = connection.color();
                } else {
                    PinCableBlock.TraversalConnection connection = PinCableBlock.portTraversalStart(level, pos);
                    if (!connection.connected() || (cableColor != null && connection.color() != null && cableColor != connection.color())) {
                        continue;
                    }
                }
                SignalValue value = valueAtCableAware(level, next, direction.getOpposite(), nextColor, false, new HashSet<>(visited));
                if (value.type() == SignalType.BOOLEAN && value.asBoolean()) {
                    return value;
                }
                if (value.type() == SignalType.SCALAR && value.asScalar() != 0.0) {
                    return value;
                }
            }
            return SignalValue.ZERO;
        }
        if (kind != null) {
            if (requiredOutputDirection != null && !hasOutputToward(level, pos, requiredOutputDirection)) {
                return SignalValue.ZERO;
            }
            return evaluateBlock(level, pos, kind, visited);
        }
        return SignalValue.ZERO;
    }

    private static boolean hasOutputToward(Level level, BlockPos pos, Direction direction) {
        for (PortSpec spec : LogicState.ports(level, pos)) {
            if (spec.output() && spec.direction() == direction) {
                return true;
            }
        }
        return false;
    }

    private static SignalValue evaluateBlock(Level level, BlockPos pos, LogicBlockKind kind, Set<CableVisit> visited) {
        return switch (kind) {
            case SCALAR_CONSTANT -> SignalValue.scalar(LogicState.scalarValue(level, pos));
            case SCALAR_COMPARATOR -> SignalValue.bool(LogicState.comparatorOperator(level, pos).test(input(level, pos, kind, "left", visited).asScalar(), input(level, pos, kind, "right", visited).asScalar()));
            case SCALAR_ADD -> SignalValue.scalar(input(level, pos, kind, "left", visited).asScalar() + input(level, pos, kind, "right", visited).asScalar());
            case BOOLEAN_AND -> SignalValue.bool(input(level, pos, kind, "left", visited).asBoolean() && input(level, pos, kind, "right", visited).asBoolean());
            case BOOLEAN_NOT -> SignalValue.bool(!input(level, pos, kind, "value", visited).asBoolean());
            case BOOLEAN_CONDITIONAL -> SignalValue.scalar(input(level, pos, kind, "condition", visited).asBoolean() ? input(level, pos, kind, "ifTrue", visited).asScalar() : input(level, pos, kind, "ifFalse", visited).asScalar());
            case REDSTONE_IMPORTER -> SignalValue.scalar(level.getBestNeighborSignal(pos));
            case ROM_POWERING_GATE -> evaluateRom(level, pos, kind, visited);
            case REDSTONE_EXPORTER -> SignalValue.scalar(input(level, pos, kind, "value", visited).asScalar());
            case EXTERNAL_PORT -> LogicState.externalOutput(level, pos) ? input(level, pos, kind, "value", visited) : SignalValue.ZERO;
            default -> SignalValue.ZERO;
        };
    }

    private static SignalValue evaluateRom(Level level, BlockPos pos, LogicBlockKind kind, Set<CableVisit> visited) {
        RomProgram romProgram = LogicState.romProgram(level, pos);
        if (romProgram == null) {
            return SignalValue.ZERO;
        }
        Map<String, SignalValue> inputs = new HashMap<>();
        if (!romProgram.inputs().isEmpty()) {
            inputs.put(romProgram.inputs().get(0), input(level, pos, kind, "input0", visited));
        }
        Map<String, SignalValue> values = CircuitEvaluator.evaluate(romProgram, inputs);
        if (romProgram.outputs().isEmpty()) {
            return SignalValue.ZERO;
        }
        return values.getOrDefault(romProgram.outputs().get(0), SignalValue.ZERO);
    }

    private static SignalValue input(Level level, BlockPos pos, LogicBlockKind kind, String name, Set<CableVisit> visited) {
        for (PortSpec spec : LogicState.ports(level, pos)) {
            if (!spec.output() && spec.name().equals(name)) {
                return valueAtCableAware(level, pos.relative(spec.direction()), spec.direction().getOpposite(), null, true, new HashSet<>(visited));
            }
        }
        if (kind == LogicBlockKind.BOOLEAN_AND || kind == LogicBlockKind.BOOLEAN_NOT || name.equals("condition")) {
            return SignalValue.FALSE;
        }
        return SignalValue.ZERO;
    }

    private record CableVisit(BlockPos pos, DyeColor color) {
    }
}
