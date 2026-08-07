package io.github.kisaragieffective.recomposedlogic.world.entity;

import io.github.kisaragieffective.recomposedlogic.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class PinCableBlockEntity extends CableBlockEntity {
    public PinCableBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.PIN_CABLE, pos, blockState);
    }
}
