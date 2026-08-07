package io.github.kisaragieffective.recomposedlogic.logic;

public final class BooleanSignal implements SignalValue, TypedSignalValue<Boolean> {
    private final boolean raw;

    private BooleanSignal(boolean raw) {
        this.raw = raw;
    }

    @Override
    public Boolean getValue() {
        return raw;
    }

    @Override
    public SignalType getType() {
        return SignalType.BOOLEAN;
    }

    public static final BooleanSignal TRUE = new BooleanSignal(true);
    public static final BooleanSignal FALSE = new BooleanSignal(false);

    public static BooleanSignal of(boolean raw) {
        return raw ? TRUE : FALSE;
    }

    @Override
    public boolean equals(Object obj) {
        return obj == this || obj instanceof BooleanSignal other && raw == other.raw;
    }

    @Override
    public int hashCode() {
        return getValue() ? 1 : 0;
    }

    @Override
    public String toString() {
        return "BooleanSignal(value = " + getValue() + ")";
    }

}
