package com.lexer;

import java.io.*;
import java.util.*;

public class LexicalAnalyzer {
    private final Map<String, DFA> dfas;
    private final Set<String> keywords;
    private final Set<String> symbols;
    public final List<Token> tokens;
    private final SymbolTable symbolTable;
    private final ErrorHandler errorHandler;

    private boolean insideFunction = false;
    private String currentFunction = "";
    private String lastKeyword = "";
    private String secondLastKeyword = "";
    private String lastToken = "";

    public LexicalAnalyzer() {
        dfas = new HashMap<>();
        RegexToDFAConverter converter = new RegexToDFAConverter();

        dfas.put("IDENTIFIER", converter.convertRegexToDFA("[a-z][a-z]*"));
        dfas.put("INTEGER", converter.convertRegexToDFA("[0-9]+"));
        dfas.put("DECIMAL", converter.convertRegexToDFA("[0-9]+\\.[0-9]+"));
        dfas.put("CHAR", converter.convertRegexToDFA("'[a-zA-Z0-9]'"));
        dfas.put("STRING", converter.convertRegexToDFA("\"[^\"]*\""));
        dfas.put("BOOLEAN", converter.convertRegexToDFA("(true|false)"));
        dfas.put("OPERATOR", converter.convertRegexToDFA("[+\\*/%^=-]"));
        errorHandler = new ErrorHandler();
        keywords = new HashSet<>(Arrays.asList(
                "if", "elif", "else", "out", "in", "deci", "int", "char", "bool", "str", "return", "def", "str"
        ));

        symbols = new HashSet<>(Arrays.asList("{", "}", "(", ")", ";", ","));

        tokens = new ArrayList<>();
        symbolTable = new SymbolTable();

        lastKeyword = "";
        secondLastKeyword = "";
        lastToken = "";
    }

    public void debugDFA(String tokenType) {
        DFA dfa = dfas.get(tokenType);
        if (dfa != null) {
            dfa.displayTable();
        } else {
            System.out.println("DFA for token type '" + tokenType + "' not found.");
        }
    }

    public void analyzeToken(String token, int lineNumber) {

        if (token.startsWith("@")) {
            String globalId = token.substring(1); // remove the '@'
            if (dfas.get("IDENTIFIER").validate(globalId)) {
                tokens.add(new Token("GLOBAL_IDENTIFIER", token, lineNumber));
                // Global variables are always in the "global" scope.
                if (!symbolTable.exists(globalId, "global")) {
                    String type = getSymbolType();
                    symbolTable.insert(globalId, type, "global", "");
                }
            } else {
                errorHandler.addError("Invalid global identifier: " + token, lineNumber);
                tokens.add(new Token("UNKNOWN", token, lineNumber));
            }
            return;
        }

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

        if (dfas.get("BOOLEAN").validate(token)) {
            tokens.add(new Token("BOOLEAN", token, lineNumber));
            symbolTable.insert(token, "BOOLEAN", insideFunction ? currentFunction : "global", token);
            return;
        }

        if (dfas.get("IDENTIFIER").validate(token)) {
            if (keywords.contains(token)) {
                tokens.add(new Token("KEYWORD", token, lineNumber));
                secondLastKeyword = lastKeyword; // Update second last keyword
                lastKeyword = token; // Update last keyword
                lastToken = token;
                return;
            }


            if (secondLastKeyword.equals("def") && (lastKeyword.equals("deci") || lastKeyword.equals("int") ||
                    lastKeyword.equals("char") || lastKeyword.equals("bool"))) {
                // function definition
                currentFunction = token;
                symbolTable.insert(token, "FUNCTION", "global", lastKeyword);
                tokens.add(new Token("FUNCTION", token, lineNumber));
                lastKeyword = ""; // reset after function definition
                secondLastKeyword = ""; // reset second last keyword
                insideFunction = true;
                return;
            }

            // Regular Identifier

            String scope = insideFunction ? currentFunction : "global";
            if (!symbolTable.exists(token, scope)) {
                String type = getSymbolType();
                symbolTable.insert(token, type, scope, "");
            }

            tokens.add(new Token("IDENTIFIER", token, lineNumber));
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

        if (dfas.get("CHAR").validate(token)) {
            tokens.add(new Token("CHAR", token, lineNumber));
            symbolTable.insert(token, "CHAR", insideFunction ? currentFunction : "global", token);
            return;
        }

        if (dfas.get("STRING").validate(token)) {
            tokens.add(new Token("STRING", token, lineNumber));
            symbolTable.insert(token, "STRING", insideFunction ? currentFunction : "global", token);
            return;
        }

        if (dfas.get("OPERATOR").validate(token)) {
            tokens.add(new Token("OPERATOR", token, lineNumber));
            return;
        }


        errorHandler.addError("Unrecognized token: " + token, lineNumber);
        tokens.add(new Token("UNKNOWN", token, lineNumber));
        lastToken = token;
    }

    private String getSymbolType() {
        String type = "UNKNOWN";

        if (lastToken.equals("deci") || lastToken.equals("int") ||
                lastToken.equals("char") || lastToken.equals("bool") || lastToken.equals("str")) {

            type = switch (lastToken) {
                case "deci" -> "DECIMAL";
                case "int" -> "INTEGER";
                case "char" -> "CHARACTER";
                case "bool" -> "BOOLEAN";
                default -> "STRING";
            };
        }
        return type;
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
                StringTokenizer tokenizer = new StringTokenizer(line, " {}(),;=+-*/%^<>\"", true);
                List<String> tokensInLine = new ArrayList<>();

                while (tokenizer.hasMoreTokens()) {
                    String token = tokenizer.nextToken().trim();
                    if (!token.isEmpty()) {
                        tokensInLine.add(token);
                    }
                }

                boolean inString = false;
                StringBuilder currentStringLiteral = new StringBuilder();

                for (String token : tokensInLine) {
                    if (token.equals("\"")) {
                        inString = !inString;
                        if (!inString && currentStringLiteral.length() > 0) {
                            analyzeToken("\"" + currentStringLiteral.toString() + "\"", lineNumber);
                            currentStringLiteral.setLength(0);
                        }
                    } else if (inString) {
                        currentStringLiteral.append(token);
                    } else {
                        analyzeToken(token, lineNumber);
                    }
                }
                if (inString) {
                    errorHandler.addError("Unclosed string literal", lineNumber);
                }
            }
        }
        scanner.close();
        if (inMultiLineComment) {
            errorHandler.addError("Unclosed multi-line comment", lineNumber);
        }
    }

    public void printTokens() {
        for (Token token : tokens) {
            System.out.println(token);
        }
    }
    //for testing
    public boolean validateToken(String tokenType, String value) {
        DFA dfa = dfas.get(tokenType);
        return dfa != null && dfa.validate(value);
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
        System.out.println("Number of Tokens: " + analyzer.tokens.size());

        analyzer.symbolTable.printTable();
        analyzer.errorHandler.printErrors();
    }
}
