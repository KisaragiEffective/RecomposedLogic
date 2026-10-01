package io.github.kisaragieffective.recomposedlogic;


import io.github.kisaragieffective.recomposedlogic.world.PerLevelNetworkDiagramCache;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.fml.common.Mod;

@Mod(Constants.MOD_ID)
public class Recomposedlogic {

    public Recomposedlogic(IEventBus eventBus) {
        // This method is invoked by the NeoForge mod loader when it is ready
        // to load your mod. You can access NeoForge and Common code in this
        // project.

        // Use NeoForge to bootstrap the Common mod.
        Constants.LOG.info("Hello NeoForge world!");
        CommonClass.init();
        NeoForge.EVENT_BUS.addListener((ChunkEvent.Unload event) -> {
            if (event.getLevel() instanceof Level level && !level.isClientSide) {
                PerLevelNetworkDiagramCache.unloadChunk(level, event.getChunk().getPos());
            }
        });
        NeoForge.EVENT_BUS.addListener((LevelEvent.Unload event) -> {
            if (event.getLevel() instanceof Level level && !level.isClientSide) {
                PerLevelNetworkDiagramCache.unloadLevel(level);
            }
        });
    }
}
