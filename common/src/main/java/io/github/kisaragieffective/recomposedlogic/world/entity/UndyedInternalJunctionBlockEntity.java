package io.github.kisaragieffective.recomposedlogic.world.entity;

import io.github.kisaragieffective.recomposedlogic.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class UndyedInternalJunctionBlockEntity extends CableBlockEntity {
    public UndyedInternalJunctionBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.UNDYED_INTERNAL_JUNCTION, pos, blockState);
    }
}
