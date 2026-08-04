package io.github.kisaragieffective.recomposedlogic.world.entity;

import io.github.kisaragieffective.recomposedlogic.logic.ComparatorOperator;
import io.github.kisaragieffective.recomposedlogic.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class ScalarComparatorBlockEntity extends BlockEntity {
    private ComparatorOperator operator = ComparatorOperator.GREATER_OR_EQUAL;

    public ScalarComparatorBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.SCALAR_COMPARATOR, pos, blockState);
    }

    public ComparatorOperator operator() {
        return operator;
    }

    public void operator(ComparatorOperator operator) {
        this.operator = operator;
        setChanged();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putString("operator", operator.name());
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("operator")) {
            operator = ComparatorOperator.valueOf(tag.getString("operator"));
        }
    }
}
