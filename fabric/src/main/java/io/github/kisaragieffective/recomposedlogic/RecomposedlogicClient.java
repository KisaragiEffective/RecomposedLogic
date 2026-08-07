package io.github.kisaragieffective.recomposedlogic;

import io.github.kisaragieffective.recomposedlogic.registry.ModBlockEntities;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;

public class RecomposedlogicClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        BlockEntityRenderers.register(ModBlockEntities.PIN_CABLE, PinCableBlockEntityRenderer::new);
        BlockEntityRenderers.register(ModBlockEntities.DYED_PIN_CABLE, PinCableBlockEntityRenderer::new);
        BlockEntityRenderers.register(ModBlockEntities.UNDYED_INTERNAL_JUNCTION, PinCableBlockEntityRenderer::new);
        BlockEntityRenderers.register(ModBlockEntities.DYED_INTERNAL_JUNCTION, PinCableBlockEntityRenderer::new);
    }
}
