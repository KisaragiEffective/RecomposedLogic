package io.github.kisaragieffective.recomposedlogic.world.block;

import com.mojang.serialization.MapCodec;
import io.github.kisaragieffective.recomposedlogic.logic.CompileResult;
import io.github.kisaragieffective.recomposedlogic.logic.RomNbt;
import io.github.kisaragieffective.recomposedlogic.registry.ModItems;
import io.github.kisaragieffective.recomposedlogic.world.LogicBlockKind;
import io.github.kisaragieffective.recomposedlogic.world.LogicBlockKindProvider;
import io.github.kisaragieffective.recomposedlogic.world.WrenchItem;
import io.github.kisaragieffective.recomposedlogic.world.WorldCircuitScanner;
import io.github.kisaragieffective.recomposedlogic.world.entity.CircuitScannerBlockEntity;
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

public class CircuitScannerBlock extends BaseEntityBlock implements LogicBlockKindProvider {
    public static final MapCodec<CircuitScannerBlock> CODEC = simpleCodec(CircuitScannerBlock::new);

    public CircuitScannerBlock(Properties properties) {
        super(properties);
    }

    @Override
    public LogicBlockKind kind() {
        return LogicBlockKind.CIRCUIT_SCANNER;
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CircuitScannerBlockEntity(pos, state);
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
        if (stack.is(ModItems.WRENCH) && level.getBlockEntity(pos) instanceof CircuitScannerBlockEntity logic) {
            BlockPos origin = WrenchItem.selectedOrigin(player);
            if (origin == null) {
                say(player, "Select a Circuit Origin first", ChatFormatting.YELLOW);
                return ItemInteractionResult.SUCCESS;
            }
            logic.pairedOrigin(origin);
            say(player, "Scanner paired to origin " + origin.toShortString(), ChatFormatting.YELLOW);
            return ItemInteractionResult.SUCCESS;
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (level.getBlockEntity(pos) instanceof CircuitScannerBlockEntity logic) {
            scanToBlueprint(level, logic, player);
        }
        return InteractionResult.SUCCESS;
    }

    private static void scanToBlueprint(Level level, CircuitScannerBlockEntity scanner, Player player) {
        if (scanner.pairedOrigin() == null) {
            say(player, "Scanner has no paired origin", ChatFormatting.RED);
            return;
        }
        CompileResult result = WorldCircuitScanner.scan(level, scanner.pairedOrigin());
        if (!result.success()) {
            String message = result.diagnostics().isEmpty() ? "Scan failed" : result.diagnostics().get(0);
            scanner.status(message);
            say(player, message, ChatFormatting.RED);
            return;
        }
        ItemStack blueprint = new ItemStack(ModItems.BLUEPRINT);
        RomNbt.write(blueprint, result.program());
        scanner.status("Blueprint generated: " + result.program().instructions().size() + " instructions");
        player.addItem(blueprint);
        say(player, scanner.status(), ChatFormatting.GREEN);
    }

    private static void say(Player player, String message, ChatFormatting formatting) {
        player.displayClientMessage(net.minecraft.network.chat.Component.literal(message).withStyle(formatting), true);
    }
}
