package io.github.kisaragieffective.recomposedlogic.world.entity;

import io.github.kisaragieffective.recomposedlogic.registry.ModBlockEntities;
import io.github.kisaragieffective.recomposedlogic.world.PerLevelNetworkDiagramCache;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class ExternalPortBlockEntity extends BlockEntity {
    private boolean externalOutput;

    public ExternalPortBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.EXTERNAL_PORT, pos, blockState);
    }

    public boolean externalOutput() {
        return externalOutput;
    }

    public void toggleExternalDirection() {
        externalOutput = !externalOutput;
        if (level != null && !level.isClientSide) {
            PerLevelNetworkDiagramCache.invalidateAround(level, worldPosition);
        }
        setChanged();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putBoolean("externalOutput", externalOutput);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        externalOutput = tag.getBoolean("externalOutput");
    }
}
