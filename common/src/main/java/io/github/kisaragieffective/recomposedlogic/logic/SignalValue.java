package io.github.kisaragieffective.recomposedlogic.logic;

public record SignalValue(SignalType type, double scalar, boolean bool) {
    public static final SignalValue FALSE = bool(false);
    public static final SignalValue TRUE = bool(true);
    public static final SignalValue ZERO = scalar(0.0);

    public static SignalValue scalar(double value) {
        if (!Double.isFinite(value)) {
            value = 0.0;
        }
        if (value == 0.0) {
            value = 0.0;
        }
        return new SignalValue(SignalType.SCALAR, value, false);
    }

    public static SignalValue bool(boolean value) {
        return new SignalValue(SignalType.BOOLEAN, 0.0, value);
    }

    public double asScalar() {
        if (type != SignalType.SCALAR) {
            throw new IllegalStateException("Expected scalar, got " + type);
        }
        return scalar;
    }

    public boolean asBoolean() {
        if (type != SignalType.BOOLEAN) {
            throw new IllegalStateException("Expected boolean, got " + type);
        }
        return bool;
    }
}
