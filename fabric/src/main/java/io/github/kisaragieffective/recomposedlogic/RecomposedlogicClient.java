package io.github.kisaragieffective.recomposedlogic;

import io.github.kisaragieffective.recomposedlogic.registry.ModBlockEntities;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;

public class RecomposedlogicClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientChunkEvents.CHUNK_LOAD.register((level, chunk) -> PinCableBlockEntityRenderer.invalidateChunk(level, chunk.getPos()));
        ClientChunkEvents.CHUNK_UNLOAD.register((level, chunk) -> PinCableBlockEntityRenderer.invalidateChunk(level, chunk.getPos()));
        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
            @Override
            public ResourceLocation getFabricId() {
                return ResourceLocation.fromNamespaceAndPath("recomposedlogic", "cable_meshes");
            }

            @Override
            public void onResourceManagerReload(ResourceManager manager) {
                PinCableBlockEntityRenderer.reloadMeshes();
            }
        });
        BlockEntityRenderers.register(ModBlockEntities.PIN_CABLE, PinCableBlockEntityRenderer::new);
        BlockEntityRenderers.register(ModBlockEntities.DYED_PIN_CABLE, PinCableBlockEntityRenderer::new);
        BlockEntityRenderers.register(ModBlockEntities.UNDYED_INTERNAL_JUNCTION, PinCableBlockEntityRenderer::new);
        BlockEntityRenderers.register(ModBlockEntities.DYED_INTERNAL_JUNCTION, PinCableBlockEntityRenderer::new);
    }
}
