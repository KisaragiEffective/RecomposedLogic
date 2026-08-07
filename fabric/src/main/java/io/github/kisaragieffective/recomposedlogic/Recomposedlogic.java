package io.github.kisaragieffective.recomposedlogic;

import io.github.kisaragieffective.recomposedlogic.registry.ModBlockEntities;
import io.github.kisaragieffective.recomposedlogic.registry.ModBlocks;
import io.github.kisaragieffective.recomposedlogic.registry.ModItems;
import io.github.kisaragieffective.recomposedlogic.world.entity.CircuitOriginBlockEntity;
import io.github.kisaragieffective.recomposedlogic.world.entity.CircuitScannerBlockEntity;
import io.github.kisaragieffective.recomposedlogic.world.entity.DyedInternalJunctionBlockEntity;
import io.github.kisaragieffective.recomposedlogic.world.entity.DyedPinCableBlockEntity;
import io.github.kisaragieffective.recomposedlogic.world.entity.ExternalPortBlockEntity;
import io.github.kisaragieffective.recomposedlogic.world.entity.PinCableBlockEntity;
import io.github.kisaragieffective.recomposedlogic.world.entity.RedstoneExporterBlockEntity;
import io.github.kisaragieffective.recomposedlogic.world.entity.RomPoweringGateBlockEntity;
import io.github.kisaragieffective.recomposedlogic.world.entity.RomProgrammerBlockEntity;
import io.github.kisaragieffective.recomposedlogic.world.entity.ScalarComparatorBlockEntity;
import io.github.kisaragieffective.recomposedlogic.world.entity.ScalarConstantBlockEntity;
import io.github.kisaragieffective.recomposedlogic.world.entity.UndyedInternalJunctionBlockEntity;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.api.ModInitializer;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class Recomposedlogic implements ModInitializer {

    @Override
    public void onInitialize() {

        // This method is invoked by the Fabric mod loader when it is ready
        // to load your mod. You can access Fabric and Common code in this
        // project.

        // Use Fabric to bootstrap the Common mod.
        Constants.LOG.info("Hello Fabric world!");
        CommonClass.init();
        ModBlockEntities.SCALAR_CONSTANT = registerBlockEntity("scalar_constant", BlockEntityType.Builder.of(ScalarConstantBlockEntity::new, ModBlocks.SCALAR_CONSTANT).build());
        ModBlockEntities.SCALAR_COMPARATOR = registerBlockEntity("scalar_comparator", BlockEntityType.Builder.of(ScalarComparatorBlockEntity::new, ModBlocks.SCALAR_COMPARATOR).build());
        ModBlockEntities.CIRCUIT_ORIGIN = registerBlockEntity("circuit_origin", BlockEntityType.Builder.of(CircuitOriginBlockEntity::new, ModBlocks.CIRCUIT_ORIGIN).build());
        ModBlockEntities.CIRCUIT_SCANNER = registerBlockEntity("circuit_scanner", BlockEntityType.Builder.of(CircuitScannerBlockEntity::new, ModBlocks.CIRCUIT_SCANNER).build());
        ModBlockEntities.EXTERNAL_PORT = registerBlockEntity("external_port", BlockEntityType.Builder.of(ExternalPortBlockEntity::new, ModBlocks.EXTERNAL_PORT).build());
        ModBlockEntities.REDSTONE_EXPORTER = registerBlockEntity("redstone_exporter", BlockEntityType.Builder.of(RedstoneExporterBlockEntity::new, ModBlocks.REDSTONE_EXPORTER).build());
        ModBlockEntities.ROM_PROGRAMMER = registerBlockEntity("rom_programmer", BlockEntityType.Builder.of(RomProgrammerBlockEntity::new, ModBlocks.ROM_PROGRAMMER).build());
        ModBlockEntities.ROM_POWERING_GATE = registerBlockEntity("rom_powering_gate", BlockEntityType.Builder.of(RomPoweringGateBlockEntity::new, ModBlocks.ROM_POWERING_GATE).build());
        ModBlockEntities.PIN_CABLE = registerBlockEntity("pin_cable", BlockEntityType.Builder.of(PinCableBlockEntity::new, ModBlocks.PIN_CABLE).build());
        ModBlockEntities.DYED_PIN_CABLE = registerBlockEntity("dyed_pin_cable", BlockEntityType.Builder.of(DyedPinCableBlockEntity::new, ModBlocks.DYED_PIN_CABLE).build());
        ModBlockEntities.UNDYED_INTERNAL_JUNCTION = registerBlockEntity("undyed_internal_junction", BlockEntityType.Builder.of(UndyedInternalJunctionBlockEntity::new, ModBlocks.UNDYED_INTERNAL_JUNCTION).build());
        ModBlockEntities.DYED_INTERNAL_JUNCTION = registerBlockEntity("dyed_internal_junction", BlockEntityType.Builder.of(DyedInternalJunctionBlockEntity::new, ModBlocks.DYED_INTERNAL_JUNCTION).build());
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

    private static <T extends net.minecraft.world.level.block.entity.BlockEntity> net.minecraft.world.level.block.entity.BlockEntityType<T> registerBlockEntity(String name, net.minecraft.world.level.block.entity.BlockEntityType<T> type) {
        return Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, name), type);
    }
}
