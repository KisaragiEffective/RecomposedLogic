package io.github.kisaragieffective.recomposedlogic.world;

import io.github.kisaragieffective.recomposedlogic.logic.ComparatorOperator;
import io.github.kisaragieffective.recomposedlogic.logic.RomProgram;
import io.github.kisaragieffective.recomposedlogic.world.entity.CircuitOriginBlockEntity;
import io.github.kisaragieffective.recomposedlogic.world.entity.CircuitScannerBlockEntity;
import io.github.kisaragieffective.recomposedlogic.world.entity.ExternalPortBlockEntity;
import io.github.kisaragieffective.recomposedlogic.world.entity.RedstoneExporterBlockEntity;
import io.github.kisaragieffective.recomposedlogic.world.entity.RomPoweringGateBlockEntity;
import io.github.kisaragieffective.recomposedlogic.world.entity.RomProgrammerBlockEntity;
import io.github.kisaragieffective.recomposedlogic.world.entity.ScalarComparatorBlockEntity;
import io.github.kisaragieffective.recomposedlogic.world.entity.ScalarConstantBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class LogicState {
    private LogicState() {
    }

    public static @Nullable LogicBlockKind kind(Level level, BlockPos pos) {
        if (level.getBlockState(pos).getBlock() instanceof LogicBlockKindProvider provider) {
            return provider.kind();
        }
        return null;
    }

    public static List<PortSpec> ports(Level level, BlockPos pos) {
        if (level.getBlockState(pos).getBlock() instanceof LogicBlockKindProvider provider) {
            return provider.ports(level, pos);
        }
        return List.of();
    }

    public static double scalarValue(Level level, BlockPos pos) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        return blockEntity instanceof ScalarConstantBlockEntity scalar ? scalar.scalarValue() : 1.0;
    }

    public static ComparatorOperator comparatorOperator(Level level, BlockPos pos) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        return blockEntity instanceof ScalarComparatorBlockEntity comparator ? comparator.operator() : ComparatorOperator.GREATER_OR_EQUAL;
    }

    public static boolean externalOutput(Level level, BlockPos pos) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        return blockEntity instanceof ExternalPortBlockEntity port && port.externalOutput();
    }

    public static RomProgram romProgram(Level level, BlockPos pos) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        return blockEntity instanceof RomPoweringGateBlockEntity rom ? rom.romProgram() : null;
    }

    public static int redstonePower(Level level, BlockPos pos) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        return blockEntity instanceof RedstoneExporterBlockEntity exporter ? exporter.redstonePower() : 0;
    }

    public static BlockPos pairedOrigin(Level level, BlockPos pos) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        return blockEntity instanceof CircuitScannerBlockEntity scanner ? scanner.pairedOrigin() : null;
    }

    public static BlockPos originMaxOffset(Level level, BlockPos pos) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        return blockEntity instanceof CircuitOriginBlockEntity origin ? origin.originMaxOffset() : new BlockPos(2, 2, 2);
    }

    public static ItemStack programmerSlotA(Level level, BlockPos pos) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        return blockEntity instanceof RomProgrammerBlockEntity programmer ? programmer.slotA() : ItemStack.EMPTY;
    }

    public static ItemStack programmerSlotB(Level level, BlockPos pos) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        return blockEntity instanceof RomProgrammerBlockEntity programmer ? programmer.slotB() : ItemStack.EMPTY;
    }
}
