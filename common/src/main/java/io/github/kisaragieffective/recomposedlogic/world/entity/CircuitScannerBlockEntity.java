package io.github.kisaragieffective.recomposedlogic.world.entity;

import io.github.kisaragieffective.recomposedlogic.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class CircuitScannerBlockEntity extends BlockEntity {
    private BlockPos pairedOrigin;
    private String status = "Idle";

    public CircuitScannerBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.CIRCUIT_SCANNER, pos, blockState);
    }

    public BlockPos pairedOrigin() {
        return pairedOrigin;
    }

    public void pairedOrigin(BlockPos pairedOrigin) {
        this.pairedOrigin = pairedOrigin;
        setChanged();
    }

    public String status() {
        return status;
    }

    public void status(String status) {
        this.status = status;
        setChanged();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (pairedOrigin != null) {
            tag.putLong("pairedOrigin", pairedOrigin.asLong());
        }
        if (!"Idle".equals(status)) {
            tag.putString("status", status);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        pairedOrigin = tag.contains("pairedOrigin") ? BlockPos.of(tag.getLong("pairedOrigin")) : null;
        status = tag.contains("status") ? tag.getString("status") : "Idle";
    }
}
