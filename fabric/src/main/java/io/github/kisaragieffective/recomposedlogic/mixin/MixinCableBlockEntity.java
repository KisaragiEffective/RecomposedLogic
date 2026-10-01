package io.github.kisaragieffective.recomposedlogic.mixin;

import io.github.kisaragieffective.recomposedlogic.PinCableBlockEntityRenderer;
import io.github.kisaragieffective.recomposedlogic.world.entity.CableBlockEntity;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockEntity.class)
public class MixinCableBlockEntity {
    @Inject(method = {"loadWithComponents", "loadCustomOnly"}, at = @At("TAIL"))
    private void invalidateSyncedCableMesh(CompoundTag tag, HolderLookup.Provider registries, CallbackInfo callback) {
        if ((Object) this instanceof CableBlockEntity cable) {
            PinCableBlockEntityRenderer.invalidateAround(cable.getLevel(), cable.getBlockPos());
        }
    }
}
