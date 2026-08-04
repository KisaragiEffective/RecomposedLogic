package io.github.kisaragieffective.recomposedlogic.world.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.ChatFormatting;
import io.github.kisaragieffective.recomposedlogic.world.LogicBlockKind;
import io.github.kisaragieffective.recomposedlogic.world.LogicBlockKindProvider;
import io.github.kisaragieffective.recomposedlogic.world.LogicState;
import io.github.kisaragieffective.recomposedlogic.world.PortSpec;
import io.github.kisaragieffective.recomposedlogic.world.entity.ExternalPortBlockEntity;
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

public class ExternalPortBlock extends BaseEntityBlock implements LogicBlockKindProvider {
    public static final List<PortSpec> PORTS = List.of(new PortSpec("value", Direction.EAST, true));
    public static final MapCodec<ExternalPortBlock> CODEC = simpleCodec(ExternalPortBlock::new);

    public ExternalPortBlock(Properties properties) {
        super(properties);
    }

    @Override
    public LogicBlockKind kind() {
        return LogicBlockKind.EXTERNAL_PORT;
    }

    @Override
    public List<PortSpec> ports() {
        return PORTS;
    }

    @Override
    public List<PortSpec> ports(Level level, BlockPos pos) {
        boolean externalOutput = LogicState.externalOutput(level, pos);
        return List.of(new PortSpec("value", externalOutput ? Direction.WEST : Direction.EAST, !externalOutput));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ExternalPortBlockEntity(pos, state);
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
        if (level.getBlockEntity(pos) instanceof ExternalPortBlockEntity logic) {
            logic.toggleExternalDirection();
            player.displayClientMessage(net.minecraft.network.chat.Component.literal("External Port = " + (logic.externalOutput() ? "output" : "input")).withStyle(ChatFormatting.AQUA), true);
        }
        return InteractionResult.SUCCESS;
    }
}
