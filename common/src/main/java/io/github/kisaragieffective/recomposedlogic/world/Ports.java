package io.github.kisaragieffective.recomposedlogic.world;

import net.minecraft.core.Direction;

import java.util.List;

public final class Ports {
    private Ports() {
    }

    public static List<PortSpec> specs(LogicBlockEntity entity) {
        return switch (entity.kind()) {
            case SCALAR_CONSTANT -> List.of(new PortSpec("value", Direction.EAST, true));
            case SCALAR_ADD -> List.of(new PortSpec("left", Direction.WEST, false), new PortSpec("right", Direction.NORTH, false), new PortSpec("value", Direction.EAST, true));
            case SCALAR_COMPARATOR -> List.of(new PortSpec("left", Direction.WEST, false), new PortSpec("right", Direction.NORTH, false), new PortSpec("value", Direction.EAST, true));
            case BOOLEAN_AND -> List.of(new PortSpec("left", Direction.WEST, false), new PortSpec("right", Direction.NORTH, false), new PortSpec("value", Direction.EAST, true));
            case BOOLEAN_NOT -> List.of(new PortSpec("value", Direction.WEST, false), new PortSpec("value", Direction.EAST, true));
            case BOOLEAN_CONDITIONAL -> List.of(new PortSpec("condition", Direction.WEST, false), new PortSpec("ifTrue", Direction.NORTH, false), new PortSpec("ifFalse", Direction.SOUTH, false), new PortSpec("value", Direction.EAST, true));
            case REDSTONE_IMPORTER -> List.of(new PortSpec("value", Direction.EAST, true));
            case REDSTONE_EXPORTER -> List.of(new PortSpec("value", Direction.WEST, false));
            case EXTERNAL_PORT -> List.of(new PortSpec("value", entity.externalOutput() ? Direction.WEST : Direction.EAST, !entity.externalOutput()));
            case ROM_POWERING_GATE -> List.of(new PortSpec("input0", Direction.WEST, false), new PortSpec("value", Direction.EAST, true));
            default -> List.of();
        };
    }
}
