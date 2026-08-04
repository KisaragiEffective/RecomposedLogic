package io.github.kisaragieffective.recomposedlogic.world.block;

import com.mojang.serialization.MapCodec;
import io.github.kisaragieffective.recomposedlogic.logic.RomNbt;
import io.github.kisaragieffective.recomposedlogic.registry.ModItems;
import io.github.kisaragieffective.recomposedlogic.world.LogicBlockKind;
import io.github.kisaragieffective.recomposedlogic.world.LogicBlockKindProvider;
import io.github.kisaragieffective.recomposedlogic.world.entity.RomProgrammerBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
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

public class RomProgrammerBlock extends BaseEntityBlock implements LogicBlockKindProvider {
    public static final MapCodec<RomProgrammerBlock> CODEC = simpleCodec(RomProgrammerBlock::new);

    public RomProgrammerBlock(Properties properties) {
        super(properties);
    }

    @Override
    public LogicBlockKind kind() {
        return LogicBlockKind.ROM_PROGRAMMER;
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new RomProgrammerBlockEntity(pos, state);
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
        if (level.getBlockEntity(pos) instanceof RomProgrammerBlockEntity logic) {
            return insertProgrammerItem(stack, logic, player);
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (level.getBlockEntity(pos) instanceof RomProgrammerBlockEntity logic) {
            burnRom(logic, player);
        }
        return InteractionResult.SUCCESS;
    }

    private static ItemInteractionResult insertProgrammerItem(ItemStack stack, RomProgrammerBlockEntity logic, Player player) {
        if (stack.is(ModItems.BLUEPRINT) && logic.slotA().isEmpty()) {
            logic.slotA(stack.copyWithCount(1));
            stack.shrink(1);
            say(player, "Blueprint inserted", ChatFormatting.GREEN);
            return ItemInteractionResult.SUCCESS;
        }
        if (stack.is(ModItems.TINY_MEMORY) && logic.slotB().isEmpty()) {
            logic.slotB(stack.copyWithCount(1));
            stack.shrink(1);
            say(player, "Tiny Memory inserted", ChatFormatting.GREEN);
            return ItemInteractionResult.SUCCESS;
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    private static void burnRom(RomProgrammerBlockEntity programmer, Player player) {
        if (programmer.slotA().isEmpty() || programmer.slotB().isEmpty()) {
            say(player, "Insert Blueprint and Tiny Memory", ChatFormatting.RED);
            return;
        }
        var program = RomNbt.read(programmer.slotA());
        if (program == null) {
            say(player, "Blueprint has no program", ChatFormatting.RED);
            return;
        }
        if (program.scannedVolume() > 27) {
            say(player, "Tiny Memory capacity exceeded: " + program.scannedVolume() + "/27", ChatFormatting.RED);
            return;
        }
        ItemStack rom = new ItemStack(ModItems.PROGRAMMED_ROM);
        RomNbt.write(rom, program);
        programmer.slotA(ItemStack.EMPTY);
        programmer.slotB(ItemStack.EMPTY);
        programmer.status("ROM burned");
        player.addItem(rom);
        say(player, "ROM burned", ChatFormatting.GREEN);
    }

    private static void say(Player player, String message, ChatFormatting formatting) {
        player.displayClientMessage(net.minecraft.network.chat.Component.literal(message).withStyle(formatting), true);
    }
}
