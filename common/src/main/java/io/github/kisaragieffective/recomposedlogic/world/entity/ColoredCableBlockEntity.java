package io.github.kisaragieffective.recomposedlogic.world.entity;

import io.github.kisaragieffective.recomposedlogic.world.ColorSet16;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public abstract class ColoredCableBlockEntity extends CableBlockEntity {
    private ColorSet16 colors = ColorSet16.EMPTY;

    protected ColoredCableBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    public ColorSet16 colors() {
        return colors;
    }

    public boolean addColor(DyeColor color) {
        if (colors.contains(color)) {
            return false;
        }
        colors = colors.with(color);
        syncChanged();
        return true;
    }

    public void colors(ColorSet16 colors) {
        this.colors = colors;
        syncChanged();
    }

    public void singleColor(DyeColor color) {
        colors(ColorSet16.of(color));
    }

    public boolean singleColor() {
        return colors.isSingle();
    }

    public boolean multipleColors() {
        return colors.size() > 1;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("colors", colors.mask());
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        colors = new ColorSet16(tag.getInt("colors"));
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
