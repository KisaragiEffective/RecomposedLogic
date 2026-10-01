package io.github.kisaragieffective.recomposedlogic.world;

import io.github.kisaragieffective.recomposedlogic.world.WorldRuntimeEvaluator.CableEdge;
import io.github.kisaragieffective.recomposedlogic.world.WorldRuntimeEvaluator.CableEdgeOfBlock;
import io.github.kisaragieffective.recomposedlogic.world.WorldRuntimeEvaluator.CableEdgeOfCable;
import io.github.kisaragieffective.recomposedlogic.world.WorldRuntimeEvaluator.CableVisit;
import io.github.kisaragieffective.recomposedlogic.world.block.PinCableBlock;
import io.github.kisaragieffective.recomposedlogic.world.entity.ColoredCableBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

public final class PerLevelNetworkDiagramCache {
    private static final Map<Level, Registry> REGISTRIES = new WeakHashMap<>();

    private PerLevelNetworkDiagramCache() {
    }

    public static CableNetwork networkAt(Level level, CableVisit start) {
        return registry(level).networkAt(level, start);
    }

    public static void invalidateAround(Level level, BlockPos pos) {
        Registry registry = REGISTRIES.get(level);
        if (registry != null) {
            registry.checkAround(level, pos);
        }
    }

    public static void beginTopologyUpdate(Level level, BlockPos pos) {
        Registry registry = REGISTRIES.get(level);
        if (registry != null) {
            registry.beginTopologyUpdate(pos);
        }
    }

    public static void endTopologyUpdate(Level level) {
        Registry registry = REGISTRIES.get(level);
        if (registry != null) {
            registry.endTopologyUpdate(level);
        }
    }

    public static void unloadChunk(Level level, ChunkPos chunkPos) {
        Registry registry = REGISTRIES.get(level);
        if (registry != null) {
            registry.invalidateChunk(chunkPos);
        }
    }

    public static void unloadLevel(Level level) {
        REGISTRIES.remove(level);
    }

    private static Registry registry(Level level) {
        return REGISTRIES.computeIfAbsent(level, ignored -> new Registry());
    }

    public static final class CableNetwork {
        private final Map<CableVisit, List<CableEdge>> edges = new HashMap<>();
        private final Set<BlockPos> dependencies = new HashSet<>();
        private int driverCount;
        private SourceEndpoint source;

        public List<CableEdge> edgesFrom(CableVisit visit) {
            return edges.getOrDefault(visit, List.of());
        }

        public int driverCount() {
            return driverCount;
        }

        public SourceEndpoint source() {
            return driverCount == 1 ? source : null;
        }

        public Set<CableVisit> visits() {
            return edges.keySet();
        }
    }

    private static final class Registry {
        private final Map<CableVisit, CableNetwork> networksByVisit = new HashMap<>();
        private final Map<Long, Set<CableNetwork>> networksByPosition = new HashMap<>();
        private final Map<Long, Set<CableNetwork>> networksByChunk = new HashMap<>();
        private final Set<BlockPos> pendingTopologyChecks = new HashSet<>();
        private final Map<Long, PositionSignature> signaturesByPosition = new HashMap<>();
        private int topologyUpdateDepth;

        private CableNetwork networkAt(Level level, CableVisit start) {
            CableNetwork cached = networksByVisit.get(start);
            if (cached != null) {
                return cached;
            }
            CableNetwork network = build(level, start);
            register(level, network);
            return network;
        }

        private CableNetwork build(Level level, CableVisit start) {
            CableNetwork network = new CableNetwork();
            Deque<CableVisit> pending = new ArrayDeque<>();
            network.edges.put(start, new ArrayList<>());
            pending.add(start);
            while (!pending.isEmpty()) {
                CableVisit current = pending.removeFirst();
                network.dependencies.add(current.pos());
                List<CableEdge> edges = network.edges.get(current);
                for (Direction direction : Direction.values()) {
                    BlockPos next = current.pos().relative(direction);
                    network.dependencies.add(next);
                    LogicBlockKind nextKind = LogicState.kind(level, next);
                    if (nextKind == LogicBlockKind.PIN_CABLE || nextKind == LogicBlockKind.JUNCTION) {
                        PinCableBlock.TraversalConnection connection = PinCableBlock.traversalConnection(level, current.pos(), next, current.color());
                        if (!connection.connected()) {
                            continue;
                        }
                        CableVisit nextVisit = new CableVisit(next, connection.color());
                        edges.add(new CableEdgeOfCable(nextVisit));
                        if (!network.edges.containsKey(nextVisit)) {
                            network.edges.put(nextVisit, new ArrayList<>());
                            pending.addLast(nextVisit);
                        }
                    } else {
                        PinCableBlock.TraversalConnection connection = PinCableBlock.portTraversalStart(level, current.pos());
                        if (connection.connected() && (current.color() == null || connection.color() == null || current.color() == connection.color())) {
                            edges.add(new CableEdgeOfBlock(next, direction.getOpposite()));
                            if (hasOutputToward(level, next, direction.getOpposite())) {
                                network.driverCount++;
                                network.source = new SourceEndpoint(next, direction.getOpposite());
                            }
                        }
                    }
                }
            }
            return network;
        }

