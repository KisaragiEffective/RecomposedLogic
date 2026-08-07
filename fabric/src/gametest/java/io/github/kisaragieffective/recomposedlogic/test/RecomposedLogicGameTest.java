package io.github.kisaragieffective.recomposedlogic.test;

import io.github.kisaragieffective.recomposedlogic.logic.CircuitCompiler;
import io.github.kisaragieffective.recomposedlogic.logic.CircuitGraph;
import io.github.kisaragieffective.recomposedlogic.logic.CircuitNode;
import io.github.kisaragieffective.recomposedlogic.logic.CompileResult;
import io.github.kisaragieffective.recomposedlogic.logic.NodeKind;
import io.github.kisaragieffective.recomposedlogic.logic.PortRef;
import io.github.kisaragieffective.recomposedlogic.logic.RomProgram;
import io.github.kisaragieffective.recomposedlogic.logic.SignalValue;
import io.github.kisaragieffective.recomposedlogic.registry.ModBlocks;
import io.github.kisaragieffective.recomposedlogic.registry.ModItems;
import io.github.kisaragieffective.recomposedlogic.world.WorldCircuitScanner;
import io.github.kisaragieffective.recomposedlogic.world.WorldRuntimeEvaluator;
import io.github.kisaragieffective.recomposedlogic.world.block.PinCableBlock;
import io.github.kisaragieffective.recomposedlogic.world.entity.ColoredCableBlockEntity;
import io.github.kisaragieffective.recomposedlogic.world.entity.ExternalPortBlockEntity;
import io.github.kisaragieffective.recomposedlogic.world.entity.RomPoweringGateBlockEntity;
import io.github.kisaragieffective.recomposedlogic.world.entity.ScalarConstantBlockEntity;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.block.Blocks;

import java.util.Map;

