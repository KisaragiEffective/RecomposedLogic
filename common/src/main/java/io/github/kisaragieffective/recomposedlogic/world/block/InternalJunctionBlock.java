package io.github.kisaragieffective.recomposedlogic.world.block;

import com.mojang.serialization.MapCodec;
import io.github.kisaragieffective.recomposedlogic.registry.ModBlocks;
import io.github.kisaragieffective.recomposedlogic.world.LogicBlockKind;
import io.github.kisaragieffective.recomposedlogic.world.LogicBlockKindProvider;
import io.github.kisaragieffective.recomposedlogic.world.entity.DyedInternalJunctionBlockEntity;
import io.github.kisaragieffective.recomposedlogic.world.entity.UndyedInternalJunctionBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class InternalJunctionBlock extends BaseEntityBlock implements LogicBlockKindProvider {
    public static final MapCodec<InternalJunctionBlock> CODEC = simpleCodec(InternalJunctionBlock::new);
    private static final VoxelShape SHAPE = Shapes.block();

    public InternalJunctionBlock(Properties properties) {
        super(properties);
    }

    @Override
    public LogicBlockKind kind() {
        return LogicBlockKind.JUNCTION;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        if (state.getBlock() == ModBlocks.DYED_INTERNAL_JUNCTION) {
            return new DyedInternalJunctionBlockEntity(pos, state);
        }
        return new UndyedInternalJunctionBlockEntity(pos, state);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return Shapes.empty();
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (!level.isClientSide && !PinCableBlock.isCableSystemBlock(oldState)) {
            PinCableBlock.updateCableAndNeighbors(level, pos);
        }
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        super.neighborChanged(state, level, pos, block, fromPos, isMoving);
        if (!level.isClientSide) {
            PinCableBlock.updateCableAndNeighbors(level, pos);
        }
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        PinCableBlock.dropCableContents(level, pos, player);
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    public static boolean shouldReturnToCable(Level level, BlockPos pos) {
        return PinCableBlock.straightAxisFor(level, pos, level.getBlockState(pos)) != null;
    }

    public static BlockState cableStateFor(Level level, BlockPos pos) {
        Direction.Axis axis = PinCableBlock.straightAxisFor(level, pos, level.getBlockState(pos));
        if (axis == null) {
            axis = Direction.Axis.Y;
        }
        return ModBlocks.PIN_CABLE.defaultBlockState().setValue(PinCableBlock.AXIS, axis);
    }
}
