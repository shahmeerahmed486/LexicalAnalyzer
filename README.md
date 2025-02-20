# **Lexical Analyzer**
This project implements a lexical analyzer in Java that tokenizes source code written in a custom language. The analyzer uses deterministic finite automata (DFAs) generated from regular expressions to identify tokens, builds a symbol table for variable and function declarations, and integrates error handling to report lexical errors.

## **Overview**
The lexical analyzer reads a source file (with a .xcl extension) and breaks the input into tokens such as identifiers, integers, decimals, characters, strings, booleans, operators, and symbols. It also distinguishes between global and local variables and supports function definitions. The analyzer was developed with the following key constraints:


## Language Features

### Data Types
- **int**: Represents integer values.
- **deci**: Represents decimal numbers. A decimal must include a dot ('.') and at least one digit after the dot (e.g., 3.14, 5.5). Decimals are expected to be accurate up to five decimal places following standard rounding rules.
- **char**: Represents a single character, enclosed in single quotes (e.g., 'A').
- **str**: Represents a string literal, enclosed in double quotes (e.g., "Hello, world!").
- **bool**: Represents boolean values, either `true` or `false`.

**Data Types Supported:**

Boolean: true/false values.
Integer: Whole numbers.
Decimal: Numbers with a fractional part, accurate up to five decimal places (following standard rounding rules).
Character: Single letters and digits enclosed in single quotes.
Identifiers:

Only lowercase letters (a to z) are recognized as valid variable names.

### Control Flow
- **if**, **elif**, **else**: Used for conditional execution of code blocks.

### Functions
- **def**: Keyword used to define a function.
- Functions are declared by specifying a return type followed by the function name and parameter list.  
  For example:
  ```xcl
  def int factorial(deci n) {
      if n == 0 {
          return 1;
      }
      return n * factorial(n - 1);
  }

## **Arithmetic Operations:**

Basic operators such as addition (+), subtraction (-), multiplication (*), division (/), and remainder/modulus (%) are supported.

## **Variables and Scope:**

Global variables are accessed with the @ prefix.
Local variables and function parameters are managed using a symbol table.

## **Comments and Whitespaces:**

The analyzer ignores extra spaces.
It correctly handles single-line (//) and multi-line (/* ... */) comments, even in complex situations.

## **State Transition Visualization:**

The system includes functionality to display the DFA transition state table for debugging purposes.

## **Code Structure**
The project is organized into several Java classes, each responsible for a specific aspect of the lexical analysis:

## **LexicalAnalyzer.java:**
Contains the main logic for tokenization, scope handling, symbol table integration, and error reporting. It uses DFAs to validate tokens based on regular expressions.

## **ErrorHandler.java:**
Manages the collection and reporting of lexical errors, including invalid tokens and unclosed comments or string literals.

## **DFA.java:**
Implements the deterministic finite automata used for token recognition. It handles state transitions based on input characters and checks if a token is accepted.

## **NFA.java & RegexToDFAConverter.java:**
These classes work together to convert a regular expression into an NFA (Non-deterministic Finite Automata) and then into a DFA using the subset construction method.

## **State.java:**
Represents a state in the automata with its transitions and final-state flag.

## **Symbol.java & SymbolTable.java:**
Represent symbols (such as variables and functions) and maintain a table to track them, ensuring uniqueness based on name and scope.

## **Token.java:**
Defines the structure of a token, including its type, value, and the line number where it appears, which aids in error reporting.
