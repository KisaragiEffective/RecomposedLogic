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
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new RedstoneExporterBlockEntity(pos, state);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return net.minecraft.world.level.block.RenderShape.MODEL;
    }

    @Override
    protected boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    protected int getSignal(BlockState state, BlockGetter level, BlockPos pos, net.minecraft.core.Direction direction) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        return blockEntity instanceof RedstoneExporterBlockEntity logic ? logic.redstonePower() : 0;
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (!level.isClientSide) {
            level.scheduleTick(pos, this, 4);
        }
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        super.neighborChanged(state, level, pos, block, fromPos, isMoving);
        if (!level.isClientSide) {
            level.scheduleTick(pos, this, 1);
        }
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof RedstoneExporterBlockEntity logic) {
            double value = WorldRuntimeEvaluator.scalarInput(level, pos, "value");
            int power = (int) Math.floor(Math.max(0.0, Math.min(15.0, value)));
            if (power != logic.redstonePower()) {
                logic.redstonePower(power);
                level.updateNeighborsAt(pos, this);
            }
        }
        level.scheduleTick(pos, this, 4);
    }
}
