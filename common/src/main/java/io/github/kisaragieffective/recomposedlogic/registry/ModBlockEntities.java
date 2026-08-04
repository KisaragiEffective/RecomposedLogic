package io.github.kisaragieffective.recomposedlogic.registry;

import io.github.kisaragieffective.recomposedlogic.world.entity.CircuitOriginBlockEntity;
import io.github.kisaragieffective.recomposedlogic.world.entity.CircuitScannerBlockEntity;
import io.github.kisaragieffective.recomposedlogic.world.entity.ExternalPortBlockEntity;
import io.github.kisaragieffective.recomposedlogic.world.entity.RedstoneExporterBlockEntity;
import io.github.kisaragieffective.recomposedlogic.world.entity.RomPoweringGateBlockEntity;
import io.github.kisaragieffective.recomposedlogic.world.entity.RomProgrammerBlockEntity;
import io.github.kisaragieffective.recomposedlogic.world.entity.ScalarComparatorBlockEntity;
import io.github.kisaragieffective.recomposedlogic.world.entity.ScalarConstantBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

public final class ModBlockEntities {
    public static BlockEntityType<ScalarConstantBlockEntity> SCALAR_CONSTANT;
    public static BlockEntityType<ScalarComparatorBlockEntity> SCALAR_COMPARATOR;
    public static BlockEntityType<CircuitOriginBlockEntity> CIRCUIT_ORIGIN;
    public static BlockEntityType<CircuitScannerBlockEntity> CIRCUIT_SCANNER;
    public static BlockEntityType<ExternalPortBlockEntity> EXTERNAL_PORT;
    public static BlockEntityType<RedstoneExporterBlockEntity> REDSTONE_EXPORTER;
    public static BlockEntityType<RomProgrammerBlockEntity> ROM_PROGRAMMER;
    public static BlockEntityType<RomPoweringGateBlockEntity> ROM_POWERING_GATE;

    private ModBlockEntities() {
    }
}
