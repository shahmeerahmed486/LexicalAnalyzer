package com.lexer;

import java.io.*;
import java.util.*;

public class LexicalAnalyzer {
    private final Map<String, DFA> dfas;
    private final Set<String> keywords;
    private final Set<String> symbols;
    private final List<Token> tokens;
    private final SymbolTable symbolTable;

    private boolean insideFunction = false;  // Track if currently inside a function
    private String currentFunction = "";     // Stores the current function name
    private String lastKeyword = "";         // Stores the last encountered keyword
    private String lastToken = "";           // Stores the last encountered token

    public LexicalAnalyzer() {
        dfas = new HashMap<>();
        dfas.put("IDENTIFIER", buildIdentifierDFA());
        dfas.put("INTEGER", buildIntegerDFA());
        dfas.put("DECIMAL", buildDecimalDFA());
        dfas.put("BOOLEAN", buildBooleanDFA());
        dfas.put("OPERATOR", buildOperatorDFA());

        keywords = new HashSet<>(Arrays.asList(
                "main", "if", "elif", "else", "out", "in", "deci", "int", "char", "bool", "return"
        ));

        symbols = new HashSet<>(Arrays.asList("{", "}", "(", ")", ";", ","));

        tokens = new ArrayList<>();
        symbolTable = new SymbolTable();

        lastKeyword = "";  // Initialize tracking variables
        lastToken = "";
    }


    private DFA buildIdentifierDFA() {
        DFA identifierDFA = new DFA(0);
        for (char c = 'a'; c <= 'z'; c++) {
            identifierDFA.addTransition(0, c, 1);
            identifierDFA.addTransition(1, c, 1);
        }
        identifierDFA.addFinalState(1);
        return identifierDFA;
    }

    private DFA buildIntegerDFA() {
        DFA intDFA = new DFA(0);
        for (char c = '1'; c <= '9'; c++) {
            intDFA.addTransition(0, c, 1);
        }
        intDFA.addTransition(0, '0', 2);
        for (char c = '0'; c <= '9'; c++) {
            intDFA.addTransition(1, c, 1);
        }
        intDFA.addFinalState(1);
        intDFA.addFinalState(2);
        return intDFA;
    }

    private DFA buildDecimalDFA() {
        DFA decimalDFA = new DFA(0);
        for (char c = '1'; c <= '9'; c++) {
            decimalDFA.addTransition(0, c, 1);
        }
        decimalDFA.addTransition(0, '0', 1);
        for (char c = '0'; c <= '9'; c++) {
            decimalDFA.addTransition(1, c, 1);
        }
        decimalDFA.addTransition(1, '.', 2);
        for (char c = '0'; c <= '9'; c++) {
            decimalDFA.addTransition(2, c, 3);
            decimalDFA.addTransition(3, c, 4);
            decimalDFA.addTransition(4, c, 5);
            decimalDFA.addTransition(5, c, 6);
            decimalDFA.addTransition(6, c, 7);
        }
        decimalDFA.addFinalState(3);
        decimalDFA.addFinalState(4);
        decimalDFA.addFinalState(5);
        decimalDFA.addFinalState(6);
        decimalDFA.addFinalState(7);
        return decimalDFA;
    }

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

    private DFA buildOperatorDFA() {
        DFA operatorDFA = new DFA(0);
        char[] singleOperators = {'+', '-', '*', '/', '%', '^', '='};
        for (char op : singleOperators) {
            operatorDFA.addTransition(0, op, 1);
        }
        operatorDFA.addFinalState(1);
        return operatorDFA;
    }

