package edu.comp331;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

public class CalculatorTest {
    private Calculator calculator;

    @Before
    public void setUp() {
        calculator = new Calculator();
    }

    @Test
    public void normalOperations() {
        assertEquals(7.0, calculator.add(3.0, 4.0), 0.0);
        assertEquals(1.0, calculator.subtract(4.0, 3.0), 0.0);
        assertEquals(12.0, calculator.multiply(3.0, 4.0), 0.0);
        assertEquals(2.0, calculator.divide(8.0, 4.0), 0.0);
        assertEquals(5.0, calculator.abs(-5.0), 0.0);
        assertEquals(-5.0, calculator.negate(5.0), 0.0);
        assertEquals(8.0, calculator.pow(2.0, 3.0), 0.0);
        assertEquals(1.0, calculator.mod(10.0, 3.0), 0.0);
    }

    @Test
    public void negativeAndFractionalValues() {
        assertEquals(-7.5, calculator.add(-3.0, -4.5), 0.0);
        assertEquals(0.25, calculator.divide(0.5, 2.0), 0.0);
        assertEquals(0.25, calculator.mod(1.25, 1.0), 0.0);
        assertEquals(4.0, calculator.abs(-4.0), 0.0);
    }

    @Test(expected = ArithmeticException.class)
    public void divideByZeroThrowsException() {
        calculator.divide(10.0, 0.0);
    }

    @Test(expected = ArithmeticException.class)
    public void moduloByZeroThrowsException() {
        calculator.mod(10.0, 0.0);
    }

    @Test
    public void specialDoubleValues() {
        assertTrue(Double.isNaN(calculator.add(Double.NaN, 1.0)));
        assertTrue(Double.isNaN(calculator.pow(-1.0, 0.5)));
        assertEquals(Double.POSITIVE_INFINITY,
                calculator.add(Double.POSITIVE_INFINITY, 1.0), 0.0);
        assertEquals(Double.NEGATIVE_INFINITY,
                calculator.negate(Double.POSITIVE_INFINITY), 0.0);
    }

    @Test
    public void overflowAndUnderflow() {
        assertEquals(Double.POSITIVE_INFINITY,
                calculator.multiply(Double.MAX_VALUE, 2.0), 0.0);
        assertEquals(0.0,
                calculator.divide(Double.MIN_VALUE, 2.0), 0.0);
    }

    @Test
    public void zeroAndNegativeZero() {
        assertEquals(0.0, calculator.abs(-0.0), 0.0);
        assertEquals(-0.0, calculator.negate(0.0), 0.0);
        assertEquals(0.0, calculator.mod(0.0, 5.0), 0.0);
    }

    @Test
    public void validExpressions() {
        assertTrue(calculator.isValidExpression("1 + 2 * 3"));
        assertTrue(calculator.isValidExpression("(1 + 2) * 3"));
        assertTrue(calculator.isValidExpression("2^3^2"));
        assertTrue(calculator.isValidExpression("-2^2"));
        assertTrue(calculator.isValidExpression("2^-3"));
        assertTrue(calculator.isValidExpression("10--5"));
        assertTrue(calculator.isValidExpression("10- -5"));
        assertTrue(calculator.isValidExpression("10-(-5)"));
        assertTrue(calculator.isValidExpression("10 / 0"));
        assertTrue(calculator.isValidExpression("10 % 0"));
    }

    @Test
    public void validDecimalAndWhitespaceForms() {
        assertTrue(calculator.isValidExpression(".5 + 5."));
        assertTrue(calculator.isValidExpression("0005.0500"));
        assertTrue(calculator.isValidExpression(" 1\t+\n2 "));
    }

    @Test
    public void invalidExpressions() {
        assertFalse(calculator.isValidExpression(null));
        assertFalse(calculator.isValidExpression(""));
        assertFalse(calculator.isValidExpression("   "));
        assertFalse(calculator.isValidExpression("5(3)"));
        assertFalse(calculator.isValidExpression("(5)(3)"));
        assertFalse(calculator.isValidExpression("--5"));
        assertFalse(calculator.isValidExpression("10---5"));
        assertFalse(calculator.isValidExpression("+5"));
        assertFalse(calculator.isValidExpression("5++3"));
        assertFalse(calculator.isValidExpression("5*/3"));
        assertFalse(calculator.isValidExpression("1.2.3"));
        assertFalse(calculator.isValidExpression("."));
        assertFalse(calculator.isValidExpression("1e3"));
        assertFalse(calculator.isValidExpression("2&3"));
        assertFalse(calculator.isValidExpression("2=3"));
        // assertFalse(calculator.isValidExpression("1e3"));
        // assertFalse(calculator.isValidExpression("--5"));
        // assertFalse(calculator.isValidExpression("10---5"));
    }

    @Test
    public void invalidParenthesesAndOperandPlacement() {
        assertFalse(calculator.isValidExpression("()"));
        assertFalse(calculator.isValidExpression("(2+3"));
        assertFalse(calculator.isValidExpression("2+3)"));
        assertFalse(calculator.isValidExpression("(2+)"));
        assertFalse(calculator.isValidExpression("*2"));
        assertFalse(calculator.isValidExpression("2/"));
        assertFalse(calculator.isValidExpression("2^"));
    }
}
