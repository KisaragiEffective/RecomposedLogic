package io.github.kisaragieffective.recomposedlogic.world;

import net.minecraft.core.Direction;

import java.util.List;

public final class Ports {
    private Ports() {
    }

    public static List<PortSpec> allOutputs(String name) {
        return List.of(
                new PortSpec(name, Direction.DOWN, true),
                new PortSpec(name, Direction.UP, true),
                new PortSpec(name, Direction.NORTH, true),
                new PortSpec(name, Direction.SOUTH, true),
                new PortSpec(name, Direction.WEST, true),
                new PortSpec(name, Direction.EAST, true)
        );
    }
}
