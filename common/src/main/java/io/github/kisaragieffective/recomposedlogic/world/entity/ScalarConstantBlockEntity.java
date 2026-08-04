package io.github.kisaragieffective.recomposedlogic.world.entity;

import io.github.kisaragieffective.recomposedlogic.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class ScalarConstantBlockEntity extends BlockEntity {
    private double scalarValue = 1.0;

    public ScalarConstantBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.SCALAR_CONSTANT, pos, blockState);
    }

    public double scalarValue() {
        return scalarValue;
    }

    public void scalarValue(double scalarValue) {
        this.scalarValue = scalarValue;
        setChanged();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putDouble("scalarValue", scalarValue);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        scalarValue = tag.contains("scalarValue") ? tag.getDouble("scalarValue") : 1.0;
    }
}
