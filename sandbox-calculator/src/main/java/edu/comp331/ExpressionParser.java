package edu.comp331;

class ExpressionParser {
    private final String expression;
    private final boolean validateOnly;
    private int position;

    ExpressionParser(String expression) {
        this(expression, false);
    }

    private ExpressionParser(String expression, boolean validateOnly) {
        this.expression = expression;
        this.validateOnly = validateOnly;
    }

    boolean isValid() {
        try {
            new ExpressionParser(expression, true).parse();
            return true;
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }

    double solve() {
        return parse();
    }

    private double parse() {
        if (expression == null || expression.isEmpty()) {
            throw invalidExpression();
        }

        double result = parseExpression();
        if (position != expression.length()) {
            throw invalidExpression();
        }
        return result;
    }

    private double parseExpression() {
        double result = parseTerm();

        while (hasNext() && (current() == '+' || current() == '-')) {
            char operator = current();
            position++;
            double rightOperand = parseTerm();
            result = validateOnly ? 0.0 : apply(operator, result, rightOperand);
        }

        return result;
    }

    private double parseTerm() {
        double result = parseUnary();

        while (hasNext() && (current() == '*' || current() == '/' || current() == '%')) {
            char operator = current();
            position++;
            double rightOperand = parseUnary();
            result = validateOnly ? 0.0 : apply(operator, result, rightOperand);
        }

        return result;
    }

    private double parseUnary() {
        boolean negate = false;
        if (hasNext() && current() == '-') {
            negate = true;
            position++;
            if (hasNext() && current() == '-') {
                throw invalidExpression();
            }
        }

        double result = parsePower();
        return negate && !validateOnly ? -result : result;
    }

    private double parsePower() {
        double base = parsePrimary();
        if (hasNext() && current() == '^') {
            position++;
            double exponent = parseUnary();
            return validateOnly ? 0.0 : Math.pow(base, exponent);
        }

        return base;
    }

    private double parsePrimary() {
        if (!hasNext()) {
            throw invalidExpression();
        }

        if (current() == '(') {
            position++;
            double result = parseExpression();
            if (!hasNext() || current() != ')') {
                throw invalidExpression();
            }
            position++;
            return result;
        }

        return parseNumber();
    }

    private double parseNumber() {
        int start = position;
        boolean digitFound = false;
        boolean decimalPointFound = false;

        while (hasNext()) {
            char character = current();
            if (Character.isDigit(character)) {
                digitFound = true;
                position++;
            } else if (character == '.' && !decimalPointFound) {
                decimalPointFound = true;
                position++;
            } else {
                break;
            }
        }

        if (!digitFound) {
            throw invalidExpression();
        }

        try {
            return Double.parseDouble(expression.substring(start, position));
        } catch (NumberFormatException exception) {
            throw invalidExpression();
        }
    }

    private double apply(char operator, double leftOperand, double rightOperand) {
        switch (operator) {
            case '+':
                return Calculator.add(leftOperand, rightOperand);
            case '-':
                return Calculator.subtract(leftOperand, rightOperand);
            case '*':
                return Calculator.multiply(leftOperand, rightOperand);
            case '/':
                return Calculator.divide(leftOperand, rightOperand);
            case '%':
                return Calculator.mod(leftOperand, rightOperand);
            default:
                throw invalidExpression();
        }
    }

    private boolean hasNext() {
        return position < expression.length();
    }

    private char current() {
        return expression.charAt(position);
    }

    private IllegalArgumentException invalidExpression() {
        return new IllegalArgumentException("Invalid expression");
    }
}
