package io.github.kisaragieffective.recomposedlogic.world;

import io.github.kisaragieffective.recomposedlogic.logic.CircuitCompiler;
import io.github.kisaragieffective.recomposedlogic.logic.CircuitGraph;
import io.github.kisaragieffective.recomposedlogic.logic.CircuitNode;
import io.github.kisaragieffective.recomposedlogic.logic.CompileResult;
import io.github.kisaragieffective.recomposedlogic.logic.NodeKind;
import io.github.kisaragieffective.recomposedlogic.logic.PortRef;
import io.github.kisaragieffective.recomposedlogic.world.block.PinCableBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public final class WorldCircuitScanner {
    private WorldCircuitScanner() {
    }

    public static CompileResult scan(Level level, BlockPos originPos) {
        if (LogicState.kind(level, originPos) != LogicBlockKind.CIRCUIT_ORIGIN) {
            CircuitGraph graph = new CircuitGraph();
            graph.diagnostics().add("Paired origin is missing");
            return CircuitCompiler.compile(graph, 0);
        }

        BlockPos max = originPos.offset(LogicState.originMaxOffset(level, originPos));
        int minX = Math.min(originPos.getX(), max.getX());
        int minY = Math.min(originPos.getY(), max.getY());
        int minZ = Math.min(originPos.getZ(), max.getZ());
        int maxX = Math.max(originPos.getX(), max.getX());
        int maxY = Math.max(originPos.getY(), max.getY());
        int maxZ = Math.max(originPos.getZ(), max.getZ());
        int volume = (maxX - minX + 1) * (maxY - minY + 1) * (maxZ - minZ + 1);

        CircuitGraph graph = new CircuitGraph();
        Map<BlockPos, LogicBlockKind> entities = new HashMap<>();
        for (BlockPos pos : BlockPos.betweenClosed(minX, minY, minZ, maxX, maxY, maxZ)) {
            LogicBlockKind kind = LogicState.kind(level, pos);
            if (kind != null && kind.nodeKind() != null) {
                addNode(level, graph, entities, pos.immutable(), kind);
            }
        }
        for (BlockPos pos : BlockPos.betweenClosed(minX - 1, minY - 1, minZ - 1, maxX + 1, maxY + 1, maxZ + 1)) {
            if (inside(pos, minX, minY, minZ, maxX, maxY, maxZ)) {
                continue;
            }
            LogicBlockKind kind = LogicState.kind(level, pos);
            if (kind == LogicBlockKind.EXTERNAL_PORT) {
                addNode(level, graph, entities, pos.immutable(), kind);
            }
        }
        if (entities.isEmpty()) {
            graph.diagnostics().add("No circuit nodes in origin volume " + minX + "," + minY + "," + minZ + " to " + maxX + "," + maxY + "," + maxZ);
            return CircuitCompiler.compile(graph, volume);
        }

        for (Map.Entry<BlockPos, LogicBlockKind> entry : entities.entrySet()) {
            CircuitNode node = graph.node(id(entry.getKey()));
            for (PortSpec spec : LogicState.ports(level, entry.getKey())) {
                if (!spec.output()) {
                    List<PortRef> sources = findSources(level, entry.getKey().relative(spec.direction()), entry.getKey(), entities);
                    if (sources.size() == 1) {
                        node.connect(spec.name(), sources.getFirst());
                    } else if (sources.size() > 1) {
                        graph.diagnostics().add("Multiple drivers for " + node.id() + "." + spec.name() + ": " + sources);
                    }
                }
            }
        }
        return CircuitCompiler.compile(graph, volume);
    }

    private static void addNode(Level level, CircuitGraph graph, Map<BlockPos, LogicBlockKind> entities, BlockPos pos, LogicBlockKind kind) {
        entities.put(pos, kind);
        CircuitNode node = new CircuitNode(id(pos), nodeKind(level, pos, kind));
        node.scalarValue(LogicState.scalarValue(level, pos));
        node.comparatorOperator(LogicState.comparatorOperator(level, pos));
        node.romProgram(LogicState.romProgram(level, pos));
        graph.add(node);
    }

    private static boolean inside(BlockPos pos, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        return pos.getX() >= minX && pos.getX() <= maxX
                && pos.getY() >= minY && pos.getY() <= maxY
                && pos.getZ() >= minZ && pos.getZ() <= maxZ;
    }

    private static NodeKind nodeKind(Level level, BlockPos pos, LogicBlockKind kind) {
        if (kind == LogicBlockKind.EXTERNAL_PORT) {
            return LogicState.externalOutput(level, pos) ? NodeKind.EXTERNAL_OUTPUT : NodeKind.EXTERNAL_INPUT;
        }
        return kind.nodeKind();
    }

    private static List<PortRef> findSources(Level level, BlockPos start, BlockPos consumerPos, Map<BlockPos, LogicBlockKind> entities) {
        List<PortRef> sources = new ArrayList<>();
        Set<CableVisit> visited = new HashSet<>();
        Queue<SearchStep> queue = new ArrayDeque<>();
        LogicBlockKind startKind = LogicState.kind(level, start);
        if (startKind == LogicBlockKind.PIN_CABLE || startKind == LogicBlockKind.JUNCTION) {
            PinCableBlock.TraversalConnection startConnection = PinCableBlock.portTraversalStart(level, start);
            if (!startConnection.connected()) {
                return sources;
            }
            queue.add(new SearchStep(start, consumerPos, startConnection.color()));
        } else {
            queue.add(new SearchStep(start, consumerPos, null));
        }
        while (!queue.isEmpty()) {
            SearchStep step = queue.remove();
            BlockPos pos = step.pos();
            LogicBlockKind kind = LogicState.kind(level, pos);
            if (kind != null) {
                if (kind == LogicBlockKind.PIN_CABLE || kind == LogicBlockKind.JUNCTION) {
                    if (!visited.add(new CableVisit(pos, step.color()))) {
                        continue;
                    }
                    for (Direction direction : Direction.values()) {
                        BlockPos next = pos.relative(direction);
                        LogicBlockKind nextKind = LogicState.kind(level, next);
                        DyeColor nextColor = step.color();
                        if (nextKind == LogicBlockKind.PIN_CABLE || nextKind == LogicBlockKind.JUNCTION) {
                            PinCableBlock.TraversalConnection connection = PinCableBlock.traversalConnection(level, pos, next, step.color());
                            if (!connection.connected()) {
                                continue;
                            }
                            nextColor = connection.color();
                        } else {
                            PinCableBlock.TraversalConnection connection = PinCableBlock.portTraversalStart(level, pos);
                            if (!connection.connected() || (step.color() != null && connection.color() != null && step.color() != connection.color())) {
                                continue;
                            }
                        }
                        queue.add(new SearchStep(next, pos, nextColor));
                    }
                    continue;
                }
                if (entities.containsKey(pos)) {
                    Direction outputDirection = directionTo(pos, step.previous());
                    for (PortSpec port : LogicState.ports(level, pos)) {
                        if (port.output() && port.direction() == outputDirection) {
                            PortRef source = new PortRef(id(pos), port.name());
                            if (!sources.contains(source)) {
                                sources.add(source);
                            }
                        }
                    }
                }
            }
        }
        return sources;
    }

    private static Direction directionTo(BlockPos from, BlockPos to) {
        for (Direction direction : Direction.values()) {
            if (from.relative(direction).equals(to)) {
                return direction;
            }
        }
        return null;
    }

    public static String id(BlockPos pos) {
        return pos.getX() + "," + pos.getY() + "," + pos.getZ();
    }

    private record SearchStep(BlockPos pos, BlockPos previous, DyeColor color) {
    }

    private record CableVisit(BlockPos pos, DyeColor color) {
    }
}
