You are helping me implement a simple Java project. 
    Goal: Create a calculator class that has functions representing calculator operations. 
    Inputs: decimal required operands to function
    Outputs: Numeric result of operation.
    Constraints: 
    - must follow rules of mathematics
    Edge cases: unsure
    Acceptance criteria: Working functions add(), subtract(), multiply(), divide(), abs(), negate(), pow(), mod(). 
    Generate only the class and a short explanation

Great. Now propose test cases to verify expected behavior for normal calls, edge cases, and unxpected input. Include tests for a result being too large or small to fit into a Java double.

Unexpected input for these methods is limited because all parameters are primitive double; null, strings, and missing operands fail at compile time. Useful runtime inputs include NaN, positive/negative infinity, positive/negative zero, and values beyond the representable range. Java double overflow produces infinity, while underflow produces zero.

Great. Add these to the project so they can be run from the command line with 'mvn test'. I've fixed previous maven dependency issue.

Added CalculatorTest.java with 7 test cases covering:

Normal operations
Negative and fractional values
Division and modulo by zero
NaN and infinity
Overflow and underflow
Zero and negative zero

Created simple calculator program with functions that take double and perform a calculation.

Initial acceptance criteria:
Working functions add(), subtract(), multiply(), divide(), abs(), negate(), pow(), mod(). 

Program worked as expected.

Follow up work involved adding a function to parse an input string and determine if it is a valid expression. The function isValidExpression() was added to the Calculator class.
