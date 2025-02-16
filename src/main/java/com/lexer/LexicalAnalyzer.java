package com.lexer;

import java.util.*;

public class LexicalAnalyzer {
    private final Map<String, DFA> dfas;
    private final Set<String> keywords;

    public LexicalAnalyzer() {
        dfas = new HashMap<>();
        dfas.put("IDENTIFIER", buildIdentifierDFA());
        dfas.put("INTEGER", buildIntegerDFA());
        dfas.put("DECIMAL", buildDecimalDFA());
        dfas.put("BOOLEAN", buildBooleanDFA());
        dfas.put("OPERATOR", buildOperatorDFA());

        // Keywords to be checked after identifier DFA validation
        keywords = new HashSet<>(Arrays.asList(
                "main", "if", "elif", "else", "out", "in", "deci", "int", "char", "bool"
        ));
    }

    // DFA for Identifiers (Only lowercase a-z, no numbers allowed)
    private DFA buildIdentifierDFA() {
        DFA identifierDFA = new DFA(0);

        // Only lowercase letters (a-z)
        for (char c = 'a'; c <= 'z'; c++) {
            identifierDFA.addTransition(0, c, 1);
            identifierDFA.addTransition(1, c, 1);
        }

        identifierDFA.addFinalState(1);
        return identifierDFA;
    }

    // DFA for Integer Numbers (Only digits, cannot start with zero unless it is a single '0')
    private DFA buildIntegerDFA() {
        DFA intDFA = new DFA(0);

        // First digit (cannot be zero unless single-digit)
        for (char c = '1'; c <= '9'; c++) {
            intDFA.addTransition(0, c, 1);
        }
        intDFA.addTransition(0, '0', 2); // Special case for "0" as a valid integer

        // Additional digits (0-9)
        for (char c = '0'; c <= '9'; c++) {
            intDFA.addTransition(1, c, 1);
        }

        intDFA.addFinalState(1);
        intDFA.addFinalState(2);
        return intDFA;
    }

    // DFA for Decimal Numbers (At most 5 digits after decimal point)
    private DFA buildDecimalDFA() {
        DFA decimalDFA = new DFA(0);

        // Whole number part (same as integer DFA)
        for (char c = '1'; c <= '9'; c++) {
            decimalDFA.addTransition(0, c, 1);
        }
        decimalDFA.addTransition(0, '0', 2); // Single "0" valid before decimal point

        // Digits after first non-zero
        for (char c = '0'; c <= '9'; c++) {
            decimalDFA.addTransition(1, c, 1);
        }

        // Decimal point
        decimalDFA.addTransition(1, '.', 3);
        decimalDFA.addTransition(2, '.', 3);

        // Decimal part (up to 5 digits)
        for (char c = '0'; c <= '9'; c++) {
            decimalDFA.addTransition(3, c, 4);
            decimalDFA.addTransition(4, c, 5);
            decimalDFA.addTransition(5, c, 6);
            decimalDFA.addTransition(6, c, 7);
            decimalDFA.addTransition(7, c, 8);
        }

        decimalDFA.addFinalState(4);
        decimalDFA.addFinalState(5);
        decimalDFA.addFinalState(6);
        decimalDFA.addFinalState(7);
        decimalDFA.addFinalState(8);
        return decimalDFA;
    }

    // DFA for Boolean Constants ("true", "false")
    private DFA buildBooleanDFA() {
        DFA boolDFA = new DFA(0);
        String[] booleans = {"true", "false"};
        for (String word : booleans) {
            int state = 0;
            for (char c : word.toCharArray()) {
                boolDFA.addTransition(state, c, state + 1);
                state++;
            }
            boolDFA.addFinalState(state);
        }
        return boolDFA;
    }

    // DFA for Operators (+, -, *, /, %, ^ and comparison operators)
    private DFA buildOperatorDFA() {
        DFA operatorDFA = new DFA(0);
        char[] singleOperators = {'+', '-', '*', '/', '%', '^'};
        String[] doubleOperators = {"==", "!=", ">=", "<="};

        // Single-character operators
        for (char op : singleOperators) {
            operatorDFA.addTransition(0, op, 1);
        }

        // Two-character operators
        for (String op : doubleOperators) {
            operatorDFA.addTransition(0, op.charAt(0), 2);
            operatorDFA.addTransition(2, op.charAt(1), 3);
            operatorDFA.addFinalState(3);
        }

        operatorDFA.addFinalState(1);
        return operatorDFA;
    }

    // Function to analyze a given token and determine its type
    public void analyzeToken(String token) {
        // First, check if the token is an identifier
        if (dfas.get("IDENTIFIER").validate(token)) {
            if (keywords.contains(token)) {
                System.out.println("Token: " + token + " -> Type: KEYWORD");
            } else {
                System.out.println("Token: " + token + " -> Type: IDENTIFIER");
            }
            return;
        }

        // Check other DFAs
        for (Map.Entry<String, DFA> entry : dfas.entrySet()) {
            if (!entry.getKey().equals("IDENTIFIER") && entry.getValue().validate(token)) {
                System.out.println("Token: " + token + " -> Type: " + entry.getKey());
                return;
            }
        }

        System.out.println("Token: " + token + " -> Type: UNKNOWN");
    }

    // Main function to test the lexical analyzer
    public static void main(String[] args) {
        LexicalAnalyzer analyzer = new LexicalAnalyzer();

        String[] testTokens = {
                "main", "elif", "abc", "123", "45.678", "true", "+", "^", "xyz",
                "123.45", "if", "else", "in", "out", "==", "!=", "<=", ">=", "deci"
        };

        for (String token : testTokens) {
            analyzer.analyzeToken(token);
        }
    }
}
