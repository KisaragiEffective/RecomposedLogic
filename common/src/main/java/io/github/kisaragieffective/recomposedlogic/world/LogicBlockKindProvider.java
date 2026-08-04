package io.github.kisaragieffective.recomposedlogic.world;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.util.List;

public interface LogicBlockKindProvider {
    LogicBlockKind kind();

    default List<PortSpec> ports() {
        return List.of();
    }

    default List<PortSpec> ports(Level level, BlockPos pos) {
        return ports();
    }
}