        private void register(Level level, CableNetwork network) {
            for (CableVisit visit : network.edges.keySet()) {
                networksByVisit.put(visit, network);
            }
            for (BlockPos dependency : network.dependencies) {
                signaturesByPosition.computeIfAbsent(dependency.asLong(), ignored -> positionSignature(level, dependency));
                networksByPosition.computeIfAbsent(dependency.asLong(), ignored -> new HashSet<>()).add(network);
                networksByChunk.computeIfAbsent(new ChunkPos(dependency).toLong(), ignored -> new HashSet<>()).add(network);
            }
        }

        private void beginTopologyUpdate(BlockPos pos) {
            collectPositionsAround(pendingTopologyChecks, pos);
            topologyUpdateDepth++;
        }

        private void endTopologyUpdate(Level level) {
            if (--topologyUpdateDepth == 0) {
                checkPositions(level, pendingTopologyChecks);
                pendingTopologyChecks.clear();
            }
        }

        private void checkAround(Level level, BlockPos pos) {
            if (topologyUpdateDepth > 0) {
                collectPositionsAround(pendingTopologyChecks, pos);
                return;
            }
            Set<BlockPos> positions = new HashSet<>();
            collectPositionsAround(positions, pos);
            checkPositions(level, positions);
        }

        private static void collectPositionsAround(Set<BlockPos> positions, BlockPos pos) {
            positions.add(pos.immutable());
            for (Direction direction : Direction.values()) {
                positions.add(pos.relative(direction));
            }
        }

        private void checkPositions(Level level, Set<BlockPos> positions) {
            Set<CableNetwork> affected = new HashSet<>();
            for (BlockPos pos : positions) {
                PositionSignature previous = signaturesByPosition.get(pos.asLong());
                if (previous != null && !previous.equals(positionSignature(level, pos))) {
                    collectAt(affected, pos);
                }
            }
            affected.forEach(this::remove);
        }

        // Only inspect positions touched by an update, never the entire cached component.
        private static PositionSignature positionSignature(Level level, BlockPos pos) {
            LogicBlockKind kind = LogicState.kind(level, pos);
            boolean cable = kind == LogicBlockKind.PIN_CABLE || kind == LogicBlockKind.JUNCTION;
            int colorMask = cable && level.getBlockEntity(pos) instanceof ColoredCableBlockEntity colored
                    ? colored.colors().mask() : -1;
            return new PositionSignature(cable, colorMask, cable ? List.of() : List.copyOf(LogicState.ports(level, pos)));
        }

        private void invalidateChunk(ChunkPos chunkPos) {
            Set<CableNetwork> affected = networksByChunk.get(chunkPos.toLong());
            if (affected != null) {
                new HashSet<>(affected).forEach(this::remove);
            }
        }

        private void collectAt(Set<CableNetwork> affected, BlockPos pos) {
            Set<CableNetwork> networks = networksByPosition.get(pos.asLong());
            if (networks != null) {
                affected.addAll(networks);
            }
        }

        private void remove(CableNetwork network) {
            for (CableVisit visit : network.edges.keySet()) {
                networksByVisit.remove(visit, network);
            }
            for (BlockPos dependency : network.dependencies) {
                removeIndex(networksByPosition, dependency.asLong(), network);
                if (!networksByPosition.containsKey(dependency.asLong())) {
                    signaturesByPosition.remove(dependency.asLong());
                }
                removeIndex(networksByChunk, new ChunkPos(dependency).toLong(), network);
            }
        }

        private static void removeIndex(Map<Long, Set<CableNetwork>> index, long key, CableNetwork network) {
            Set<CableNetwork> networks = index.get(key);
            if (networks != null) {
                networks.remove(network);
                if (networks.isEmpty()) {
                    index.remove(key);
                }
            }
        }
    }

    public record SourceEndpoint(BlockPos pos, Direction direction) {
    }

    private record PositionSignature(boolean cable, int colorMask, List<PortSpec> ports) {
    }

    private static boolean hasOutputToward(Level level, BlockPos pos, Direction direction) {
        for (PortSpec spec : LogicState.ports(level, pos)) {
            if (spec.output() && spec.direction() == direction) {
                return true;
            }
        }
        return false;
    }
}
