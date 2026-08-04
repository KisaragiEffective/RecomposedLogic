package io.github.kisaragieffective.recomposedlogic.world.entity;

import io.github.kisaragieffective.recomposedlogic.logic.RomNbt;
import io.github.kisaragieffective.recomposedlogic.logic.RomProgram;
import io.github.kisaragieffective.recomposedlogic.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class RomPoweringGateBlockEntity extends BlockEntity {
    private RomProgram romProgram;

    public RomPoweringGateBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.ROM_POWERING_GATE, pos, blockState);
    }

    public RomProgram romProgram() {
        return romProgram;
    }

    public void romProgram(RomProgram romProgram) {
        this.romProgram = romProgram;
        setChanged();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (romProgram != null) {
            tag.put("romProgram", RomNbt.write(romProgram));
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        romProgram = tag.contains("romProgram", Tag.TAG_COMPOUND) ? RomNbt.read(tag.getCompound("romProgram")) : null;
    }
}
