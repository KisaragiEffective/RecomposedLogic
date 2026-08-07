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

        Map<String, SignalValue> values = CircuitEvaluator.evaluate(program, Map.of("input", new ScalarSignal(5.0)));

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
    void scalarInputsRejectBooleanSources() {
        CircuitGraph graph = new CircuitGraph();
        graph.add(constant("one", 1.0));
        CircuitNode comparator = node("isOne", NodeKind.SCALAR_COMPARATOR);
        comparator.connect("left", new PortRef("one", "value"));
        comparator.connect("right", new PortRef("one", "value"));
        graph.add(comparator);

        CircuitNode add = node("add", NodeKind.SCALAR_ADD);
        add.connect("left", new PortRef("isOne", "value"));
        add.connect("right", new PortRef("one", "value"));
        graph.add(add);

        CircuitNode output = node("output", NodeKind.EXTERNAL_OUTPUT);
        output.connect("value", new PortRef("add", "value"));
        graph.add(output);

        CompileResult result = CircuitCompiler.compile(graph, 4);

        assertSingleDiagnostic(result, "Type mismatch at add.left: expected SCALAR, got BOOLEAN");
    }

    @Test
    void booleanInputsRejectScalarSources() {
        CircuitGraph graph = new CircuitGraph();
        graph.add(constant("one", 1.0));
        graph.add(constant("two", 2.0));

        CircuitNode and = node("and", NodeKind.BOOLEAN_AND);
        and.connect("left", new PortRef("one", "value"));
        and.connect("right", new PortRef("two", "value"));
        graph.add(and);

        CircuitNode conditional = node("conditional", NodeKind.BOOLEAN_CONDITIONAL);
        conditional.connect("condition", new PortRef("and", "value"));
        conditional.connect("ifTrue", new PortRef("one", "value"));
        conditional.connect("ifFalse", new PortRef("two", "value"));
        graph.add(conditional);

        CircuitNode output = node("output", NodeKind.EXTERNAL_OUTPUT);
        output.connect("value", new PortRef("conditional", "value"));
        graph.add(output);

        CompileResult result = CircuitCompiler.compile(graph, 5);

        assertFalse(result.success());
        assertEquals(2, result.diagnostics().size());
        assertTrue(result.diagnostics().contains("Type mismatch at and.left: expected BOOLEAN, got SCALAR"));
        assertTrue(result.diagnostics().contains("Type mismatch at and.right: expected BOOLEAN, got SCALAR"));
    }

    @Test
    void conditionalConditionRejectsScalarSource() {
        CircuitGraph graph = new CircuitGraph();
        graph.add(constant("one", 1.0));
        graph.add(constant("two", 2.0));

        CircuitNode conditional = node("conditional", NodeKind.BOOLEAN_CONDITIONAL);
        conditional.connect("condition", new PortRef("one", "value"));
        conditional.connect("ifTrue", new PortRef("one", "value"));
        conditional.connect("ifFalse", new PortRef("two", "value"));
        graph.add(conditional);

        CircuitNode output = node("output", NodeKind.EXTERNAL_OUTPUT);
        output.connect("value", new PortRef("conditional", "value"));
        graph.add(output);

        CompileResult result = CircuitCompiler.compile(graph, 4);

        assertSingleDiagnostic(result, "Type mismatch at conditional.condition: expected BOOLEAN, got SCALAR");
    }

    @Test
    void combinationalLoopsAreRejected() {
        CircuitGraph graph = new CircuitGraph();
        graph.add(constant("one", 1.0));

        CircuitNode first = node("first", NodeKind.SCALAR_ADD);
        first.connect("left", new PortRef("second", "value"));
        first.connect("right", new PortRef("one", "value"));
        graph.add(first);

        CircuitNode second = node("second", NodeKind.SCALAR_ADD);
        second.connect("left", new PortRef("first", "value"));
        second.connect("right", new PortRef("one", "value"));
        graph.add(second);

        CircuitNode output = node("output", NodeKind.EXTERNAL_OUTPUT);
        output.connect("value", new PortRef("first", "value"));
        graph.add(output);

        CompileResult result = CircuitCompiler.compile(graph, 4);

        assertFalse(result.success());
        assertTrue(result.diagnostics().contains("Cycle detected at first"));
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

        assertEquals(8.0, CircuitEvaluator.evaluate(program, Map.of("input", new ScalarSignal(1.0))).get("output").asScalar());
        assertEquals(3.0, CircuitEvaluator.evaluate(program, Map.of("input", new ScalarSignal(0.0))).get("output").asScalar());
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

        assertEquals(5.0, CircuitEvaluator.evaluate(addTwo, Map.of("input", new ScalarSignal(3.0))).get("output").asScalar());
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

    private static void assertSingleDiagnostic(CompileResult result, String diagnostic) {
        assertFalse(result.success());
        assertEquals(1, result.diagnostics().size());
        assertEquals(diagnostic, result.diagnostics().getFirst());
    }
}