    public void analyzeToken(String token, int lineNumber) {
        if (symbols.contains(token)) {
            tokens.add(new Token("SYMBOL", token, lineNumber));

            if (token.equals("{")) {
                insideFunction = true;  // Now inside function scope
            } else if (token.equals("}")) {
                insideFunction = false;
                currentFunction = "";  // Exit function scope
            }
            return;
        }

        if (dfas.get("IDENTIFIER").validate(token)) {
            if (keywords.contains(token)) {
                tokens.add(new Token("KEYWORD", token, lineNumber));
                lastKeyword = token; // Track last keyword to detect functions
                return;
            }

            // **Detect Function Definition**
            if (lastKeyword.equals("deci") || lastKeyword.equals("int") || lastKeyword.equals("char") || lastKeyword.equals("bool")) {
                // This is a function definition
                currentFunction = token;
                symbolTable.insert(token, "FUNCTION", "global", lastKeyword);
                tokens.add(new Token("FUNCTION", token, lineNumber));
                lastKeyword = ""; // Reset after function definition
                return;
            }

            // **Detect Function Parameter**
            if (insideFunction && lastToken.equals("(")) {
                // Function parameters are local variables
                tokens.add(new Token("PARAMETER", token, lineNumber));
                symbolTable.insert(token, "UNKNOWN", currentFunction, "");
                return;
            }

            // **Regular Identifier**
            String scope = insideFunction ? currentFunction : "global";
            tokens.add(new Token("IDENTIFIER", token, lineNumber));

            if (!symbolTable.exists(token)) {
                symbolTable.insert(token, "UNKNOWN", scope, "");
            }
            return;
        }

        if (dfas.get("INTEGER").validate(token)) {
            tokens.add(new Token("INTEGER", token, lineNumber));
            symbolTable.insert(token, "INTEGER", insideFunction ? currentFunction : "global", token);
            return;
        }

        if (dfas.get("DECIMAL").validate(token)) {
            tokens.add(new Token("DECIMAL", token, lineNumber));
            symbolTable.insert(token, "DECIMAL", insideFunction ? currentFunction : "global", token);
            return;
        }

        if (dfas.get("BOOLEAN").validate(token)) {
            tokens.add(new Token("BOOLEAN", token, lineNumber));
            symbolTable.insert(token, "BOOLEAN", insideFunction ? currentFunction : "global", token);
            return;
        }

        tokens.add(new Token("UNKNOWN", token, lineNumber));
        lastToken = token; // Store last token for function parameter detection
    }


    public void processInput(String input) {
        Scanner scanner = new Scanner(input);
        boolean inMultiLineComment = false;
        int lineNumber = 0;

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine().trim();
            lineNumber++;

            if (inMultiLineComment) {
                if (line.contains("*/")) {
                    inMultiLineComment = false;
                }
                continue;
            }
            if (line.startsWith("/*")) {
                if (!line.endsWith("*/")) {
                    inMultiLineComment = true;
                }
                continue;
            }
            if (line.contains("//")) {
                line = line.substring(0, line.indexOf("//")).trim();
            }
            if (!line.isEmpty()) {
                StringTokenizer tokenizer = new StringTokenizer(line, " {}(),;=+-*/%^<>!", true);
                while (tokenizer.hasMoreTokens()) {
                    String token = tokenizer.nextToken().trim();
                    if (!token.isEmpty()) {
                        analyzeToken(token, lineNumber);
                    }
                }
            }
        }
        scanner.close();
    }

    public static void main(String[] args) {
        LexicalAnalyzer analyzer = new LexicalAnalyzer();
        Scanner scanner = new Scanner(System.in);

        System.out.println("Enter the name of your .xcl file (without extension):");
        String fileName = scanner.nextLine();
        scanner.close();

        String filePath = "test_files/" + fileName + ".xcl";
        File file = new File(filePath);

        if (!file.exists()) {
            System.out.println("File not found: " + filePath);
            return;
        }

        StringBuilder input = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                input.append(line).append("\n");
            }
        } catch (IOException e) {
            System.out.println("Error reading the file: " + e.getMessage());
            return;
        }

        analyzer.processInput(input.toString());
        System.out.println("\nComplete Token List:");
        for (Token token : analyzer.tokens) {
            System.out.println(token);
        }

        analyzer.symbolTable.printTable();
    }
}
