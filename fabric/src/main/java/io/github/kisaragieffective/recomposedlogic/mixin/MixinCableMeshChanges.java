package io.github.kisaragieffective.recomposedlogic.mixin;

import io.github.kisaragieffective.recomposedlogic.PinCableBlockEntityRenderer;
import io.github.kisaragieffective.recomposedlogic.world.entity.CableBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = CableBlockEntity.class, remap = false)
public class MixinCableMeshChanges {
    @Inject(method = "syncChanged", at = @At("TAIL"))
    private void invalidateLocalCableMesh(CallbackInfo callback) {
        CableBlockEntity cable = (CableBlockEntity) (Object) this;
        PinCableBlockEntityRenderer.invalidateAround(cable.getLevel(), cable.getBlockPos());
    }
}
