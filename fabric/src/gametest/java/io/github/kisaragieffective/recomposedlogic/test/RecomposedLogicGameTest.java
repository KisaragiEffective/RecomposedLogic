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
import io.github.kisaragieffective.recomposedlogic.world.WorldCircuitScanner;
import io.github.kisaragieffective.recomposedlogic.world.WorldRuntimeEvaluator;
import io.github.kisaragieffective.recomposedlogic.world.entity.ExternalPortBlockEntity;
import io.github.kisaragieffective.recomposedlogic.world.entity.RomPoweringGateBlockEntity;
import io.github.kisaragieffective.recomposedlogic.world.entity.ScalarConstantBlockEntity;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
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
