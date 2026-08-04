package io.github.kisaragieffective.recomposedlogic.world.block;

import com.mojang.serialization.MapCodec;
import io.github.kisaragieffective.recomposedlogic.logic.RomNbt;
import io.github.kisaragieffective.recomposedlogic.logic.SignalValue;
import io.github.kisaragieffective.recomposedlogic.registry.ModItems;
import io.github.kisaragieffective.recomposedlogic.world.LogicBlockKind;
import io.github.kisaragieffective.recomposedlogic.world.LogicBlockKindProvider;
import io.github.kisaragieffective.recomposedlogic.world.PortSpec;
import io.github.kisaragieffective.recomposedlogic.world.WorldRuntimeEvaluator;
import io.github.kisaragieffective.recomposedlogic.world.entity.RomPoweringGateBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import java.util.List;

public class RomPoweringGateBlock extends BaseEntityBlock implements LogicBlockKindProvider {
    public static final List<PortSpec> PORTS = List.of(new PortSpec("input0", Direction.WEST, false), new PortSpec("value", Direction.EAST, true));
    public static final MapCodec<RomPoweringGateBlock> CODEC = simpleCodec(RomPoweringGateBlock::new);

    public RomPoweringGateBlock(Properties properties) {
        super(properties);
    }

    @Override
    public LogicBlockKind kind() {
        return LogicBlockKind.ROM_POWERING_GATE;
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
        return new RomPoweringGateBlockEntity(pos, state);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return net.minecraft.world.level.block.RenderShape.MODEL;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (level.isClientSide) {
            return ItemInteractionResult.SUCCESS;
        }
        if (stack.is(ModItems.PROGRAMMED_ROM) && level.getBlockEntity(pos) instanceof RomPoweringGateBlockEntity logic) {
            logic.romProgram(RomNbt.read(stack));
            player.displayClientMessage(net.minecraft.network.chat.Component.literal("Loaded ROM into gate").withStyle(ChatFormatting.GREEN), true);
            return ItemInteractionResult.SUCCESS;
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        SignalValue value = WorldRuntimeEvaluator.valueAt(level, pos, new java.util.HashSet<>());
        player.displayClientMessage(net.minecraft.network.chat.Component.literal("ROM output = " + value).withStyle(ChatFormatting.AQUA), true);
        return InteractionResult.SUCCESS;
    }
}
