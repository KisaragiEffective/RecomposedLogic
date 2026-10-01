package io.github.kisaragieffective.recomposedlogic.world;

import io.github.kisaragieffective.recomposedlogic.logic.*;
import io.github.kisaragieffective.recomposedlogic.world.block.PinCableBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

public final class WorldRuntimeEvaluator {
    private static final Map<Level, TickCache> TICK_CACHES = new WeakHashMap<>();

    private WorldRuntimeEvaluator() {
    }

    public static double scalarInput(Level level, BlockPos pos, String inputName) {
        LogicBlockKind kind = LogicState.kind(level, pos);
        if (kind == null) {
            return 0.0;
        }
        for (PortSpec spec : LogicState.ports(level, pos)) {
            if (!spec.output() && spec.name().equals(inputName)) {
                SignalValue value = valueAtCableAware(level, pos.relative(spec.direction()), spec.direction().getOpposite(), null, true, new EvaluationContext(tickCache(level), true));
                return value instanceof ScalarSignal ss ? ss.getValue() : 0.0;
            }
        }
        return 0.0;
    }

    public static SignalValue valueAt(Level level, BlockPos pos, Set<BlockPos> visited) {
        EvaluationContext context = new EvaluationContext(tickCache(level), false);
        for (BlockPos visitedPos : visited) {
            context.visited.add(new CableVisit(visitedPos, null));
        }
        return valueAtCableAware(level, pos, null, null, false, context);
    }

    private static SignalValue valueAtCableAware(Level level, BlockPos pos, Direction requiredOutputDirection, DyeColor cableColor, boolean enteredFromPort, EvaluationContext context) {
        LogicBlockKind kind = LogicState.kind(level, pos);
        if (kind == LogicBlockKind.PIN_CABLE || kind == LogicBlockKind.JUNCTION) {
            if (enteredFromPort) {
                PinCableBlock.TraversalConnection start = PinCableBlock.portTraversalStart(level, pos);
                if (!start.connected()) {
                    return ScalarSignal.ZERO;
                }
                cableColor = start.color();
            }
            return evaluateCableComponent(level, context, new CableVisit(pos, cableColor));
        }
        if (kind != null) {
            if (requiredOutputDirection != null && !hasOutputToward(level, pos, requiredOutputDirection)) {
                return ScalarSignal.ZERO;
            }
            return evaluateBlock(level, pos, kind, context);
        }
        return ScalarSignal.ZERO;
    }

    private static SignalValue evaluateCableComponent(Level level, EvaluationContext context, CableVisit start) {
        PerLevelNetworkDiagramCache.CableNetwork component = context.networkFor(level, start);
        if (context.claimRootValueCache(component)) {
            return context.tickCache.componentValues.computeIfAbsent(
                    component,
                    (component2) -> evaluateNetworkSource(level, context, component2)
            );
        }
        return evaluateCableComponentUncached(level, context, start, component);
    }

    private static SignalValue evaluateNetworkSource(Level level, EvaluationContext context, PerLevelNetworkDiagramCache.CableNetwork network) {
        PerLevelNetworkDiagramCache.SourceEndpoint source = network.source();
        if (source == null) {
            return ScalarSignal.ZERO;
        }
        context.visited.addAll(network.visits());
        try {
            return valueAtCableAware(level, source.pos(), source.direction(), null, false, context);
        } finally {
            context.visited.removeAll(network.visits());
        }
    }

    private static SignalValue evaluateCableComponentUncached(Level level, EvaluationContext context, CableVisit start, PerLevelNetworkDiagramCache.CableNetwork component) {
        if (!context.visited.add(start)) {
            return ScalarSignal.ZERO;
        }
        try {
            for (CableEdge edge : component.edgesFrom(start)) {
                SignalValue value = switch (edge) {
                    case CableEdgeOfCable(CableVisit cable) ->
                            evaluateCableComponentUncached(level, context, cable, component);
                    case CableEdgeOfBlock(BlockPos block, Direction requiredOutputDirection) ->
                            valueAtCableAware(level, block, requiredOutputDirection, null, false, context);
                };

                if (hasSignal(value)) {
                    return value;
                }
            }
            return ScalarSignal.ZERO;
        } finally {
            context.visited.remove(start);
        }
    }

    private static TickCache tickCache(Level level) {
        long gameTime = level.getGameTime();
        TickCache cache = TICK_CACHES.get(level);
        if (cache == null || cache.gameTime != gameTime) {
            cache = new TickCache(gameTime);
            TICK_CACHES.put(level, cache);
        }
        return cache;
    }

    private static boolean hasSignal(SignalValue value) {
        return value.getType() == SignalType.BOOLEAN && value.asBoolean()
                || value.getType() == SignalType.SCALAR && value.asScalar() != 0.0;
    }

    private static boolean hasOutputToward(Level level, BlockPos pos, Direction direction) {
        for (PortSpec spec : LogicState.ports(level, pos)) {
            if (spec.output() && spec.direction() == direction) {
                return true;
            }
        }
        return false;
    }

