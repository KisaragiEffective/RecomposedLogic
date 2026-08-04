package io.github.kisaragieffective.recomposedlogic.logic;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CircuitCompilerEvaluatorTest {
    @Test
    void scalarAddCanBeCompiledAndEvaluatedThroughExternalPorts() {
        RomProgram program = compileAddOneProgram();

        Map<String, SignalValue> values = CircuitEvaluator.evaluate(program, Map.of("input", SignalValue.scalar(5.0)));

        assertEquals(6.0, values.get("output").asScalar());
        assertEquals(4, program.instructions().size());
        assertEquals("input", program.inputs().getFirst());
        assertEquals("output", program.outputs().getFirst());
    }

    @Test
    void comparatorAndBooleanGatesTypeCheckAndEvaluate() {
        CircuitGraph graph = new CircuitGraph();
        graph.add(externalInput("left"));
        graph.add(constant("limit", 4.0));

        CircuitNode comparator = node("gte", NodeKind.SCALAR_COMPARATOR);
        comparator.comparatorOperator(ComparatorOperator.GREATER_OR_EQUAL);
        comparator.connect("left", new PortRef("left", "value"));
        comparator.connect("right", new PortRef("limit", "value"));
        graph.add(comparator);

        CircuitNode not = node("not", NodeKind.BOOLEAN_NOT);
        not.connect("value", new PortRef("gte", "value"));
        graph.add(not);

        CircuitNode output = node("out", NodeKind.EXTERNAL_OUTPUT);
        output.connect("value", new PortRef("not", "value"));
        graph.add(output);

        CompileResult result = CircuitCompiler.compile(graph, 4);
        assertFalse(result.success());
        assertTrue(result.diagnostics().getFirst().contains("Type mismatch"));
    }

    @Test
    void conditionalConvertsBooleanChoiceToScalarOutput() {
        CircuitGraph graph = new CircuitGraph();
        graph.add(externalInput("input"));
        graph.add(constant("limit", 1.0));
        graph.add(constant("trueValue", 8.0));
        graph.add(constant("falseValue", 3.0));

        CircuitNode comparator = node("condition", NodeKind.SCALAR_COMPARATOR);
        comparator.comparatorOperator(ComparatorOperator.GREATER_OR_EQUAL);
        comparator.connect("left", new PortRef("input", "value"));
        comparator.connect("right", new PortRef("limit", "value"));
        graph.add(comparator);

        CircuitNode conditional = node("conditional", NodeKind.BOOLEAN_CONDITIONAL);
        conditional.connect("condition", new PortRef("condition", "value"));
        conditional.connect("ifTrue", new PortRef("trueValue", "value"));
        conditional.connect("ifFalse", new PortRef("falseValue", "value"));
        graph.add(conditional);

        CircuitNode output = node("output", NodeKind.EXTERNAL_OUTPUT);
        output.connect("value", new PortRef("conditional", "value"));
        graph.add(output);

        RomProgram program = requireProgram(CircuitCompiler.compile(graph, 5));

        assertEquals(8.0, CircuitEvaluator.evaluate(program, Map.of("input", SignalValue.scalar(1.0))).get("output").asScalar());
        assertEquals(3.0, CircuitEvaluator.evaluate(program, Map.of("input", SignalValue.scalar(0.0))).get("output").asScalar());
    }

    @Test
    void circuitsWithoutOutputsAreRejected() {
        CircuitGraph graph = new CircuitGraph();
        graph.add(externalInput("input"));
        graph.add(constant("one", 1.0));
        CircuitNode add = node("add", NodeKind.SCALAR_ADD);
        add.connect("left", new PortRef("input", "value"));
        add.connect("right", new PortRef("one", "value"));
        graph.add(add);

        CompileResult result = CircuitCompiler.compile(graph, 3);

        assertFalse(result.success());
        assertEquals("Circuit has no outputs", result.diagnostics().getFirst());
    }

    @Test
    void compiledRomCanBeUsedAsANodeAndCompiledAgain() {
        RomProgram addOne = compileAddOneProgram();

        CircuitGraph graph = new CircuitGraph();
        graph.add(externalInput("input"));
        CircuitNode rom = node("romA", NodeKind.ROM);
        rom.romProgram(addOne);
        rom.connect("input0", new PortRef("input", "value"));
        graph.add(rom);
        graph.add(constant("one", 1.0));

        CircuitNode add = node("addAfterRom", NodeKind.SCALAR_ADD);
        add.connect("left", new PortRef("romA", "value"));
        add.connect("right", new PortRef("one", "value"));
        graph.add(add);

        CircuitNode output = node("output", NodeKind.EXTERNAL_OUTPUT);
        output.connect("value", new PortRef("addAfterRom", "value"));
        graph.add(output);

        RomProgram addTwo = requireProgram(CircuitCompiler.compile(graph, 6));

        assertEquals(5.0, CircuitEvaluator.evaluate(addTwo, Map.of("input", SignalValue.scalar(3.0))).get("output").asScalar());
    }

    private static RomProgram compileAddOneProgram() {
        CircuitGraph graph = new CircuitGraph();
        graph.add(externalInput("input"));
        graph.add(constant("one", 1.0));
        CircuitNode add = node("add", NodeKind.SCALAR_ADD);
        add.connect("left", new PortRef("input", "value"));
        add.connect("right", new PortRef("one", "value"));
        graph.add(add);
        CircuitNode output = node("output", NodeKind.EXTERNAL_OUTPUT);
        output.connect("value", new PortRef("add", "value"));
        graph.add(output);
        return requireProgram(CircuitCompiler.compile(graph, 4));
    }

    private static CircuitNode externalInput(String id) {
        return node(id, NodeKind.EXTERNAL_INPUT);
    }

    private static CircuitNode constant(String id, double value) {
        CircuitNode node = node(id, NodeKind.SCALAR_CONSTANT);
        node.scalarValue(value);
        return node;
    }

    private static CircuitNode node(String id, NodeKind kind) {
        return new CircuitNode(id, kind);
    }

    private static RomProgram requireProgram(CompileResult result) {
        assertTrue(result.success(), () -> String.join(", ", result.diagnostics()));
        return result.program();
    }
}
