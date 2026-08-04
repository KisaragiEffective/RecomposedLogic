package io.github.kisaragieffective.recomposedlogic.world.entity;

import io.github.kisaragieffective.recomposedlogic.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class CircuitOriginBlockEntity extends BlockEntity {
    private BlockPos originMaxOffset = new BlockPos(2, 2, 2);

    public CircuitOriginBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.CIRCUIT_ORIGIN, pos, blockState);
    }

    public BlockPos originMaxOffset() {
        return originMaxOffset;
    }

    public void growOrigin() {
        originMaxOffset = originMaxOffset.offset(1, 1, 1);
        setChanged();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putLong("originMaxOffset", originMaxOffset.asLong());
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        originMaxOffset = tag.contains("originMaxOffset") ? BlockPos.of(tag.getLong("originMaxOffset")) : new BlockPos(2, 2, 2);
    }
}
