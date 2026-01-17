package com.dnocturne.afflictions.condition;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A condition composed of multiple sub-conditions with logical operators.
 *
 * <p>Supports AND, OR, and NOT operations for building complex conditions.</p>
 */
public final class CompositeCondition implements Condition {

    private final List<Condition> conditions;
    private final Operator operator;

    private CompositeCondition(@NotNull Operator operator, @NotNull List<Condition> conditions) {
        this.operator = operator;
        this.conditions = List.copyOf(conditions);
    }

    /**
     * Create a condition that is true only if ALL sub-conditions are true.
     *
     * @param conditions The conditions to combine
     * @return A composite AND condition
     */
    public static @NotNull Condition and(@NotNull Condition... conditions) {
        if (conditions.length == 0) {
            return Condition.ALWAYS;
        }
        if (conditions.length == 1) {
            return conditions[0];
        }
        return new CompositeCondition(Operator.AND, Arrays.asList(conditions));
    }

    /**
     * Create a condition that is true if ANY sub-condition is true.
     *
     * @param conditions The conditions to combine
     * @return A composite OR condition
     */
    public static @NotNull Condition or(@NotNull Condition... conditions) {
        if (conditions.length == 0) {
            return Condition.NEVER;
        }
        if (conditions.length == 1) {
            return conditions[0];
        }
        return new CompositeCondition(Operator.OR, Arrays.asList(conditions));
    }

    /**
     * Create a condition that negates the given condition.
     *
     * @param condition The condition to negate
     * @return A negated condition
     */
    public static @NotNull Condition not(@NotNull Condition condition) {
        // Double negation optimization
        if (condition instanceof CompositeCondition composite && composite.operator == Operator.NOT) {
            return composite.conditions.get(0);
        }
        return new CompositeCondition(Operator.NOT, List.of(condition));
    }

    @Override
    public boolean test(@NotNull Player player) {
        return switch (operator) {
            case AND -> testAnd(player);
            case OR -> testOr(player);
            case NOT -> !conditions.get(0).test(player);
        };
    }

    /**
     * Test AND condition using for-loop (avoids stream allocation overhead).
     */
    private boolean testAnd(Player player) {
        for (Condition condition : conditions) {
            if (!condition.test(player)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Test OR condition using for-loop (avoids stream allocation overhead).
     */
    private boolean testOr(Player player) {
        for (Condition condition : conditions) {
            if (condition.test(player)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public @NotNull String describe() {
        return switch (operator) {
            case AND -> "(" + conditions.stream()
                    .map(Condition::describe)
                    .collect(Collectors.joining(" AND ")) + ")";
            case OR -> "(" + conditions.stream()
                    .map(Condition::describe)
                    .collect(Collectors.joining(" OR ")) + ")";
            case NOT -> "NOT " + conditions.get(0).describe();
        };
    }

    /**
     * Logical operators for combining conditions.
     */
    private enum Operator {
        AND,
        OR,
        NOT
    }
}
