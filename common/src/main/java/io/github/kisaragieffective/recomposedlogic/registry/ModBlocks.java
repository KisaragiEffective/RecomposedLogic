package io.github.kisaragieffective.recomposedlogic.registry;

import io.github.kisaragieffective.recomposedlogic.Constants;
import io.github.kisaragieffective.recomposedlogic.world.LogicBlock;
import io.github.kisaragieffective.recomposedlogic.world.LogicBlockKind;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

import java.util.LinkedHashMap;
import java.util.Map;

public final class ModBlocks {
    public static final Map<String, Block> ALL = new LinkedHashMap<>();

    public static final Block PIN_CABLE = block("pin_cable", LogicBlockKind.PIN_CABLE);
    public static final Block JUNCTION = block("junction", LogicBlockKind.JUNCTION);
    public static final Block SCALAR_CONSTANT = block("scalar_constant", LogicBlockKind.SCALAR_CONSTANT);
    public static final Block SCALAR_ADD = block("scalar_add", LogicBlockKind.SCALAR_ADD);
    public static final Block SCALAR_COMPARATOR = block("scalar_comparator", LogicBlockKind.SCALAR_COMPARATOR);
    public static final Block BOOLEAN_AND = block("boolean_and", LogicBlockKind.BOOLEAN_AND);
    public static final Block BOOLEAN_NOT = block("boolean_not", LogicBlockKind.BOOLEAN_NOT);
    public static final Block BOOLEAN_CONDITIONAL = block("boolean_conditional", LogicBlockKind.BOOLEAN_CONDITIONAL);
    public static final Block REDSTONE_IMPORTER = block("redstone_importer", LogicBlockKind.REDSTONE_IMPORTER);
    public static final Block REDSTONE_EXPORTER = block("redstone_exporter", LogicBlockKind.REDSTONE_EXPORTER);
    public static final Block CIRCUIT_ORIGIN = block("circuit_origin", LogicBlockKind.CIRCUIT_ORIGIN);
    public static final Block CIRCUIT_SCANNER = block("circuit_scanner", LogicBlockKind.CIRCUIT_SCANNER);
    public static final Block EXTERNAL_PORT = block("external_port", LogicBlockKind.EXTERNAL_PORT);
    public static final Block ROM_PROGRAMMER = block("rom_programmer", LogicBlockKind.ROM_PROGRAMMER);
    public static final Block ROM_POWERING_GATE = block("rom_powering_gate", LogicBlockKind.ROM_POWERING_GATE);

    private ModBlocks() {
    }

    public static void register() {
        ALL.forEach((name, block) -> Registry.register(BuiltInRegistries.BLOCK, id(name), block));
    }

    private static Block block(String name, LogicBlockKind kind) {
        Block block = new LogicBlock(kind, BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(1.5F).sound(SoundType.METAL));
        ALL.put(name, block);
        return block;
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, path);
    }
}
