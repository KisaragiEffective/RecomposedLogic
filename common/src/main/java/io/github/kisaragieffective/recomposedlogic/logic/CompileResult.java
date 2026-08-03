package io.github.kisaragieffective.recomposedlogic.logic;

import java.util.List;

public record CompileResult(boolean success, RomProgram program, List<String> diagnostics) {
    public static CompileResult fail(List<String> diagnostics) {
        return new CompileResult(false, null, List.copyOf(diagnostics));
    }

    public static CompileResult ok(RomProgram program, List<String> diagnostics) {
        return new CompileResult(true, program, List.copyOf(diagnostics));
    }
}
