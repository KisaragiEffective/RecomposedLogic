package io.github.kisaragieffective.recomposedlogic.world;

import io.github.kisaragieffective.recomposedlogic.logic.CircuitCompiler;
import io.github.kisaragieffective.recomposedlogic.logic.CircuitGraph;
import io.github.kisaragieffective.recomposedlogic.logic.CircuitNode;
import io.github.kisaragieffective.recomposedlogic.logic.CompileResult;
import io.github.kisaragieffective.recomposedlogic.logic.NodeKind;
import io.github.kisaragieffective.recomposedlogic.logic.PortRef;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public final class WorldCircuitScanner {
    private WorldCircuitScanner() {
    }

    public static CompileResult scan(Level level, BlockPos originPos) {
        BlockEntity originBlockEntity = level.getBlockEntity(originPos);
        if (!(originBlockEntity instanceof LogicBlockEntity origin) || origin.kind() != LogicBlockKind.CIRCUIT_ORIGIN) {
            CircuitGraph graph = new CircuitGraph();
            graph.diagnostics().add("Paired origin is missing");
            return CircuitCompiler.compile(graph, 0);
        }

        BlockPos max = originPos.offset(origin.originMaxOffset());
        int minX = Math.min(originPos.getX(), max.getX());
        int minY = Math.min(originPos.getY(), max.getY());
        int minZ = Math.min(originPos.getZ(), max.getZ());
        int maxX = Math.max(originPos.getX(), max.getX());
        int maxY = Math.max(originPos.getY(), max.getY());
        int maxZ = Math.max(originPos.getZ(), max.getZ());
        int volume = (maxX - minX + 1) * (maxY - minY + 1) * (maxZ - minZ + 1);

        CircuitGraph graph = new CircuitGraph();
        Map<BlockPos, LogicBlockEntity> entities = new HashMap<>();
        for (BlockPos pos : BlockPos.betweenClosed(minX, minY, minZ, maxX, maxY, maxZ)) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof LogicBlockEntity logic && logic.kind().nodeKind() != null) {
                entities.put(pos.immutable(), logic);
                CircuitNode node = new CircuitNode(id(pos), nodeKind(logic));
                node.scalarValue(logic.scalarValue());
                node.comparatorOperator(logic.comparatorOperator());
                node.romProgram(logic.romProgram());
                graph.add(node);
            }
        }

        for (Map.Entry<BlockPos, LogicBlockEntity> entry : entities.entrySet()) {
            CircuitNode node = graph.node(id(entry.getKey()));
            for (PortSpec spec : Ports.specs(entry.getValue())) {
                if (!spec.output()) {
                    PortRef source = findSource(level, entry.getKey().relative(spec.direction()), entry.getKey(), entities);
                    if (source != null) {
                        node.connect(spec.name(), source);
                    }
                }
            }
        }
        return CircuitCompiler.compile(graph, volume);
    }

    private static NodeKind nodeKind(LogicBlockEntity entity) {
        if (entity.kind() == LogicBlockKind.EXTERNAL_PORT) {
            return entity.externalOutput() ? NodeKind.EXTERNAL_OUTPUT : NodeKind.EXTERNAL_INPUT;
        }
        return entity.kind().nodeKind();
    }

    private static PortRef findSource(Level level, BlockPos start, BlockPos consumerPos, Map<BlockPos, LogicBlockEntity> entities) {
        Set<BlockPos> visited = new HashSet<>();
        Queue<BlockPos> queue = new ArrayDeque<>();
        queue.add(start);
        while (!queue.isEmpty()) {
            BlockPos pos = queue.remove();
            if (!visited.add(pos)) {
                continue;
            }
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof LogicBlockEntity logic) {
                if (logic.kind() == LogicBlockKind.PIN_CABLE || logic.kind() == LogicBlockKind.JUNCTION) {
                    for (Direction direction : Direction.values()) {
                        queue.add(pos.relative(direction));
                    }
                    continue;
                }
                if (entities.containsKey(pos)) {
                    for (PortSpec port : Ports.specs(logic)) {
                        if (port.output() && pos.relative(port.direction()).equals(start) || port.output() && pos.relative(port.direction()).equals(consumerPos) || port.output()) {
                            return new PortRef(id(pos), port.name());
                        }
                    }
                }
            }
        }
        return null;
    }

    public static String id(BlockPos pos) {
        return pos.getX() + "," + pos.getY() + "," + pos.getZ();
    }
}
