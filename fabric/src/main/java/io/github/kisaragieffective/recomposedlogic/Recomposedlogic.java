package io.github.kisaragieffective.recomposedlogic;

import io.github.kisaragieffective.recomposedlogic.registry.ModBlockEntities;
import io.github.kisaragieffective.recomposedlogic.registry.ModBlocks;
import io.github.kisaragieffective.recomposedlogic.registry.ModItems;
import io.github.kisaragieffective.recomposedlogic.world.LogicBlockEntity;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.api.ModInitializer;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public class Recomposedlogic implements ModInitializer {

    @Override
    public void onInitialize() {

        // This method is invoked by the Fabric mod loader when it is ready
        // to load your mod. You can access Fabric and Common code in this
        // project.

        // Use Fabric to bootstrap the Common mod.
        Constants.LOG.info("Hello Fabric world!");
        CommonClass.init();
        ModBlockEntities.LOGIC = Registry.register(
                BuiltInRegistries.BLOCK_ENTITY_TYPE,
                ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "logic"),
                FabricBlockEntityTypeBuilder.create(LogicBlockEntity::new, ModBlocks.ALL.values().toArray(new net.minecraft.world.level.block.Block[0])).build()
        );
        Registry.register(
                BuiltInRegistries.CREATIVE_MODE_TAB,
                ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "main"),
                FabricItemGroup.builder()
                        .title(Component.literal(Constants.MOD_NAME))
                        .icon(() -> new ItemStack(ModItems.WRENCH))
                        .displayItems((parameters, output) -> ModItems.ALL.values().forEach(output::accept))
                        .build()
        );
    }
}
