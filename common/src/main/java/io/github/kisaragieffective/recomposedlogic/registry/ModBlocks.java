package io.github.kisaragieffective.recomposedlogic.registry;

import io.github.kisaragieffective.recomposedlogic.Constants;
import io.github.kisaragieffective.recomposedlogic.world.block.BooleanAndBlock;
import io.github.kisaragieffective.recomposedlogic.world.block.BooleanConditionalBlock;
import io.github.kisaragieffective.recomposedlogic.world.block.BooleanNotBlock;
import io.github.kisaragieffective.recomposedlogic.world.block.CircuitOriginBlock;
import io.github.kisaragieffective.recomposedlogic.world.block.CircuitScannerBlock;
import io.github.kisaragieffective.recomposedlogic.world.block.ExternalPortBlock;
import io.github.kisaragieffective.recomposedlogic.world.block.JunctionBlock;
import io.github.kisaragieffective.recomposedlogic.world.block.PinCableBlock;
import io.github.kisaragieffective.recomposedlogic.world.block.RedstoneExporterBlock;
import io.github.kisaragieffective.recomposedlogic.world.block.RedstoneImporterBlock;
import io.github.kisaragieffective.recomposedlogic.world.block.RomPoweringGateBlock;
import io.github.kisaragieffective.recomposedlogic.world.block.RomProgrammerBlock;
import io.github.kisaragieffective.recomposedlogic.world.block.ScalarAddBlock;
import io.github.kisaragieffective.recomposedlogic.world.block.ScalarComparatorBlock;
import io.github.kisaragieffective.recomposedlogic.world.block.ScalarConstantBlock;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

public final class ModBlocks {
    public static final Map<String, Block> ALL = new LinkedHashMap<>();

    public static final Block PIN_CABLE = block("pin_cable", PinCableBlock::new);
    public static final Block JUNCTION = block("junction", JunctionBlock::new);
    public static final Block SCALAR_CONSTANT = block("scalar_constant", ScalarConstantBlock::new);
    public static final Block SCALAR_ADD = block("scalar_add", ScalarAddBlock::new);
    public static final Block SCALAR_COMPARATOR = block("scalar_comparator", ScalarComparatorBlock::new);
    public static final Block BOOLEAN_AND = block("boolean_and", BooleanAndBlock::new);
    public static final Block BOOLEAN_NOT = block("boolean_not", BooleanNotBlock::new);
    public static final Block BOOLEAN_CONDITIONAL = block("boolean_conditional", BooleanConditionalBlock::new);
    public static final Block REDSTONE_IMPORTER = block("redstone_importer", RedstoneImporterBlock::new);
    public static final Block REDSTONE_EXPORTER = block("redstone_exporter", RedstoneExporterBlock::new);
    public static final Block CIRCUIT_ORIGIN = block("circuit_origin", CircuitOriginBlock::new);
    public static final Block CIRCUIT_SCANNER = block("circuit_scanner", CircuitScannerBlock::new);
    public static final Block EXTERNAL_PORT = block("external_port", ExternalPortBlock::new);
    public static final Block ROM_PROGRAMMER = block("rom_programmer", RomProgrammerBlock::new);
    public static final Block ROM_POWERING_GATE = block("rom_powering_gate", RomPoweringGateBlock::new);

    private ModBlocks() {
    }

    public static void register() {
        ALL.forEach((name, block) -> Registry.register(BuiltInRegistries.BLOCK, id(name), block));
    }

    private static Block block(String name, Function<BlockBehaviour.Properties, Block> factory) {
        Block block = factory.apply(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(1.5F).sound(SoundType.METAL));
        ALL.put(name, block);
        return block;
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, path);
    }
}
