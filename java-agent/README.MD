# -- Lab 3 Readme --

# Current architecture:

## Tool interface: AgentTool.java
- contains name(), description(), and execute(...) methods that must be implemented.
- contains static validatePath()
    - method that validates a passed String is a path inside the base sandbox directory
    - does not check that a path actually exists
    - returns a Java Path object if passed String is valid/safe

### Tools using interface:
- GetFileContentTool - reads content of a file.
    - reads only text content, not binary data.
    - limited to 10,000 Java characters.
    - uses a BufferedReader to read up to 10,000 characters into a StringBuilder. Copilots initial suggestion was to read the entire file into a String and truncate it if necessary. A Java String maximum length is tracked internally by an int, which means it can hold Integer.MAX_VALUE characters. As 2GB of text is slightly more than I will probably need, I decided to make this more efficient.
- GetFilesInfoTool - gets info for files and directories
    - if no directory is specified, runs on the base sandbox directory.
    - rough imitation of Windows ls/dir console command.
    - displays type (file, directory), size (files only), and name.
    - by Java docs own admission, size is not necessarily perfectly accurate due to things like disk compression or filesystem quirks.
    - displays files and directories intermixed. in the future, I will probably change them to be grouped.
- WriteFileTool - writes text to a file
    - writes only text content, not binary data.
    - does not create non-existing parent directories in path.
    - does create non-existing file in an existing directory.
    - limited to writing 10,100 Java characters. The extra 100 characters are for testing reading truncation.
    - cannot append or insert, only rewrites entire contents of file. 
- RunJavaTestsTool - executes 'mvn test' in the sandbox envrironment

### Running Individual Tools
- GetFileContentTool, GetFilesInfoTool, and WriteFileTool cannot be run individually from the command line right now. I'm just waiting to see how they are used in future labs.
- Demo of three above tools with output: ```mvn exec:java "-Dexec.args=--tool-demo"```
- RunJavaTestsTool - ```mvn exec:java "-Dexec.args=--run-tests"```
    - Humorously, Copilot's suggestion for running this tool from the command line was ... not running it at all. It tried to directly spawn a new process running mvn instead.
- ```mvn exec:java "-Dexec.args=--help"``` to see summary of existing commands

### Sandbox Containment
- implemented into every tool with Java Path object restrictions
- assumes the sandbox directory shares the same parent as the working directory (../sandbox-calculator)
- not sufficient for a number of situations I deemed outside the scope of this project:
    - hard or soft links inside the sandbox linking elsewhere
    - executing from a different working directory and not moving the sandbox accordingly
    - changing the system PATH variable to point to a different mvn executable
    - changing the file system between validation and tool execution with secondary threads
    - running mvn or Java code from the sandbox - currently unrestricted

### Testing
- test coverage for GetFileContentTool, GetFilesInfoTool,WriteFileTool
- test expected operations
- test failure paths (when appropriate):
    - missing files or directories
    - illegal path access or traversal
    - intended tool restrictions
    - invalid arguments

### Sample Tool Demo Execution (Maven output excluded)
```mvn exec:java "-Dexec.args=--tool-demo"```

        Running tool demo...

        Directory F:\Coding\comp-331\sandbox-calculator

        Type     Size (bytes)     Name
        ----     ------------     ----
        F        2744             pom.xml                                           
        F        741              README.MD                                         
        D                         src                                               
        D                         target                                            
        F        44               test.txt                                          

        Content of file F:\Coding\comp-331\sandbox-calculator\test.txt

        This is a test. Sun Nov 08 11:26:38 EST 2026

        Successfully wrote data to F:\Coding\comp-331\sandbox-calculator\test.txt

        Content of file F:\Coding\comp-331\sandbox-calculator\test.txt

        This is a test. Sun Nov 08 12:16:11 EST 2026



### Maven Test Output
```mvn test
(build output omitted)
[INFO] 
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running edu.comp331.ToolTest
[INFO] Tests run: 8, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.083 s -- in edu.comp331.ToolTest
[INFO] 
[INFO] Results:
[INFO] 
[INFO] Tests run: 8, Failures: 0, Errors: 0, Skipped: 0
[INFO] 
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  0.956 s
[INFO] Finished at: 2026-11-08T12:36:54-05:00
[INFO] ------------------------------------------------------------------------
```
