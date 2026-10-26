Calculator - performs basic math operations on doubles

ExpressionParser - Takes a math expression as a String and calculates the value

CalculatorTest.java with test cases covering:
Normal operations
Negative and fractional values
Division and modulo by zero
NaN and infinity
Overflow and underflow
Zero and negative zero
Invalid expressions
Subtracting a negation
Order of operations
Parenthesis grouping
Whitespace
Leading/trailing zeroes
Implied multiplication i.e. 5(3)
Chained negation
Maximum length
Scientific notation

run with 'mvn test' from project directory
sandbox-calculator-string-solver branch - all tests pass
sandbox-alternate-calculator - fails on expressions that end with a closing parenthesis
