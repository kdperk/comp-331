package edu.comp331;

class ExpressionParser {
    private final String expression;
    private int position;

    ExpressionParser(String expression) {
        this.expression = expression;
    }

    boolean isValid() {
        return parseExpression() && position == expression.length();
    }

    private boolean parseExpression() {
        if (!parseTerm()) {
            return false;
        }

        while (hasNext() && (current() == '+' || current() == '-')) {
            position++;
            if (!parseTerm()) {
                return false;
            }
        }

        return true;
    }

    private boolean parseTerm() {
        if (!parseUnary()) {
            return false;
        }

        while (hasNext() && (current() == '*' || current() == '/' || current() == '%')) {
            position++;
            if (!parseUnary()) {
                return false;
            }
        }

        return true;
    }

    private boolean parseUnary() {
        if (hasNext() && current() == '-') {
            position++;
            if (hasNext() && current() == '-') {
                return false;
            }
        }

        return parsePower();
    }

    private boolean parsePower() {
        if (!parsePrimary()) {
            return false;
        }

        if (hasNext() && current() == '^') {
            position++;
            return parseUnary();
        }

        return true;
    }

    private boolean parsePrimary() {
        if (!hasNext()) {
            return false;
        }

        if (current() == '(') {
            position++;
            if (!parseExpression() || !hasNext() || current() != ')') {
                return false;
            }
            position++;
            return true;
        }

        return parseNumber();
    }

    private boolean parseNumber() {
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

        return digitFound;
    }

    private boolean hasNext() {
        return position < expression.length();
    }

    private char current() {
        return expression.charAt(position);
    }
}
