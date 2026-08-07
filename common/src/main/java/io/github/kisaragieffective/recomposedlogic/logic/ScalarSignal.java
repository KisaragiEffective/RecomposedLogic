package io.github.kisaragieffective.recomposedlogic.logic;

public final class ScalarSignal implements SignalValue, TypedSignalValue<Double> {
    public static final ScalarSignal ZERO = new ScalarSignal(0.0);
    private final double raw;

    public ScalarSignal(double raw) {
        if (!Double.isFinite(raw)) {
            raw = 0.0;
        }
        if (raw == 0.0) {
            raw = 0.0;
        }

        this.raw = raw;
    }

    @Override
    public Double getValue() {
        return raw;
    }

    @Override
    public SignalType getType() {
        return SignalType.SCALAR;
    }

    @Override
    public boolean equals(Object obj) {
        return obj == this || obj instanceof ScalarSignal other && Double.compare(raw, other.raw) == 0;
    }

    @Override
    public int hashCode() {
        return Double.hashCode(raw);
    }

    @Override
    public String toString() {
        return "ScalarSignal(value = " + getValue() + ")";
    }
}
