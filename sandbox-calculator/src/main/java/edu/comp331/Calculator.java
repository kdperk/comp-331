package edu.comp331;

public class Calculator {
    public double add(double firstOperand, double secondOperand) {
        return firstOperand + secondOperand;
    }

    public double subtract(double firstOperand, double secondOperand) {
        return firstOperand - secondOperand;
    }

    public double multiply(double firstOperand, double secondOperand) {
        return firstOperand * secondOperand;
    }

    public double divide(double firstOperand, double secondOperand) {
        if (secondOperand == 0.0) {
            throw new ArithmeticException("Cannot divide by zero");
        }

        return firstOperand / secondOperand;
    }

    public double abs(double operand) {
        return Math.abs(operand);
    }

    public double negate(double operand) {
        return -operand;
    }

    public double pow(double base, double exponent) {
        return Math.pow(base, exponent);
    }

    public double mod(double firstOperand, double secondOperand) {
        if (secondOperand == 0.0) {
            throw new ArithmeticException("Cannot take modulo by zero");
        }

        return firstOperand % secondOperand;
    }
}
