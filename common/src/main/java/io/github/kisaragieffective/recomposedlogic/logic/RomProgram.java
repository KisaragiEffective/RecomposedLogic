package io.github.kisaragieffective.recomposedlogic.logic;

import java.util.List;

public record RomProgram(byte[] wasm, List<IrInstruction> instructions, List<String> inputs, List<String> outputs, int scannedVolume) {
}
