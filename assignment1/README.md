Kenneth Perkovich
COMP 331
Assignment 1 - Setup
10/3/2026

Minimal Java project with Copilot-generated method.

I asked Copilot to create a method to verify a binary number (as a String) can fit into
a signed 32-bit integer. 

Copilot is working.

It created a method that worked for all positive values, but in order to work for negative values
I had to adjust its validation for passed Strings, as it wanted a signed binary string passed in. 
The parseLong method it used internally does not use signed representation but instead accepts
an optional negative sign as the first character.

I verfied its accuracy by verifying the function passes with the max and min values, and then fails
with values one more/less than those.
