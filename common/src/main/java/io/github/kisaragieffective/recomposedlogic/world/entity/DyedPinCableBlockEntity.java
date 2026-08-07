package io.github.kisaragieffective.recomposedlogic.world.entity;

import io.github.kisaragieffective.recomposedlogic.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class DyedPinCableBlockEntity extends ColoredCableBlockEntity {
    public DyedPinCableBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.DYED_PIN_CABLE, pos, blockState);
    }
}
