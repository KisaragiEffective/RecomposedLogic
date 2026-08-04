package io.github.kisaragieffective.recomposedlogic.world.entity;

import io.github.kisaragieffective.recomposedlogic.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class RedstoneExporterBlockEntity extends BlockEntity {
    private int redstonePower;

    public RedstoneExporterBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.REDSTONE_EXPORTER, pos, blockState);
    }

    public int redstonePower() {
        return redstonePower;
    }

    public void redstonePower(int redstonePower) {
        this.redstonePower = Math.max(0, Math.min(15, redstonePower));
        setChanged();
    }
}
