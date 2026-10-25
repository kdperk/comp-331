package edu.comp331;

import java.util.ArrayDeque;
import java.util.Deque;

public final class Calculator {
    private static final int MAX_LENGTH = 100;

    private Calculator() {
    }

    public static double solve(String expression) {
        if (expression == null) {
            throw new IllegalArgumentException("Expression cannot be null");
        }

        String input = removeWhitespace(expression);
        if (input.isEmpty()) {
            throw new IllegalArgumentException("Expression cannot be empty");
        }
        if (input.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("Expression is too long");
        }

        Parser parser = new Parser(input);
        double result = parser.parseExpression(false);
        if (parser.hasMore()) {
            throw new IllegalArgumentException("Unexpected character at position " + parser.position());
        }
        return result;
    }

    public static double add(double left, double right) {
        return left + right;
    }

    public static double subtract(double left, double right) {
        return left - right;
    }

    public static double multiply(double left, double right) {
        return left * right;
    }

    public static double divide(double left, double right) {
        if (right == 0.0) {
            throw new ArithmeticException("Cannot divide by zero");
        }
        return left / right;
    }

    public static double modulo(double left, double right) {
        if (right == 0.0) {
            throw new ArithmeticException("Cannot take modulus by zero");
        }
        return left % right;
    }

    public static double exponentiate(double left, double right) {
        return Math.pow(left, right);
    }

    public static double negate(double value) {
        return -value;
    }

    private static String removeWhitespace(String expression) {
        StringBuilder result = new StringBuilder(expression.length());
        for (int index = 0; index < expression.length(); index++) {
            char character = expression.charAt(index);
            if (!Character.isWhitespace(character)) {
                result.append(character);
            }
        }
        return result.toString();
    }

    private static final class Parser {
        private final String input;
        private int index;

        private Parser(String input) {
            this.input = input;
        }

        private double parseExpression(boolean insideParentheses) {
            Deque<Double> operands = new ArrayDeque<>();
            Deque<Character> operators = new ArrayDeque<>();
            boolean expectsOperand = true;

            while (index < input.length()) {
                char character = input.charAt(index);

                if (character == ')') {
                    if (!insideParentheses) {
                        throw new IllegalArgumentException("Unmatched closing parenthesis");
                    }
                    if (expectsOperand) {
                        throw new IllegalArgumentException("Missing operand before closing parenthesis");
                    }
                    index++;
                    break;
                }

                if (isDigitOrDecimalPoint(character)) {
                    if (!expectsOperand) {
                        throw new IllegalArgumentException("Missing operator before number");
                    }
                    operands.push(readNumber());
                    expectsOperand = false;
                    continue;
                }

                if (character == '(') {
                    if (!expectsOperand) {
                        throw new IllegalArgumentException("Implied multiplication is not allowed");
                    }
                    index++;
                    operands.push(parseExpression(true));
                    expectsOperand = false;
                    continue;
                }

                if (!isOperator(character)) {
                    throw new IllegalArgumentException("Invalid character: " + character);
                }

                if (character == '-' && expectsOperand) {
                    if (index + 1 >= input.length() || input.charAt(index + 1) == '-'
                            || input.charAt(index + 1) == '+') {
                        throw new IllegalArgumentException("Invalid negation");
                    }
                    operators.push('~');
                    index++;
                    continue;
                }

                if (expectsOperand) {
                    throw new IllegalArgumentException("Operator requires a left operand");
                }

                while (!operators.isEmpty() && shouldApplyBefore(operators.peek(), character)) {
                    applyTopOperator(operands, operators);
                }
                operators.push(character);
                index++;
                expectsOperand = true;
            }

            if (insideParentheses && (index == input.length() || input.charAt(index - 1) != ')')) {
                throw new IllegalArgumentException("Unmatched opening parenthesis");
            }
            if (expectsOperand) {
                throw new IllegalArgumentException("Expression ends with an operator");
            }

            while (!operators.isEmpty()) {
                applyTopOperator(operands, operators);
            }
            if (operands.size() != 1) {
                throw new IllegalArgumentException("Invalid expression");
            }
            return operands.pop();
        }

        private double readNumber() {
            int start = index;
            boolean hasDigits = false;
            boolean hasDecimalPoint = false;

            while (index < input.length()) {
                char character = input.charAt(index);
                if (Character.isDigit(character)) {
                    hasDigits = true;
                    index++;
                } else if (character == '.' && !hasDecimalPoint) {
                    hasDecimalPoint = true;
                    index++;
                } else {
                    break;
                }
            }

            if (!hasDigits) {
                throw new IllegalArgumentException("A decimal point must have digits");
            }
            try {
                return Double.parseDouble(input.substring(start, index));
            } catch (NumberFormatException exception) {
                throw new IllegalArgumentException("Invalid number", exception);
            }
        }

        private boolean hasMore() {
            return index < input.length();
        }

        private int position() {
            return index;
        }

        private static boolean isDigitOrDecimalPoint(char character) {
            return Character.isDigit(character) || character == '.';
        }

        private static boolean isOperator(char character) {
            return character == '+' || character == '-' || character == '*' || character == '/' || character == '%'
                    || character == '^';
        }

        private static boolean shouldApplyBefore(char top, char incoming) {
            if (top == '~') {
                return precedence(top) >= precedence(incoming);
            }
            if (isRightAssociative(incoming)) {
                return precedence(top) > precedence(incoming);
            }
            return precedence(top) >= precedence(incoming);
        }

        private static int precedence(char operator) {
            return switch (operator) {
                case '+', '-' -> 1;
                case '*', '/', '%' -> 2;
                case '~' -> 3;
                case '^' -> 4;
                default -> throw new IllegalArgumentException("Unknown operator: " + operator);
            };
        }

        private static boolean isRightAssociative(char operator) {
            return operator == '^';
        }

        private static void applyTopOperator(Deque<Double> operands, Deque<Character> operators) {
            char operator = operators.pop();
            if (operator == '~') {
                if (operands.isEmpty()) {
                    throw new IllegalArgumentException("Negation requires an operand");
                }
                operands.push(negate(operands.pop()));
                return;
            }
            if (operands.size() < 2) {
                throw new IllegalArgumentException("Operator requires two operands");
            }
            double right = operands.pop();
            double left = operands.pop();
            operands.push(apply(operator, left, right));
        }

        private static double apply(char operator, double left, double right) {
            return switch (operator) {
                case '+' -> add(left, right);
                case '-' -> subtract(left, right);
                case '*' -> multiply(left, right);
                case '/' -> divide(left, right);
                case '%' -> modulo(left, right);
                case '^' -> exponentiate(left, right);
                default -> throw new IllegalArgumentException("Unknown operator: " + operator);
            };
        }
    }
}
