package io.github.kisaragieffective.recomposedlogic.mixin;

import io.github.kisaragieffective.recomposedlogic.PinCableBlockEntityRenderer;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientLevel.class)
public class MixinClientLevel {
    @Inject(method = "setBlock", at = @At("RETURN"))
    private void invalidateCableMesh(BlockPos pos, BlockState state, int flags, int recursionLeft, CallbackInfoReturnable<Boolean> callback) {
        if (callback.getReturnValue()) {
            PinCableBlockEntityRenderer.invalidateAround((Level) (Object) this, pos);
        }
    }

    @Inject(method = "setServerVerifiedBlockState", at = @At("TAIL"))
    private void invalidateServerUpdatedCableMesh(BlockPos pos, BlockState state, int flags, CallbackInfo callback) {
        // This path calls super.setBlock and bypasses the ClientLevel override.
        PinCableBlockEntityRenderer.invalidateAround((Level) (Object) this, pos);
    }
}
