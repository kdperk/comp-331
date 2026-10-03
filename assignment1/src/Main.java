/**
 * Kenneth Perkovich
 * COMP 331
 * Assignment 1 - Setup
 * 10/3/2026
 *
 */

public class Main {
    public static void main(String[] args) throws Exception {
        System.out.println("Testing...");

        String max = "01111111111111111111111111111111";
        String min = "-10000000000000000000000000000000";
        String tooLage = "10000000000000000000000000000000";
        String tooSmall = "-10000000000000000000000000000001";
        String invalid = "21111111111111111111111111111111";
        boolean error = false;

        if (!canFitIn32BitSignedInteger(max) || !canFitIn32BitSignedInteger(min)
                || canFitIn32BitSignedInteger(tooLage) || canFitIn32BitSignedInteger(tooSmall)) {
            error = true;
        }
        try {
            canFitIn32BitSignedInteger(invalid);
            error = true;
        } catch (IllegalArgumentException e) {
        }
        System.out.println(error ? "Test failed" : "Test passed");
    }

    // method to add two numbers
    public static int add(int a, int b) {
        return a + b;
    }

    // method to verify a binary number, given as a string, can fit into a 32-bit
    // signed integer
    public static boolean canFitIn32BitSignedInteger(String binaryString) {
        // Check if the binary string is valid
        if (!binaryString.matches("-?[01]+")) {
            throw new IllegalArgumentException("Input must be a binary string.");
        }

        // Convert binary string to decimal
        long decimalValue = Long.parseLong(binaryString, 2);

        // Check if the decimal value fits in a 32-bit signed integer range
        return decimalValue >= Integer.MIN_VALUE && decimalValue <= Integer.MAX_VALUE;
    }
}
