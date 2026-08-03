package io.github.kisaragieffective.recomposedlogic.logic;

public enum ComparatorOperator {
    EQUALS,
    NOT_EQUALS,
    GREATER_THAN,
    GREATER_OR_EQUAL,
    LESS_THAN,
    LESS_OR_EQUAL;

    public boolean test(double left, double right) {
        return switch (this) {
            case EQUALS -> Double.compare(left, right) == 0;
            case NOT_EQUALS -> Double.compare(left, right) != 0;
            case GREATER_THAN -> left > right;
            case GREATER_OR_EQUAL -> left >= right;
            case LESS_THAN -> left < right;
            case LESS_OR_EQUAL -> left <= right;
        };
    }

    public ComparatorOperator next() {
        ComparatorOperator[] values = values();
        return values[(ordinal() + 1) % values.length];
    }
}
