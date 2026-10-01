package io.github.kisaragieffective.recomposedlogic.world.block;

import com.mojang.serialization.MapCodec;
import io.github.kisaragieffective.recomposedlogic.world.LogicBlockKind;
import io.github.kisaragieffective.recomposedlogic.world.LogicBlockKindProvider;
import io.github.kisaragieffective.recomposedlogic.world.PortSpec;
import io.github.kisaragieffective.recomposedlogic.world.WorldRuntimeEvaluator;
import io.github.kisaragieffective.recomposedlogic.world.entity.RedstoneExporterBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class RedstoneExporterBlock extends BaseEntityBlock implements LogicBlockKindProvider {
    public static final List<PortSpec> PORTS = List.of(new PortSpec("value", Direction.WEST, false));
    public static final MapCodec<RedstoneExporterBlock> CODEC = simpleCodec(RedstoneExporterBlock::new);

    public RedstoneExporterBlock(Properties properties) {
        super(properties);
    }

    @Override
    public LogicBlockKind kind() {
        return LogicBlockKind.REDSTONE_EXPORTER;
    }

    @Override
    public List<PortSpec> ports() {
        return PORTS;
    }

    @Override
    protected @NotNull MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new RedstoneExporterBlockEntity(pos, state);
    }

    @SuppressWarnings("deprecation")
    @Override
    protected @NotNull RenderShape getRenderShape(@NotNull BlockState state) {
        return net.minecraft.world.level.block.RenderShape.MODEL;
    }

    @SuppressWarnings("deprecation")
    @Override
    protected boolean isSignalSource(@NotNull BlockState state) {
        return true;
    }

    @SuppressWarnings("deprecation")
    @Override
    protected int getSignal(@NotNull BlockState state, BlockGetter level, @NotNull BlockPos pos, net.minecraft.core.@NotNull Direction direction) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        return blockEntity instanceof RedstoneExporterBlockEntity logic ? logic.redstonePower() : 0;
    }

    @Override
    protected void onPlace(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (!level.isClientSide) {
            level.scheduleTick(pos, this, 4);
        }
    }

    @Override
    protected void neighborChanged(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Block block, @NotNull BlockPos fromPos, boolean isMoving) {
        super.neighborChanged(state, level, pos, block, fromPos, isMoving);
        if (!level.isClientSide) {
            level.scheduleTick(pos, this, 1);
        }
    }

    @Override
    protected void tick(@NotNull BlockState state, ServerLevel level, @NotNull BlockPos pos, @NotNull RandomSource random) {
        if (level.getBlockEntity(pos) instanceof RedstoneExporterBlockEntity logic) {
            double value = WorldRuntimeEvaluator.scalarInput(level, pos, "value");
            int power = (int) Math.floor(Math.clamp(value, 0.0, 15.0));
            if (power != logic.redstonePower()) {
                logic.redstonePower(power);
                level.updateNeighborsAt(pos, this);
            }
        }
        level.scheduleTick(pos, this, 4);
    }

}