public final class RecomposedLogicGameTest implements FabricGameTest {
    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE, timeoutTicks = 100)
    public void w2eRuntimeAddOneFromRedstoneImporter(GameTestHelper helper) {
        BlockPos redstone = new BlockPos(1, 1, 1);
        BlockPos importer = new BlockPos(2, 1, 1);
        BlockPos add = new BlockPos(3, 1, 1);
        BlockPos one = new BlockPos(3, 1, 0);
        BlockPos exporter = new BlockPos(4, 1, 1);

        helper.setBlock(redstone, Blocks.REDSTONE_BLOCK);
        helper.setBlock(importer, ModBlocks.REDSTONE_IMPORTER);
        helper.setBlock(add, ModBlocks.SCALAR_ADD);
        scalar(helper, one, 1.0);
        helper.setBlock(exporter, ModBlocks.REDSTONE_EXPORTER);

        double value = WorldRuntimeEvaluator.scalarInput(helper.getLevel(), helper.absolutePos(exporter), "value");
        helper.assertValueEqual(value, 16.0, "RS importer -> Add(+1) -> exporter should evaluate to 16");
        helper.succeed();
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE, timeoutTicks = 100)
    public void scannerCompilesAddOneWithExternalPortsOnVolumeShell(GameTestHelper helper) {
        BlockPos origin = addOneCircuit(helper);

        CompileResult result = WorldCircuitScanner.scan(helper.getLevel(), helper.absolutePos(origin));
        RomProgram program = requireProgram(helper, result);

        helper.assertValueEqual(program.inputs().size(), 1, "add-one ROM should have one input");
        helper.assertValueEqual(program.outputs().size(), 1, "add-one ROM should have one output");
        helper.assertValueEqual(program.instructions().size(), 4, "add-one ROM should compile input, constant, add, output");
        double value = io.github.kisaragieffective.recomposedlogic.logic.CircuitEvaluator
                .evaluate(program, Map.of(program.inputs().getFirst(), SignalValue.scalar(5.0)))
                .get(program.outputs().getFirst())
                .asScalar();
        helper.assertValueEqual(value, 6.0, "compiled add-one ROM should return input + 1");
        helper.succeed();
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE, timeoutTicks = 100)
    public void romGateExecutesLoadedRomInWorldRuntime(GameTestHelper helper) {
        RomProgram addOne = compileAddOneProgram();
        BlockPos input = new BlockPos(1, 1, 1);
        BlockPos rom = new BlockPos(2, 1, 1);
        BlockPos exporter = new BlockPos(3, 1, 1);

        scalar(helper, input, 2.0);
        helper.setBlock(rom, ModBlocks.ROM_POWERING_GATE);
        helper.<RomPoweringGateBlockEntity>getBlockEntity(rom).romProgram(addOne);
        helper.setBlock(exporter, ModBlocks.REDSTONE_EXPORTER);

        double value = WorldRuntimeEvaluator.scalarInput(helper.getLevel(), helper.absolutePos(exporter), "value");
        helper.assertValueEqual(value, 3.0, "ROM gate should execute add-one program");
        helper.succeed();
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE, timeoutTicks = 100)
    public void scannerCanCompileCircuitContainingRomGateAgain(GameTestHelper helper) {
        RomProgram addOne = compileAddOneProgram();
        BlockPos origin = new BlockPos(2, 0, 2);
        BlockPos input = new BlockPos(1, 1, 3);
        BlockPos rom = new BlockPos(2, 1, 3);
        BlockPos add = new BlockPos(3, 1, 3);
        BlockPos one = new BlockPos(3, 1, 2);
        BlockPos cable = new BlockPos(4, 1, 3);
        BlockPos output = new BlockPos(5, 1, 3);

        helper.setBlock(origin, ModBlocks.CIRCUIT_ORIGIN);
        externalPort(helper, input, false);
        helper.setBlock(rom, ModBlocks.ROM_POWERING_GATE);
        helper.<RomPoweringGateBlockEntity>getBlockEntity(rom).romProgram(addOne);
        helper.setBlock(add, ModBlocks.SCALAR_ADD);
        scalar(helper, one, 1.0);
        helper.setBlock(cable, ModBlocks.PIN_CABLE);
        externalPort(helper, output, true);

        RomProgram addTwo = requireProgram(helper, WorldCircuitScanner.scan(helper.getLevel(), helper.absolutePos(origin)));
        helper.assertTrue(addTwo.instructions().stream().anyMatch(instruction -> instruction.kind() == NodeKind.ROM),
                "recompiled ROM should contain a ROM instruction");

        double value = io.github.kisaragieffective.recomposedlogic.logic.CircuitEvaluator
                .evaluate(addTwo, Map.of(addTwo.inputs().getFirst(), SignalValue.scalar(3.0)))
                .get(addTwo.outputs().getFirst())
                .asScalar();
        helper.assertValueEqual(value, 5.0, "ROM containing add-one plus Add(+1) should return input + 2");
        helper.succeed();
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE, timeoutTicks = 100)
    public void bentPinCableBecomesInternalJunction(GameTestHelper helper) {
        BlockPos center = new BlockPos(2, 1, 2);

        helper.setBlock(center, ModBlocks.PIN_CABLE);
        helper.setBlock(center.west(), ModBlocks.PIN_CABLE);
        helper.setBlock(center.north(), ModBlocks.PIN_CABLE);

        helper.assertBlockPresent(ModBlocks.UNDYED_INTERNAL_JUNCTION, center);
        helper.succeed();
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE, timeoutTicks = 100)
    public void scannerDoesNotConnectDifferentDyedCableColors(GameTestHelper helper) {
        BlockPos origin = new BlockPos(2, 0, 2);
        BlockPos red = new BlockPos(2, 1, 3);
        BlockPos blue = new BlockPos(3, 1, 3);
        BlockPos output = new BlockPos(4, 1, 3);

        helper.setBlock(origin, ModBlocks.CIRCUIT_ORIGIN);
        dyedCable(helper, red, DyeColor.RED);
        dyedCable(helper, blue, DyeColor.BLUE);
        externalPort(helper, new BlockPos(1, 1, 3), false);
        externalPort(helper, output, true);

        RomProgram program = requireProgram(helper, WorldCircuitScanner.scan(helper.getLevel(), helper.absolutePos(origin)));
        double value = io.github.kisaragieffective.recomposedlogic.logic.CircuitEvaluator
                .evaluate(program, Map.of(program.inputs().getFirst(), SignalValue.scalar(5.0)))
                .get(program.outputs().getFirst())
                .asScalar();

        helper.assertValueEqual(value, 0.0, "different dyed cable colors should not propagate the input");
        helper.succeed();
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE, timeoutTicks = 100)
    public void dyedCableBlockEntityCanHoldMultipleColors(GameTestHelper helper) {
        BlockPos pos = new BlockPos(2, 1, 2);

        dyedCable(helper, pos, DyeColor.RED);
        helper.<ColoredCableBlockEntity>getBlockEntity(pos).addColor(DyeColor.BLUE);
        PinCableBlock.updateCableAndNeighbors(helper.getLevel(), helper.absolutePos(pos));

        ColoredCableBlockEntity cable = helper.getBlockEntity(pos);
        helper.assertTrue(cable.colors().contains(DyeColor.RED), "dyed cable should keep the first color");
        helper.assertTrue(cable.colors().contains(DyeColor.BLUE), "dyed cable should accept another color in the same block");
        helper.assertValueEqual(cable.colors().size(), 2, "dyed cable should hold two colors");
        helper.succeed();
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE, timeoutTicks = 100)
    public void sameColorDyedBranchBecomesInternalJunction(GameTestHelper helper) {
        BlockPos center = new BlockPos(2, 1, 2);

        dyedCable(helper, center, DyeColor.RED);
        dyedCable(helper, center.west(), DyeColor.RED);
        dyedCable(helper, center.north(), DyeColor.RED);
        PinCableBlock.updateCableAndNeighbors(helper.getLevel(), helper.absolutePos(center));

        if (!helper.getBlockState(center).is(ModBlocks.DYED_INTERNAL_JUNCTION)) {
            helper.fail("expected dyed junction; red directions were "
                    + PinCableBlock.connectionDirections(helper.getLevel(), helper.absolutePos(center), DyeColor.RED)
                    + ", axis was "
                    + PinCableBlock.straightAxisFor(helper.getLevel(), helper.absolutePos(center), helper.getLevel().getBlockState(helper.absolutePos(center))));
        }
        ColoredCableBlockEntity cable = helper.getBlockEntity(center);
        helper.assertTrue(cable.colors().contains(DyeColor.RED), "dyed junction should keep the branched color");
        helper.succeed();
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE, timeoutTicks = 100)
    public void differentDyedColorsCanCrossAsInternalJunction(GameTestHelper helper) {
        BlockPos center = new BlockPos(2, 1, 2);

        dyedCable(helper, center, DyeColor.RED);
        dyedCable(helper, center.west(), DyeColor.RED);
        dyedCable(helper, center.east(), DyeColor.RED);
        helper.<ColoredCableBlockEntity>getBlockEntity(center).addColor(DyeColor.BLUE);
        dyedCable(helper, center.north(), DyeColor.BLUE);
        dyedCable(helper, center.south(), DyeColor.BLUE);
        PinCableBlock.updateCableAndNeighbors(helper.getLevel(), helper.absolutePos(center));

        if (!helper.getBlockState(center).is(ModBlocks.DYED_INTERNAL_JUNCTION)) {
            helper.fail("expected dyed junction; red directions were "
                    + PinCableBlock.connectionDirections(helper.getLevel(), helper.absolutePos(center), DyeColor.RED)
                    + ", blue directions were "
                    + PinCableBlock.connectionDirections(helper.getLevel(), helper.absolutePos(center), DyeColor.BLUE)
                    + ", axis was "
                    + PinCableBlock.straightAxisFor(helper.getLevel(), helper.absolutePos(center), helper.getLevel().getBlockState(helper.absolutePos(center))));
        }
        ColoredCableBlockEntity cable = helper.getBlockEntity(center);
        helper.assertTrue(cable.colors().contains(DyeColor.RED), "dyed crossing should keep red");
        helper.assertTrue(cable.colors().contains(DyeColor.BLUE), "dyed crossing should keep blue");
        helper.succeed();
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE, timeoutTicks = 100)
    public void runtimeDoesNotLeakSignalBetweenColorsInDyedJunction(GameTestHelper helper) {
        BlockPos center = new BlockPos(3, 1, 2);
        BlockPos source = new BlockPos(1, 1, 2);
        BlockPos redCable = new BlockPos(2, 1, 2);
        BlockPos blueCable = new BlockPos(4, 1, 2);
        BlockPos exporter = new BlockPos(5, 1, 2);

        scalar(helper, source, 7.0);
        dyedCable(helper, redCable, DyeColor.RED);
        dyedCable(helper, center, DyeColor.RED);
        helper.<ColoredCableBlockEntity>getBlockEntity(center).addColor(DyeColor.BLUE);
        dyedCable(helper, blueCable, DyeColor.BLUE);
        helper.setBlock(exporter, ModBlocks.REDSTONE_EXPORTER);
        PinCableBlock.updateCableAndNeighbors(helper.getLevel(), helper.absolutePos(center));

        double value = WorldRuntimeEvaluator.scalarInput(helper.getLevel(), helper.absolutePos(exporter), "value");

        helper.assertValueEqual(value, 0.0, "red input must not leak through a dyed junction into a blue cable");
        helper.succeed();
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE, timeoutTicks = 100)
    public void runtimeKeepsSameColorConnectedThroughDyedJunction(GameTestHelper helper) {
        BlockPos center = new BlockPos(3, 1, 2);
        BlockPos source = new BlockPos(1, 1, 2);
        BlockPos westRedCable = new BlockPos(2, 1, 2);
        BlockPos eastRedCable = new BlockPos(4, 1, 2);
        BlockPos exporter = new BlockPos(5, 1, 2);

        scalar(helper, source, 7.0);
        dyedCable(helper, westRedCable, DyeColor.RED);
        dyedCable(helper, center, DyeColor.RED);
        helper.<ColoredCableBlockEntity>getBlockEntity(center).addColor(DyeColor.BLUE);
        dyedCable(helper, center.north(), DyeColor.BLUE);
        dyedCable(helper, center.south(), DyeColor.BLUE);
        dyedCable(helper, eastRedCable, DyeColor.RED);
        helper.setBlock(exporter, ModBlocks.REDSTONE_EXPORTER);
        PinCableBlock.updateCableAndNeighbors(helper.getLevel(), helper.absolutePos(center));

        double value = WorldRuntimeEvaluator.scalarInput(helper.getLevel(), helper.absolutePos(exporter), "value");

        helper.assertValueEqual(value, 7.0, "red input should stay connected to red output through a dyed junction");
        helper.succeed();
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE, timeoutTicks = 100)
    public void scannerDoesNotConnectAcrossDifferentColorsInDyedJunction(GameTestHelper helper) {
        BlockPos origin = new BlockPos(2, 0, 2);
        BlockPos center = new BlockPos(3, 1, 2);
        BlockPos source = new BlockPos(1, 1, 2);
        BlockPos redCable = new BlockPos(2, 1, 2);
        BlockPos blueCable = new BlockPos(4, 1, 2);
        BlockPos output = new BlockPos(5, 1, 2);

        helper.setBlock(origin, ModBlocks.CIRCUIT_ORIGIN);
        scalar(helper, source, 7.0);
        dyedCable(helper, redCable, DyeColor.RED);
        dyedCable(helper, center, DyeColor.RED);
        helper.<ColoredCableBlockEntity>getBlockEntity(center).addColor(DyeColor.BLUE);
        dyedCable(helper, blueCable, DyeColor.BLUE);
        externalPort(helper, output, true);
        PinCableBlock.updateCableAndNeighbors(helper.getLevel(), helper.absolutePos(center));

        RomProgram program = requireProgram(helper, WorldCircuitScanner.scan(helper.getLevel(), helper.absolutePos(origin)));
        double value = io.github.kisaragieffective.recomposedlogic.logic.CircuitEvaluator
                .evaluate(program, Map.of())
                .get(program.outputs().getFirst())
                .asScalar();

        helper.assertValueEqual(value, 0.0, "scanner must not connect red source through a dyed junction into a blue cable");
        helper.succeed();
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE, timeoutTicks = 100)
    public void scannerRejectsCombinationalCableLoop(GameTestHelper helper) {
        BlockPos origin = new BlockPos(2, 0, 2);
        BlockPos add = new BlockPos(3, 1, 3);

        helper.setBlock(origin, ModBlocks.CIRCUIT_ORIGIN);
        helper.setBlock(add, ModBlocks.SCALAR_ADD);
        scalar(helper, new BlockPos(3, 1, 2), 0.0);
        helper.setBlock(new BlockPos(2, 1, 3), ModBlocks.PIN_CABLE);
        helper.setBlock(new BlockPos(2, 1, 4), ModBlocks.PIN_CABLE);
        helper.setBlock(new BlockPos(3, 1, 4), ModBlocks.PIN_CABLE);
        helper.setBlock(new BlockPos(4, 1, 4), ModBlocks.PIN_CABLE);
        helper.setBlock(new BlockPos(4, 1, 3), ModBlocks.PIN_CABLE);
        externalPort(helper, new BlockPos(5, 1, 3), true);

        CompileResult result = WorldCircuitScanner.scan(helper.getLevel(), helper.absolutePos(origin));

        helper.assertTrue(!result.success(), "cable loop should fail compilation");
        helper.assertTrue(result.diagnostics().stream().anyMatch(diagnostic -> diagnostic.contains("Cycle detected")),
                String.join(", ", result.diagnostics()));
        helper.succeed();
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE, timeoutTicks = 100)
    public void scannerRejectsMultipleDriversOnOneInputNetwork(GameTestHelper helper) {
        BlockPos origin = new BlockPos(2, 0, 2);
        BlockPos inputCable = new BlockPos(2, 1, 3);
        BlockPos add = new BlockPos(3, 1, 3);

        helper.setBlock(origin, ModBlocks.CIRCUIT_ORIGIN);
        scalar(helper, inputCable.below(), 7.0);
        scalar(helper, inputCable.above(), 9.0);
        helper.setBlock(inputCable, ModBlocks.PIN_CABLE);
        helper.setBlock(add, ModBlocks.SCALAR_ADD);
        scalar(helper, new BlockPos(3, 1, 2), 1.0);
        helper.setBlock(new BlockPos(4, 1, 3), ModBlocks.PIN_CABLE);
        externalPort(helper, new BlockPos(5, 1, 3), true);

        CompileResult result = WorldCircuitScanner.scan(helper.getLevel(), helper.absolutePos(origin));

        helper.assertTrue(!result.success(), "multiple drivers should fail compilation");
        helper.assertTrue(result.diagnostics().stream().anyMatch(diagnostic -> diagnostic.contains("Multiple drivers")),
                String.join(", ", result.diagnostics()));
        helper.succeed();
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE, timeoutTicks = 100)
    public void runtimeCableLoopOutsideCompilationReturnsZeroInsteadOfFailing(GameTestHelper helper) {
        BlockPos add = new BlockPos(3, 1, 3);
        BlockPos exporter = new BlockPos(4, 1, 3);

        helper.setBlock(add, ModBlocks.SCALAR_ADD);
        scalar(helper, new BlockPos(3, 1, 2), 0.0);
        helper.setBlock(new BlockPos(2, 1, 3), ModBlocks.PIN_CABLE);
        helper.setBlock(new BlockPos(2, 1, 4), ModBlocks.PIN_CABLE);
        helper.setBlock(new BlockPos(3, 1, 4), ModBlocks.PIN_CABLE);
        helper.setBlock(new BlockPos(4, 1, 4), ModBlocks.PIN_CABLE);
        helper.setBlock(exporter, ModBlocks.REDSTONE_EXPORTER);

        double value = WorldRuntimeEvaluator.scalarInput(helper.getLevel(), helper.absolutePos(exporter), "value");

        helper.assertValueEqual(value, 0.0, "runtime cable loop outside compilation should evaluate through visited guard");
        helper.succeed();
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE, timeoutTicks = 100)
    public void runtimeMultipleDriversOutsideCompilationUseFirstNonZeroCableValue(GameTestHelper helper) {
        BlockPos cable = new BlockPos(2, 1, 1);
        BlockPos exporter = new BlockPos(3, 1, 1);

        scalar(helper, cable.below(), 7.0);
        scalar(helper, cable.above(), 9.0);
        helper.setBlock(cable, ModBlocks.PIN_CABLE);
        helper.setBlock(exporter, ModBlocks.REDSTONE_EXPORTER);

        double value = WorldRuntimeEvaluator.scalarInput(helper.getLevel(), helper.absolutePos(exporter), "value");

        helper.assertValueEqual(value, 7.0, "runtime cable lookup should keep its first non-zero value behavior");
        helper.succeed();
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE, timeoutTicks = 100)
    public void undyedCableConnectsOnlyToSingleColoredCable(GameTestHelper helper) {
        BlockPos undyed = new BlockPos(2, 1, 2);
        BlockPos dyed = undyed.east();

        helper.setBlock(undyed, ModBlocks.PIN_CABLE);
        dyedCable(helper, dyed, DyeColor.RED);
        PinCableBlock.updateCableAndNeighbors(helper.getLevel(), helper.absolutePos(undyed));

        helper.assertTrue(PinCableBlock.cableCompatible(helper.getLevel(), helper.absolutePos(undyed), helper.absolutePos(dyed)),
                "undyed cable should connect to a single colored cable");

        helper.<ColoredCableBlockEntity>getBlockEntity(dyed).addColor(DyeColor.BLUE);
        PinCableBlock.updateCableAndNeighbors(helper.getLevel(), helper.absolutePos(dyed));

        helper.assertTrue(!PinCableBlock.cableCompatible(helper.getLevel(), helper.absolutePos(undyed), helper.absolutePos(dyed)),
                "undyed cable must not connect to a multi-colored cable");
        helper.succeed();
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE, timeoutTicks = 100)
    public void singleColoredCableConnectsToUndyedJunction(GameTestHelper helper) {
        BlockPos junction = new BlockPos(2, 1, 2);
        BlockPos dyed = junction.east();

        helper.setBlock(junction, ModBlocks.PIN_CABLE);
        helper.setBlock(junction.west(), ModBlocks.PIN_CABLE);
        helper.setBlock(junction.north(), ModBlocks.PIN_CABLE);
        helper.assertBlockPresent(ModBlocks.UNDYED_INTERNAL_JUNCTION, junction);

        dyedCable(helper, dyed, DyeColor.RED);
        PinCableBlock.updateCableAndNeighbors(helper.getLevel(), helper.absolutePos(dyed));

        helper.assertTrue(PinCableBlock.connectionDirections(helper.getLevel(), helper.absolutePos(dyed), DyeColor.RED).contains(Direction.WEST),
                "a single colored cable should connect to an undyed junction");
        helper.succeed();
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE, timeoutTicks = 100)
    public void multiColorCablesConnectOnlyThroughSharedColors(GameTestHelper helper) {
        BlockPos first = new BlockPos(2, 1, 2);
        BlockPos second = first.east();

        dyedCable(helper, first, DyeColor.RED);
        helper.<ColoredCableBlockEntity>getBlockEntity(first).addColor(DyeColor.BLUE);
        dyedCable(helper, second, DyeColor.BLUE);
        helper.<ColoredCableBlockEntity>getBlockEntity(second).addColor(DyeColor.GREEN);
        PinCableBlock.updateCableAndNeighbors(helper.getLevel(), helper.absolutePos(first));
        PinCableBlock.updateCableAndNeighbors(helper.getLevel(), helper.absolutePos(second));

        helper.assertTrue(PinCableBlock.connectionDirections(helper.getLevel(), helper.absolutePos(first), DyeColor.BLUE).contains(Direction.EAST),
                "multi-color cables should connect through a shared color");
        helper.assertTrue(!PinCableBlock.connectionDirections(helper.getLevel(), helper.absolutePos(first), DyeColor.RED).contains(Direction.EAST),
                "multi-color cables must not connect through an unshared color");
        helper.succeed();
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE, timeoutTicks = 100)
    public void adjacentDyedJunctionsConnectEachSharedColor(GameTestHelper helper) {
        BlockPos first = new BlockPos(2, 1, 2);
        BlockPos second = first.east();

        dyedCable(helper, first, DyeColor.RED);
        dyedCable(helper, first.west(), DyeColor.RED);
        dyedCable(helper, first.north(), DyeColor.RED);
        dyedCable(helper, second, DyeColor.RED);
        dyedCable(helper, second.east(), DyeColor.RED);
        dyedCable(helper, second.south(), DyeColor.RED);
        PinCableBlock.updateCableAndNeighbors(helper.getLevel(), helper.absolutePos(first));
        PinCableBlock.updateCableAndNeighbors(helper.getLevel(), helper.absolutePos(second));

        helper.assertBlockPresent(ModBlocks.DYED_INTERNAL_JUNCTION, first);
        helper.assertBlockPresent(ModBlocks.DYED_INTERNAL_JUNCTION, second);
        helper.assertTrue(PinCableBlock.connectionDirections(helper.getLevel(), helper.absolutePos(first), DyeColor.RED).contains(Direction.EAST),
                "the first colored junction should connect to the adjacent colored junction");
        helper.assertTrue(PinCableBlock.connectionDirections(helper.getLevel(), helper.absolutePos(second), DyeColor.RED).contains(Direction.WEST),
                "the second colored junction should connect to the adjacent colored junction");
        helper.succeed();
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE, timeoutTicks = 100)
    public void dyedJunctionRetainsAdditionalColorFromAdjacentCable(GameTestHelper helper) {
        BlockPos junction = new BlockPos(2, 1, 2);
        BlockPos source = junction.west();

        dyedCable(helper, junction, DyeColor.RED);
        dyedCable(helper, source, DyeColor.RED);
        dyedCable(helper, junction.north(), DyeColor.RED);
        PinCableBlock.updateCableAndNeighbors(helper.getLevel(), helper.absolutePos(junction));
        helper.assertBlockPresent(ModBlocks.DYED_INTERNAL_JUNCTION, junction);

        helper.<ColoredCableBlockEntity>getBlockEntity(source).addColor(DyeColor.BLUE);
        PinCableBlock.updateCableAndNeighbors(helper.getLevel(), helper.absolutePos(source));

        ColoredCableBlockEntity coloredJunction = helper.getBlockEntity(junction);
        helper.assertTrue(coloredJunction.colors().contains(DyeColor.BLUE),
                "a colored junction should retain colors offered by an adjacent colored cable");
        helper.assertTrue(PinCableBlock.connectionDirections(helper.getLevel(), helper.absolutePos(junction), DyeColor.BLUE).contains(Direction.WEST),
                "the retained color should connect back to the adjacent colored cable");
        helper.succeed();
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE, timeoutTicks = 100)
    public void newlyCreatedDyedJunctionCapturesAdjacentCableColors(GameTestHelper helper) {
        BlockPos junction = new BlockPos(2, 1, 2);
        BlockPos source = junction.west();

        dyedCable(helper, source, DyeColor.RED);
        helper.<ColoredCableBlockEntity>getBlockEntity(source).addColor(DyeColor.BLUE);
        dyedCable(helper, junction, DyeColor.RED);
        dyedCable(helper, junction.north(), DyeColor.RED);
        PinCableBlock.updateCableAndNeighbors(helper.getLevel(), helper.absolutePos(junction));

        helper.assertBlockPresent(ModBlocks.DYED_INTERNAL_JUNCTION, junction);
        ColoredCableBlockEntity coloredJunction = helper.getBlockEntity(junction);
        helper.assertTrue(coloredJunction.colors().contains(DyeColor.BLUE),
                "a newly created colored junction should capture all colors from an adjacent colored cable");
        helper.assertTrue(PinCableBlock.connectionDirections(helper.getLevel(), helper.absolutePos(junction), DyeColor.BLUE).contains(Direction.WEST),
                "the captured color should connect to the adjacent colored cable");
        helper.succeed();
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE, timeoutTicks = 100)
    public void junctionReturnsToStraightCableAfterBranchRemoval(GameTestHelper helper) {
        BlockPos center = new BlockPos(2, 1, 2);

        helper.setBlock(center, ModBlocks.PIN_CABLE);
        helper.setBlock(center.west(), ModBlocks.PIN_CABLE);
        helper.setBlock(center.north(), ModBlocks.PIN_CABLE);
        helper.assertBlockPresent(ModBlocks.UNDYED_INTERNAL_JUNCTION, center);

        helper.setBlock(center.north(), Blocks.AIR);
        PinCableBlock.updateCableAndNeighbors(helper.getLevel(), helper.absolutePos(center));

        helper.assertBlockPresent(ModBlocks.PIN_CABLE, center);
        helper.succeed();
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE, timeoutTicks = 100)
    public void duplicateColorDoesNotPlaceOrConsumeOnMultiColorCable(GameTestHelper helper) {
        BlockPos cable = new BlockPos(2, 1, 2);
        dyedCable(helper, cable, DyeColor.RED);
        helper.<ColoredCableBlockEntity>getBlockEntity(cable).addColor(DyeColor.BLUE);
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.getAbilities().instabuild = false;
        ItemStack stack = new ItemStack(ModItems.DYED_PIN_CABLES.get(DyeColor.RED), 2);
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);

        BlockPos absoluteCable = helper.absolutePos(cable);
        ModItems.DYED_PIN_CABLES.get(DyeColor.RED).useOn(new net.minecraft.world.item.context.UseOnContext(
                player,
                InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(absoluteCable), Direction.EAST, absoluteCable, false)
        ));

        helper.assertValueEqual(player.getItemInHand(InteractionHand.MAIN_HAND).getCount(), 2,
                "placing a duplicate color on a multi-color cable must not consume an item");
        helper.assertBlockPresent(Blocks.AIR, cable.east());
        helper.succeed();
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE, timeoutTicks = 100)
    public void duplicateColorExtendsSingleColorCable(GameTestHelper helper) {
        BlockPos cable = new BlockPos(2, 1, 2);
        dyedCable(helper, cable, DyeColor.RED);
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.getAbilities().instabuild = false;
        ItemStack stack = new ItemStack(ModItems.DYED_PIN_CABLES.get(DyeColor.RED));
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        BlockPos absoluteCable = helper.absolutePos(cable);

        ModItems.DYED_PIN_CABLES.get(DyeColor.RED).useOn(new net.minecraft.world.item.context.UseOnContext(
                player,
                InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(absoluteCable), Direction.EAST, absoluteCable, false)
        ));

        helper.assertBlockPresent(ModBlocks.DYED_PIN_CABLE, cable.east());
        helper.assertValueEqual(player.getItemInHand(InteractionHand.MAIN_HAND).getCount(), 0,
                "extending a single-color cable should consume the placed cable item");
        helper.succeed();
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE, timeoutTicks = 100)
    public void dyedCableCannotBePlacedOnUndyedCable(GameTestHelper helper) {
        BlockPos cable = new BlockPos(2, 1, 2);
        helper.setBlock(cable, ModBlocks.PIN_CABLE);
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        ItemStack stack = new ItemStack(ModItems.DYED_PIN_CABLES.get(DyeColor.RED));
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        BlockPos absoluteCable = helper.absolutePos(cable);

        ModItems.DYED_PIN_CABLES.get(DyeColor.RED).useOn(new net.minecraft.world.item.context.UseOnContext(
                player,
                InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(absoluteCable), Direction.EAST, absoluteCable, false)
        ));

        helper.assertBlockPresent(ModBlocks.PIN_CABLE, cable);
        helper.assertValueEqual(player.getItemInHand(InteractionHand.MAIN_HAND).getCount(), 1,
                "placing a dyed cable onto an undyed cable must not consume an item");
        helper.succeed();
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE, timeoutTicks = 100)
    public void multiColorCableDropContentsIncludesEveryHeldColor(GameTestHelper helper) {
        BlockPos cable = new BlockPos(2, 1, 2);
        dyedCable(helper, cable, DyeColor.RED);
        helper.<ColoredCableBlockEntity>getBlockEntity(cable).addColor(DyeColor.BLUE);

        PinCableBlock.dropCableContents(helper.getLevel(), helper.absolutePos(cable), null);

        helper.assertItemEntityCountIs(ModItems.DYED_PIN_CABLES.get(DyeColor.RED), cable, 2.0, 1);
        helper.assertItemEntityCountIs(ModItems.DYED_PIN_CABLES.get(DyeColor.BLUE), cable, 2.0, 1);
        helper.succeed();
    }

    private static BlockPos addOneCircuit(GameTestHelper helper) {
        BlockPos origin = new BlockPos(2, 0, 2);
        helper.setBlock(origin, ModBlocks.CIRCUIT_ORIGIN);
        externalPort(helper, new BlockPos(1, 1, 3), false);
        helper.setBlock(new BlockPos(2, 1, 3), ModBlocks.SCALAR_ADD);
        scalar(helper, new BlockPos(2, 1, 2), 1.0);
        helper.setBlock(new BlockPos(3, 1, 3), ModBlocks.PIN_CABLE);
        helper.setBlock(new BlockPos(4, 1, 3), ModBlocks.PIN_CABLE);
        externalPort(helper, new BlockPos(5, 1, 3), true);
        return origin;
    }

    private static void scalar(GameTestHelper helper, BlockPos pos, double value) {
        helper.setBlock(pos, ModBlocks.SCALAR_CONSTANT);
        helper.<ScalarConstantBlockEntity>getBlockEntity(pos).scalarValue(value);
    }

    private static void externalPort(GameTestHelper helper, BlockPos pos, boolean output) {
        helper.setBlock(pos, ModBlocks.EXTERNAL_PORT);
        ExternalPortBlockEntity entity = helper.getBlockEntity(pos);
        if (entity.externalOutput() != output) {
            entity.toggleExternalDirection();
        }
    }

    private static void dyedCable(GameTestHelper helper, BlockPos pos, DyeColor color) {
        helper.setBlock(pos, ModBlocks.DYED_PIN_CABLE);
        helper.<ColoredCableBlockEntity>getBlockEntity(pos).singleColor(color);
        PinCableBlock.updateCableAndNeighbors(helper.getLevel(), helper.absolutePos(pos));
    }

    private static RomProgram compileAddOneProgram() {
        CircuitGraph graph = new CircuitGraph();
        graph.add(node("input", NodeKind.EXTERNAL_INPUT));
        CircuitNode constant = node("one", NodeKind.SCALAR_CONSTANT);
        constant.scalarValue(1.0);
        graph.add(constant);
        CircuitNode add = node("add", NodeKind.SCALAR_ADD);
        add.connect("left", new PortRef("input", "value"));
        add.connect("right", new PortRef("one", "value"));
        graph.add(add);
        CircuitNode output = node("output", NodeKind.EXTERNAL_OUTPUT);
        output.connect("value", new PortRef("add", "value"));
        graph.add(output);
        return CircuitCompiler.compile(graph, 4).program();
    }

    private static CircuitNode node(String id, NodeKind kind) {
        return new CircuitNode(id, kind);
    }

    private static RomProgram requireProgram(GameTestHelper helper, CompileResult result) {
        helper.assertTrue(result.success(), String.join(", ", result.diagnostics()));
        return result.program();
    }
}
