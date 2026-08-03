package io.github.kisaragieffective.recomposedlogic.world;

import com.mojang.serialization.MapCodec;
import io.github.kisaragieffective.recomposedlogic.logic.CompileResult;
import io.github.kisaragieffective.recomposedlogic.logic.RomNbt;
import io.github.kisaragieffective.recomposedlogic.logic.SignalValue;
import io.github.kisaragieffective.recomposedlogic.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class LogicBlock extends BaseEntityBlock {
    public static final MapCodec<LogicBlock> CODEC = simpleCodec(properties -> new LogicBlock(LogicBlockKind.PIN_CABLE, properties));
    private final LogicBlockKind kind;

    public LogicBlock(LogicBlockKind kind, Properties properties) {
        super(properties);
        this.kind = kind;
    }

    public LogicBlockKind kind() {
        return kind;
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new LogicBlockEntity(pos, state);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected boolean isSignalSource(BlockState state) {
        return kind == LogicBlockKind.REDSTONE_EXPORTER;
    }

    @Override
    protected int getSignal(BlockState state, BlockGetter level, BlockPos pos, net.minecraft.core.Direction direction) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        return blockEntity instanceof LogicBlockEntity logic ? logic.redstonePower() : 0;
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (!level.isClientSide) {
            level.scheduleTick(pos, this, 4);
        }
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, net.minecraft.world.level.block.Block block, BlockPos fromPos, boolean isMoving) {
        super.neighborChanged(state, level, pos, block, fromPos, isMoving);
        if (!level.isClientSide) {
            level.scheduleTick(pos, this, 1);
        }
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (kind == LogicBlockKind.REDSTONE_EXPORTER) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof LogicBlockEntity logic) {
                double value = WorldRuntimeEvaluator.scalarInput(level, pos, "value");
                int power = (int) Math.floor(Math.max(0.0, Math.min(15.0, value)));
                if (power != logic.redstonePower()) {
                    logic.redstonePower(power);
                    level.updateNeighborsAt(pos, this);
                }
            }
        }
        level.scheduleTick(pos, this, 4);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (level.isClientSide) {
            return ItemInteractionResult.SUCCESS;
        }
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof LogicBlockEntity logic)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (stack.is(ModItems.WRENCH)) {
            WrenchItem.useOnLogic(player, pos, logic);
            return ItemInteractionResult.SUCCESS;
        }
        if (kind == LogicBlockKind.ROM_PROGRAMMER) {
            return useProgrammerItem(stack, logic, player);
        }
        if (kind == LogicBlockKind.ROM_POWERING_GATE && stack.is(ModItems.PROGRAMMED_ROM)) {
            logic.romProgram(RomNbt.read(stack));
            say(player, "Loaded ROM into gate", ChatFormatting.GREEN);
            return ItemInteractionResult.SUCCESS;
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof LogicBlockEntity logic)) {
            return InteractionResult.PASS;
        }
        switch (kind) {
            case SCALAR_CONSTANT -> {
                double next = logic.scalarValue() >= 15.0 ? 0.0 : logic.scalarValue() + 1.0;
                logic.scalarValue(next);
                say(player, "Scalar Constant = " + next, ChatFormatting.AQUA);
            }
            case SCALAR_COMPARATOR -> {
                logic.comparatorOperator(logic.comparatorOperator().next());
                say(player, "Comparator = " + logic.comparatorOperator(), ChatFormatting.AQUA);
            }
            case EXTERNAL_PORT -> {
                logic.toggleExternalDirection();
                say(player, "External Port = " + (logic.externalOutput() ? "output" : "input"), ChatFormatting.AQUA);
            }
            case CIRCUIT_ORIGIN -> {
                logic.growOrigin();
                say(player, "Origin volume max offset = " + logic.originMaxOffset().toShortString(), ChatFormatting.AQUA);
            }
            case CIRCUIT_SCANNER -> scanToBlueprint(level, logic, player);
            case ROM_PROGRAMMER -> burnRom(logic, player);
            case ROM_POWERING_GATE -> {
                SignalValue value = WorldRuntimeEvaluator.valueAt(level, pos, new java.util.HashSet<>());
                say(player, "ROM output = " + value, ChatFormatting.AQUA);
            }
            default -> say(player, logic.status(), ChatFormatting.GRAY);
        }
        return InteractionResult.SUCCESS;
    }

    private static ItemInteractionResult useProgrammerItem(ItemStack stack, LogicBlockEntity logic, Player player) {
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

    private static void scanToBlueprint(Level level, LogicBlockEntity scanner, Player player) {
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

    private static void burnRom(LogicBlockEntity programmer, Player player) {
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
        player.displayClientMessage(Component.literal(message).withStyle(formatting), true);
    }
}
