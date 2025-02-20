package com.lexer;

import java.util.ArrayList;
import java.util.List;

public class ErrorHandler {
    private final List<String> errors;

    public ErrorHandler() {
        errors = new ArrayList<>();
    }

    public void addError(String message, int lineNumber) {
        errors.add("Error at line " + lineNumber + ": " + message);
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    public void printErrors() {
        if (hasErrors()) {
            System.out.println("\nLexical Errors:");
            for (String error : errors) {
                System.out.println(error);
            }
        } else {
            System.out.println("\nNo lexical errors found.");
        }
    }
}