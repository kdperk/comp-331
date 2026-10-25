package edu.comp331;

public class Calculator {
    private Calculator() {
        // Private constructor to prevent instantiation
    }

    public static boolean isValidExpression(String input) {
        String expression = normalizeExpression(input);
        if (expression.isEmpty() || expression.length() > 100) {
            return false;
        }

        return new ExpressionParser(expression).isValid();
    }

    public static double solve(String input) {
        String expression = normalizeExpression(input);
        if (expression.isEmpty()) {
            throw new IllegalArgumentException("Expression cannot be empty");
        }
        if (expression.length() > 100) {
            throw new IllegalArgumentException("Expression cannot exceed 100 characters");
        }

        return new ExpressionParser(expression).solve();
    }

    private static String normalizeExpression(String input) {
        return input == null ? "" : input.replaceAll("\\s+", "");
    }

    public static double add(double firstOperand, double secondOperand) {
        return firstOperand + secondOperand;
    }

    public static double subtract(double firstOperand, double secondOperand) {
        return firstOperand - secondOperand;
    }

    public static double multiply(double firstOperand, double secondOperand) {
        return firstOperand * secondOperand;
    }

    public static double divide(double firstOperand, double secondOperand) {
        if (secondOperand == 0.0) {
            throw new ArithmeticException("Cannot divide by zero");
        }

        return firstOperand / secondOperand;
    }

    public static double abs(double operand) {
        return Math.abs(operand);
    }

    public static double negate(double operand) {
        return -operand;
    }

    public static double pow(double base, double exponent) {
        return Math.pow(base, exponent);
    }

    public static double mod(double firstOperand, double secondOperand) {
        if (secondOperand == 0.0) {
            throw new ArithmeticException("Cannot take modulo by zero");
        }

        return firstOperand % secondOperand;
    }
}
