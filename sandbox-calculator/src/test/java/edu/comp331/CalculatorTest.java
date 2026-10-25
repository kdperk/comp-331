package edu.comp331;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

public class CalculatorTest {

    @Test
    public void normalOperations() {
        assertEquals(7.0, Calculator.add(3.0, 4.0), 0.0);
        assertEquals(1.0, Calculator.subtract(4.0, 3.0), 0.0);
        assertEquals(12.0, Calculator.multiply(3.0, 4.0), 0.0);
        assertEquals(2.0, Calculator.divide(8.0, 4.0), 0.0);
        assertEquals(5.0, Calculator.abs(-5.0), 0.0);
        assertEquals(-5.0, Calculator.negate(5.0), 0.0);
        assertEquals(8.0, Calculator.pow(2.0, 3.0), 0.0);
        assertEquals(1.0, Calculator.mod(10.0, 3.0), 0.0);
    }

    @Test
    public void negativeAndFractionalValues() {
        assertEquals(-7.5, Calculator.add(-3.0, -4.5), 0.0);
        assertEquals(0.25, Calculator.divide(0.5, 2.0), 0.0);
        assertEquals(0.25, Calculator.mod(1.25, 1.0), 0.0);
        assertEquals(4.0, Calculator.abs(-4.0), 0.0);
    }

    @Test(expected = ArithmeticException.class)
    public void divideByZeroThrowsException() {
        Calculator.divide(10.0, 0.0);
    }

    @Test(expected = ArithmeticException.class)
    public void moduloByZeroThrowsException() {
        Calculator.mod(10.0, 0.0);
    }

    @Test
    public void specialDoubleValues() {
        assertTrue(Double.isNaN(Calculator.add(Double.NaN, 1.0)));
        assertTrue(Double.isNaN(Calculator.pow(-1.0, 0.5)));
        assertEquals(Double.POSITIVE_INFINITY,
                Calculator.add(Double.POSITIVE_INFINITY, 1.0), 0.0);
        assertEquals(Double.NEGATIVE_INFINITY,
                Calculator.negate(Double.POSITIVE_INFINITY), 0.0);
    }

    @Test
    public void overflowAndUnderflow() {
        assertEquals(Double.POSITIVE_INFINITY,
                Calculator.multiply(Double.MAX_VALUE, 2.0), 0.0);
        assertEquals(0.0,
                Calculator.divide(Double.MIN_VALUE, 2.0), 0.0);
    }

    @Test
    public void zeroAndNegativeZero() {
        assertEquals(0.0, Calculator.abs(-0.0), 0.0);
        assertEquals(-0.0, Calculator.negate(0.0), 0.0);
        assertEquals(0.0, Calculator.mod(0.0, 5.0), 0.0);
    }

    @Test
    public void validExpressions() {
        assertTrue(Calculator.isValidExpression("1 + 2 * 3"));
        assertTrue(Calculator.isValidExpression("(1 + 2) * 3"));
        assertTrue(Calculator.isValidExpression("2^3^2"));
        assertTrue(Calculator.isValidExpression("-2^2"));
        assertTrue(Calculator.isValidExpression("2^-3"));
        assertTrue(Calculator.isValidExpression("10--5"));
        assertTrue(Calculator.isValidExpression("10- -5"));
        assertTrue(Calculator.isValidExpression("10-(-5)"));
        assertTrue(Calculator.isValidExpression("10 / 0"));
        assertTrue(Calculator.isValidExpression("10 % 0"));
    }

    @Test
    public void validDecimalAndWhitespaceForms() {
        assertTrue(Calculator.isValidExpression(".5 + 5."));
        assertTrue(Calculator.isValidExpression("0005.0500"));
        assertTrue(Calculator.isValidExpression(" 1\t+\n2 "));
    }

    @Test
    public void invalidExpressions() {
        assertFalse(Calculator.isValidExpression(null));
        assertFalse(Calculator.isValidExpression(""));
        assertFalse(Calculator.isValidExpression("   "));
        assertFalse(Calculator.isValidExpression("5(3)"));
        assertFalse(Calculator.isValidExpression("(5)(3)"));
        assertFalse(Calculator.isValidExpression("--5"));
        assertFalse(Calculator.isValidExpression("10---5"));
        assertFalse(Calculator.isValidExpression("+5"));
        assertFalse(Calculator.isValidExpression("5++3"));
        assertFalse(Calculator.isValidExpression("5*/3"));
        assertFalse(Calculator.isValidExpression("1.2.3"));
        assertFalse(Calculator.isValidExpression("."));
        assertFalse(Calculator.isValidExpression("1e3"));
        assertFalse(Calculator.isValidExpression("2&3"));
        assertFalse(Calculator.isValidExpression("2=3"));
        // assertFalse(Calculator.isValidExpression("1e3"));
        // assertFalse(Calculator.isValidExpression("--5"));
        // assertFalse(Calculator.isValidExpression("10---5"));
    }

    @Test
    public void invalidParenthesesAndOperandPlacement() {
        assertFalse(Calculator.isValidExpression("()"));
        assertFalse(Calculator.isValidExpression("(2+3"));
        assertFalse(Calculator.isValidExpression("2+3)"));
        assertFalse(Calculator.isValidExpression("(2+)"));
        assertFalse(Calculator.isValidExpression("*2"));
        assertFalse(Calculator.isValidExpression("2/"));
        assertFalse(Calculator.isValidExpression("2^"));
    }

    @Test
    public void solvesExpressions() {
        assertEquals(14.0, Calculator.solve("2 + 3 * 4"), 0.0);
        assertEquals(20.0, Calculator.solve("(2 + 3) * 4"), 0.0);
        assertEquals(512.0, Calculator.solve("2^3^2"), 0.0);
        assertEquals(-4.0, Calculator.solve("-2^2"), 0.0);
        assertEquals(0.125, Calculator.solve("2^-3"), 0.0);
        assertEquals(15.0, Calculator.solve("10--5"), 0.0);
        assertEquals(5.5, Calculator.solve(".5 + 5."), 0.0);
    }

    @Test
    public void solvesWhitespaceAndNestedExpressions() {
        assertEquals(15.0, Calculator.solve(" ( 2 + 3 ) * 3 "), 0.0);
        assertEquals(10.0, Calculator.solve("2 * (3 + 2)"), 0.0);
    }

    @Test(expected = ArithmeticException.class)
    public void solvingDivisionByZeroThrowsException() {
        Calculator.solve("10 / 0");
    }

    @Test(expected = ArithmeticException.class)
    public void solvingModuloByZeroThrowsException() {
        Calculator.solve("10 % 0");
    }

    @Test
    public void solvingInvalidExpressionsThrowsException() {
        assertInvalidSolve(null);
        assertInvalidSolve("");
        assertInvalidSolve("--5");
        assertInvalidSolve("5(3)");
        assertInvalidSolve("1.2.3");
        assertInvalidSolve("2=3");
    }

    @Test
    public void enforcesMaximumExpressionLength() {
        assertTrue(Double.isFinite(Calculator.solve("1 ".repeat(100))));
        assertFalse(Calculator.isValidExpression("1".repeat(101)));
        assertInvalidSolve("1".repeat(101));
    }

    private void assertInvalidSolve(String expression) {
        try {
            Calculator.solve(expression);
        } catch (IllegalArgumentException exception) {
            return;
        }

        throw new AssertionError("Expected an invalid expression exception");
    }
}