    private static SignalValue evaluateBlock(Level level, BlockPos pos, LogicBlockKind kind, EvaluationContext context) {
        return switch (kind) {
            case SCALAR_CONSTANT -> new ScalarSignal(LogicState.scalarValue(level, pos));
            case SCALAR_COMPARATOR ->
                    BooleanSignal.of(LogicState.comparatorOperator(level, pos).test(input(level, pos, kind, "left", context).asScalar(), input(level, pos, kind, "right", context).asScalar()));
            case SCALAR_ADD ->
                    new ScalarSignal(input(level, pos, kind, "left", context).asScalar() + input(level, pos, kind, "right", context).asScalar());
            case BOOLEAN_AND ->
                    BooleanSignal.of(input(level, pos, kind, "left", context).asBoolean() && input(level, pos, kind, "right", context).asBoolean());
            case BOOLEAN_NOT -> BooleanSignal.of(!input(level, pos, kind, "value", context).asBoolean());
            case BOOLEAN_CONDITIONAL ->
                    new ScalarSignal(input(level, pos, kind, "condition", context).asBoolean() ? input(level, pos, kind, "ifTrue", context).asScalar() : input(level, pos, kind, "ifFalse", context).asScalar());
            case REDSTONE_IMPORTER -> new ScalarSignal(level.getBestNeighborSignal(pos));
            case ROM_POWERING_GATE -> evaluateRom(level, pos, kind, context);
            case REDSTONE_EXPORTER -> new ScalarSignal(input(level, pos, kind, "value", context).asScalar());
            case EXTERNAL_PORT -> LogicState.externalOutput(level, pos) ? input(level, pos, kind, "value", context) : ScalarSignal.ZERO;
            default -> ScalarSignal.ZERO;
        };
    }

    private static SignalValue evaluateRom(Level level, BlockPos pos, LogicBlockKind kind, EvaluationContext context) {
        RomProgram romProgram = LogicState.romProgram(level, pos);
        if (romProgram == null) {
            return ScalarSignal.ZERO;
        }
        Map<String, SignalValue> inputs = new HashMap<>();
        if (!romProgram.inputs().isEmpty()) {
            inputs.put(romProgram.inputs().get(0), input(level, pos, kind, "input0", context));
        }
        Map<String, SignalValue> values = CircuitEvaluator.evaluate(romProgram, inputs);
        if (romProgram.outputs().isEmpty()) {
            return ScalarSignal.ZERO;
        }
        return values.getOrDefault(romProgram.outputs().get(0), ScalarSignal.ZERO);
    }

    private static SignalValue input(Level level, BlockPos pos, LogicBlockKind kind, String name, EvaluationContext context) {
        for (PortSpec spec : LogicState.ports(level, pos)) {
            if (!spec.output() && spec.name().equals(name)) {
                return valueAtCableAware(level, pos.relative(spec.direction()), spec.direction().getOpposite(), null, true, context);
            }
        }
        if (kind == LogicBlockKind.BOOLEAN_AND || kind == LogicBlockKind.BOOLEAN_NOT || name.equals("condition")) {
            return BooleanSignal.FALSE;
        }
        return ScalarSignal.ZERO;
    }

    public record CableVisit(BlockPos pos, DyeColor color) {
    }

    public sealed interface CableEdge {
        CableEdgeKind kind();
    }

    public enum CableEdgeKind {
        CABLE,
        BLOCK,
    }

    public record CableEdgeOfBlock(BlockPos block, Direction requiredOutputDirection) implements CableEdge {
        @Override
        public CableEdgeKind kind() {
            return CableEdgeKind.BLOCK;
        }
    }

    public record CableEdgeOfCable(CableVisit cable) implements CableEdge {
        @Override
        public CableEdgeKind kind() {
            return CableEdgeKind.CABLE;
        }
    }

    private static final class EvaluationContext {
        private final TickCache tickCache;
        private boolean rootValueCacheAvailable;
        private final Set<CableVisit> visited = new HashSet<>();

        private EvaluationContext(TickCache tickCache, boolean rootValueCacheAvailable) {
            this.tickCache = tickCache;
            this.rootValueCacheAvailable = rootValueCacheAvailable;
        }

        private boolean claimRootValueCache(PerLevelNetworkDiagramCache.CableNetwork component) {
            if (!rootValueCacheAvailable || !visited.isEmpty() || component.driverCount() > 1) {
                return false;
            }
            rootValueCacheAvailable = false;
            return true;
        }

        private PerLevelNetworkDiagramCache.CableNetwork networkFor(Level level, CableVisit start) {
            return PerLevelNetworkDiagramCache.networkAt(level, start);
        }
    }

    private static final class TickCache {
        private final long gameTime;
        private final Map<PerLevelNetworkDiagramCache.CableNetwork, SignalValue> componentValues = new HashMap<>();

        private TickCache(long gameTime) {
            this.gameTime = gameTime;
        }

    }
}
