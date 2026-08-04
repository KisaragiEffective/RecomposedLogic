package io.github.kisaragieffective.recomposedlogic.world.entity;

import io.github.kisaragieffective.recomposedlogic.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class RomProgrammerBlockEntity extends BlockEntity {
    private ItemStack slotA = ItemStack.EMPTY;
    private ItemStack slotB = ItemStack.EMPTY;
    private String status = "Idle";

    public RomProgrammerBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.ROM_PROGRAMMER, pos, blockState);
    }

    public ItemStack slotA() {
        return slotA;
    }

    public void slotA(ItemStack slotA) {
        this.slotA = slotA;
        setChanged();
    }

    public ItemStack slotB() {
        return slotB;
    }

    public void slotB(ItemStack slotB) {
        this.slotB = slotB;
        setChanged();
    }

    public void status(String status) {
        this.status = status;
        setChanged();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (!slotA.isEmpty()) {
            tag.put("slotA", slotA.save(registries));
        }
        if (!slotB.isEmpty()) {
            tag.put("slotB", slotB.save(registries));
        }
        if (!"Idle".equals(status)) {
            tag.putString("status", status);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        slotA = tag.contains("slotA", Tag.TAG_COMPOUND) ? ItemStack.parseOptional(registries, tag.getCompound("slotA")) : ItemStack.EMPTY;
        slotB = tag.contains("slotB", Tag.TAG_COMPOUND) ? ItemStack.parseOptional(registries, tag.getCompound("slotB")) : ItemStack.EMPTY;
        status = tag.contains("status") ? tag.getString("status") : "Idle";
    }
}
