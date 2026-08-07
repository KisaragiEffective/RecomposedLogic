package io.github.kisaragieffective.recomposedlogic.logic;

public sealed interface SignalValue permits BooleanSignal, ScalarSignal {
    SignalType getType();

    default double asScalar() {
        if (this instanceof ScalarSignal scalar) {
            return scalar.getValue();
        }
        throw new IllegalStateException("Expected scalar, got " + getType());
    }

    default boolean asBoolean() {
        if (this instanceof BooleanSignal bool) {
            return bool.getValue();
        }
        throw new IllegalStateException("Expected boolean, got " + getType());
    }
}
