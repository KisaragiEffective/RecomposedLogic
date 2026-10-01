package io.github.kisaragieffective.recomposedlogic;

import io.github.kisaragieffective.recomposedlogic.world.PerLevelNetworkDiagramCache;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.fml.common.Mod;

@Mod(Constants.MOD_ID)
public class Recomposedlogic {

    public Recomposedlogic() {
        // This method is invoked by the Forge mod loader when it is ready
        // to load your mod. You can access Forge and Common code in this
        // project.

        // Use Forge to bootstrap the Common mod.
        Constants.LOG.info("Hello Forge world!");
        CommonClass.init();
        MinecraftForge.EVENT_BUS.addListener((ChunkEvent.Unload event) -> {
            if (event.getLevel() instanceof Level level && !level.isClientSide) {
                PerLevelNetworkDiagramCache.unloadChunk(level, event.getChunk().getPos());
            }
        });
        MinecraftForge.EVENT_BUS.addListener((LevelEvent.Unload event) -> {
            if (event.getLevel() instanceof Level level && !level.isClientSide) {
                PerLevelNetworkDiagramCache.unloadLevel(level);
            }
        });

    }
}
