package com.lexer;

public class Main {
    public static void main(String[] args) {
        LexicalAnalyzer lexer = new LexicalAnalyzer();
        lexer.debugDFA("IDENTIFIER");  // Debug DFA transitions
        System.out.println("Valid IDENTIFIER: " + lexer.validateToken("IDENTIFIER", "hello"));
        System.out.println("Invalid IDENTIFIER: " + lexer.validateToken("IDENTIFIER", "123"));
    }
}