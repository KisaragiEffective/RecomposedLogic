package io.github.kisaragieffective.recomposedlogic.world.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.ChatFormatting;
import io.github.kisaragieffective.recomposedlogic.world.LogicBlockKind;
import io.github.kisaragieffective.recomposedlogic.world.LogicBlockKindProvider;
import io.github.kisaragieffective.recomposedlogic.world.PortSpec;
import io.github.kisaragieffective.recomposedlogic.world.entity.ScalarComparatorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import java.util.List;

public class ScalarComparatorBlock extends BaseEntityBlock implements LogicBlockKindProvider {
    public static final List<PortSpec> PORTS = List.of(new PortSpec("left", Direction.WEST, false), new PortSpec("right", Direction.NORTH, false), new PortSpec("value", Direction.EAST, true));
    public static final MapCodec<ScalarComparatorBlock> CODEC = simpleCodec(ScalarComparatorBlock::new);

    public ScalarComparatorBlock(Properties properties) {
        super(properties);
    }

    @Override
    public LogicBlockKind kind() {
        return LogicBlockKind.SCALAR_COMPARATOR;
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
        return new ScalarComparatorBlockEntity(pos, state);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return net.minecraft.world.level.block.RenderShape.MODEL;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (level.getBlockEntity(pos) instanceof ScalarComparatorBlockEntity logic) {
            logic.operator(logic.operator().next());
            player.displayClientMessage(net.minecraft.network.chat.Component.literal("Comparator = " + logic.operator()).withStyle(ChatFormatting.AQUA), true);
        }
        return InteractionResult.SUCCESS;
    }
}
