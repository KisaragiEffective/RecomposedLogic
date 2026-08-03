package io.github.kisaragieffective.recomposedlogic.logic;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class CircuitCompiler {
    private CircuitCompiler() {
    }

    public static CompileResult compile(CircuitGraph graph, int scannedVolume) {
        List<String> diagnostics = new ArrayList<>(graph.diagnostics());
        Map<String, SignalType> types = new HashMap<>();
        List<IrInstruction> ir = new ArrayList<>();
        Set<String> visiting = new HashSet<>();
        Set<String> visited = new HashSet<>();

        for (CircuitNode node : graph.nodes().values()) {
            emit(node, graph, types, ir, visiting, visited, diagnostics);
        }
        if (!diagnostics.isEmpty()) {
            return CompileResult.fail(diagnostics);
        }

        List<String> inputs = graph.nodes().values().stream()
                .filter(node -> node.kind() == NodeKind.EXTERNAL_INPUT)
                .map(CircuitNode::id)
                .toList();
        List<String> outputs = graph.nodes().values().stream()
                .filter(node -> node.kind() == NodeKind.EXTERNAL_OUTPUT || node.kind() == NodeKind.REDSTONE_EXPORTER)
                .map(CircuitNode::id)
                .toList();
        RomProgram program = new RomProgram(WasmEmitter.emit(ir, inputs, outputs), List.copyOf(ir), inputs, outputs, scannedVolume);
        return CompileResult.ok(program, diagnostics);
    }

    private static SignalType emit(CircuitNode node, CircuitGraph graph, Map<String, SignalType> types, List<IrInstruction> ir, Set<String> visiting, Set<String> visited, List<String> diagnostics) {
        if (types.containsKey(node.id())) {
            return types.get(node.id());
        }
        if (!visiting.add(node.id())) {
            diagnostics.add("Cycle detected at " + node.id());
            return SignalType.SCALAR;
        }

        SignalType output = switch (node.kind()) {
            case SCALAR_CONSTANT, SCALAR_ADD, BOOLEAN_CONDITIONAL, REDSTONE_IMPORTER, EXTERNAL_INPUT, ROM -> SignalType.SCALAR;
            case SCALAR_COMPARATOR, BOOLEAN_AND, BOOLEAN_NOT -> SignalType.BOOLEAN;
            case REDSTONE_EXPORTER, EXTERNAL_OUTPUT -> SignalType.SCALAR;
        };

        for (Map.Entry<String, PortRef> input : node.inputs().entrySet()) {
            CircuitNode source = graph.node(input.getValue().nodeId());
            if (source == null) {
                diagnostics.add("Missing source " + input.getValue().nodeId() + " for " + node.id() + "." + input.getKey());
                continue;
            }
            SignalType sourceType = emit(source, graph, types, ir, visiting, visited, diagnostics);
            SignalType expected = expectedInput(node.kind(), input.getKey());
            if (expected != sourceType) {
                diagnostics.add("Type mismatch at " + node.id() + "." + input.getKey() + ": expected " + expected + ", got " + sourceType);
            }
        }

        visiting.remove(node.id());
        types.put(node.id(), output);
        if (visited.add(node.id())) {
            List<String> inputs = node.inputs().entrySet().stream()
                    .map(entry -> entry.getKey() + "=" + entry.getValue().nodeId())
                    .toList();
            ir.add(new IrInstruction(node.id(), node.kind(), output, inputs, node.scalarValue(), node.comparatorOperator(), node.romProgram()));
        }
        return output;
    }

    private static SignalType expectedInput(NodeKind kind, String input) {
        return switch (kind) {
            case SCALAR_ADD, SCALAR_COMPARATOR, REDSTONE_EXPORTER, EXTERNAL_OUTPUT -> SignalType.SCALAR;
            case BOOLEAN_AND, BOOLEAN_NOT -> SignalType.BOOLEAN;
            case BOOLEAN_CONDITIONAL -> input.equals("condition") ? SignalType.BOOLEAN : SignalType.SCALAR;
            default -> SignalType.SCALAR;
        };
    }

    private static final class WasmEmitter {
        static byte[] emit(List<IrInstruction> ir, List<String> inputs, List<String> outputs) {
            StringBuilder payload = new StringBuilder();
            payload.append("inputs ").append(inputs).append('\n');
            payload.append("outputs ").append(outputs).append('\n');
            for (IrInstruction instruction : ir) {
                payload.append(instruction.kind()).append(' ').append(instruction.id()).append(' ').append(instruction.inputs()).append('\n');
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            out.writeBytes(new byte[]{0x00, 0x61, 0x73, 0x6d, 0x01, 0x00, 0x00, 0x00});
            section(out, 1, bytes(0x01, 0x60, 0x00, 0x00));
            section(out, 3, bytes(0x01, 0x00));
            section(out, 7, exportSection());
            section(out, 10, bytes(0x01, 0x02, 0x00, 0x0b));
            customSection(out, "recomposedlogic.ir", payload.toString().getBytes(StandardCharsets.UTF_8));
            return out.toByteArray();
        }

        private static byte[] exportSection() {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            writeU32(out, 1);
            writeName(out, "eval");
            out.write(0x00);
            writeU32(out, 0);
            return out.toByteArray();
        }

        private static void customSection(ByteArrayOutputStream module, String name, byte[] payload) {
            ByteArrayOutputStream section = new ByteArrayOutputStream();
            writeName(section, name);
            section.writeBytes(payload);
            section(module, 0, section.toByteArray());
        }

        private static void section(ByteArrayOutputStream module, int id, byte[] content) {
            module.write(id);
            writeU32(module, content.length);
            module.writeBytes(content);
        }

        private static void writeName(ByteArrayOutputStream out, String value) {
            byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
            writeU32(out, bytes.length);
            out.writeBytes(bytes);
        }

        private static byte[] bytes(int... values) {
            byte[] bytes = new byte[values.length];
            for (int i = 0; i < values.length; i++) {
                bytes[i] = (byte) values[i];
            }
            return bytes;
        }

        private static void writeU32(ByteArrayOutputStream out, int value) {
            do {
                int b = value & 0x7f;
                value >>>= 7;
                if (value != 0) {
                    b |= 0x80;
                }
                out.write(b);
            } while (value != 0);
        }
    }
}
