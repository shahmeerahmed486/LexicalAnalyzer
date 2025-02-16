package com.lexer;

public class Token {
    private String type;   // Token type (e.g., IDENTIFIER, INTEGER, OPERATOR)
    private String value;  // Actual token value (e.g., "x", "5", "+")
    private int lineNumber; // Line number for error handling

    public Token(String type, String value, int lineNumber) {
        this.type = type;
        this.value = value;
        this.lineNumber = lineNumber;
    }

    public String getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    @Override
    public String toString() {
        return "Token: " + value + " -> Type: " + type + " (Line: " + lineNumber + ")";
    }
}
