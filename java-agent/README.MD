Steps performed:
Create Gemini API key and create initial Main.java file that performs basic http query to Gemini
Received a lot of http 503 errors indicating Gemini API service is unusually busy

Initiated inline copilot prompt to retry on 503 errors - output was a while loop than continually retried 
untila non-503 status was achieved.
I manually added a maximum # of retries and exponential backoff behavior for successive retries.

In a separate git branch, I prompted Copilot chat to 'create a Java utility method to perform an http request to the Gemini API'.
The output was actually pretty reasonable, which I think was partially due to the fact that I had an existing loop in main() that 
performed that task. It recognized the matching behavior and mostly copied it to a new class with one utility method, perserving 
the already existing URLs, console outputs, and connection retry policy. It included some other status codes along with 503, 
some of which were not good candidates for a connection retry. ex: 504 (service took too long to process, indicating the payload is 
too large or client wait time is set too short) and 429 (exceeded rate limit or daily quota).

In another separate branch, I used a more structured prompt for the same thing. As follows:
    You are helping me implement a Java utility method.
    Goal: Initiate a http connection and send a prompt to Google Gemini. Method should have input validation and limited retries in the event of failure.
    Inputs: 
    - prompt
    - Gemini API key
    - optional verbose flag that causes progress to be printed
    Outputs: Geminis response to the given prompt
    Constraints: 
    - max prompt size: 5000 characters
    - max number of retries: 5
    - exponential backoff for retries
    - only retry on http 503, 502, 500 errors
    Edge cases: unsure
    Acceptance criteria: Method returns desired Gemini response or gives up after the maximum number of retries.
    Generate only the method and a short explanation.
This generated a more complete version of the utility function which eventually ended up in Main.java. While similar, this version did have better input 
validation that matched my contraints, retried only on the error codes I specified, and insert verbose flag checks before println() calls. Because I 
did not list them initially, I used Copilot to help identify some edge cases that may be problematic. Many were either not applicable or outside the 
scope of what I wanted to implement, but it did identify a possible situation where the function could wait indefinitely for a response because no 
timeout time was specified. 

Anticipating additional command-line arguments in coming labs, I created a simple Flag record that holds both the longhand and shorthand versions 
of an argument along with a description. In class Main, I create a Flag for 'help' and 'verbose', and populated a hashmap with 
shorthand and longhand versions that point to the correct flag object. At this point, Copilot could see what I was doing and filled used inline 
completion to complete a for() loop that reacted to incoming arguments, however it was somewhat sloppy. It used substring() to grab the 
argument text and duplicated behavior for both versions (- and --) of the arguments. I adjusted it to just pass the full arg with dash(es) into a 
hashmap query, since a match is a match regardless of which format it is in. 

I moved actions for the 'help' argument into separate functions (printHelp() and printFlags()) which Copilot recognized immediately and suggested 
by inline completion.
