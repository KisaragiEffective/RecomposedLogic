package io.github.kisaragieffective.recomposedlogic.world;

import io.github.kisaragieffective.recomposedlogic.world.block.BooleanAndBlock;
import io.github.kisaragieffective.recomposedlogic.world.block.BooleanConditionalBlock;
import io.github.kisaragieffective.recomposedlogic.world.block.BooleanNotBlock;
import io.github.kisaragieffective.recomposedlogic.world.block.ExternalPortBlock;
import io.github.kisaragieffective.recomposedlogic.world.block.RedstoneExporterBlock;
import io.github.kisaragieffective.recomposedlogic.world.block.RedstoneImporterBlock;
import io.github.kisaragieffective.recomposedlogic.world.block.RomPoweringGateBlock;
import io.github.kisaragieffective.recomposedlogic.world.block.ScalarAddBlock;
import io.github.kisaragieffective.recomposedlogic.world.block.ScalarComparatorBlock;
import io.github.kisaragieffective.recomposedlogic.world.block.ScalarConstantBlock;
import net.minecraft.core.Direction;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PortsTest {
    @Test
    void scalarConstantAndRedstoneImporterOutputOnEverySide() {
        assertAllOutputs(ScalarConstantBlock.PORTS);
        assertAllOutputs(RedstoneImporterBlock.PORTS);
    }

    @Test
    void scalarBinaryGatesUseWestAndNorthInputsAndEastOutput() {
        assertPort(ScalarAddBlock.PORTS, "left", Direction.WEST, false);
        assertPort(ScalarAddBlock.PORTS, "right", Direction.NORTH, false);
        assertPort(ScalarAddBlock.PORTS, "value", Direction.EAST, true);

        assertPort(ScalarComparatorBlock.PORTS, "left", Direction.WEST, false);
        assertPort(ScalarComparatorBlock.PORTS, "right", Direction.NORTH, false);
        assertPort(ScalarComparatorBlock.PORTS, "value", Direction.EAST, true);
    }

    @Test
    void booleanGatesHaveStablePorts() {
        assertPort(BooleanAndBlock.PORTS, "left", Direction.WEST, false);
        assertPort(BooleanAndBlock.PORTS, "right", Direction.NORTH, false);
        assertPort(BooleanAndBlock.PORTS, "value", Direction.EAST, true);

        assertPort(BooleanNotBlock.PORTS, "value", Direction.WEST, false);
        assertPort(BooleanNotBlock.PORTS, "value", Direction.EAST, true);
    }

    @Test
    void conditionalUsesConditionWestTrueNorthFalseSouthAndEastOutput() {
        List<PortSpec> specs = BooleanConditionalBlock.PORTS;
        assertPort(specs, "condition", Direction.WEST, false);
        assertPort(specs, "ifTrue", Direction.NORTH, false);
        assertPort(specs, "ifFalse", Direction.SOUTH, false);
        assertPort(specs, "value", Direction.EAST, true);
    }

    @Test
    void externalRuntimeBlocksKeepPoCFixedDirections() {
        assertPort(RedstoneExporterBlock.PORTS, "value", Direction.WEST, false);
        assertPort(ExternalPortBlock.PORTS, "value", Direction.EAST, true);
        assertPort(RomPoweringGateBlock.PORTS, "input0", Direction.WEST, false);
        assertPort(RomPoweringGateBlock.PORTS, "value", Direction.EAST, true);
    }

    private static void assertAllOutputs(List<PortSpec> specs) {
        assertEquals(6, specs.size());
        for (Direction direction : Direction.values()) {
            assertPort(specs, "value", direction, true);
        }
    }

    private static void assertPort(List<PortSpec> specs, String name, Direction direction, boolean output) {
        assertTrue(specs.stream().anyMatch(spec -> spec.name().equals(name) && spec.direction() == direction && spec.output() == output),
                () -> "Missing port " + name + " " + direction + " output=" + output + " in " + specs);
    }

}
