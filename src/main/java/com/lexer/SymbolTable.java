package com.lexer;

import java.util.HashMap;
import java.util.Map;

public class SymbolTable {
    // Use a composite key (name, scope) to ensure uniqueness
    private Map<String, Symbol> table;

    public SymbolTable() {
        this.table = new HashMap<>();
    }

    // Insert a symbol into the symbol table using name and scope as a composite key
    public void insert(String name, String type, String scope, String value) {
        String key = generateKey(name, scope);
        if (!table.containsKey(key)) {
            Symbol symbol = new Symbol(name, type, scope, value);
            table.put(key, symbol);
        }
    }

    // Lookup a symbol by its name and scope
    public Symbol lookup(String name, String scope) {
        String key = generateKey(name, scope);
        return table.get(key);
    }

    // Check if a symbol exists by name and scope
    public boolean exists(String name, String scope) {
        String key = generateKey(name, scope);
        return table.containsKey(key);
    }

    // Generate a composite key using both name and scope
    private String generateKey(String name, String scope) {
        return name + ":" + scope;  // Combining name and scope with a delimiter
    }

    // Print the symbol table
    public void printTable() {
        System.out.println("\n--- Symbol Table ---");
        for (Symbol symbol : table.values()) {
            System.out.println(symbol);
        }
    }
}
