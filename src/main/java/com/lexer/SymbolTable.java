package com.lexer;

import java.util.HashMap;
import java.util.Map;

public class SymbolTable {

    private Map<String, Symbol> table;

    public SymbolTable() {
        this.table = new HashMap<>();
    }


    public void insert(String name, String type, String scope, String value) {
        String key = generateKey(name, scope);
        if (!table.containsKey(key)) {
            Symbol symbol = new Symbol(name, type, scope, value);
            table.put(key, symbol);
        }
    }


    public boolean exists(String name, String scope) {
        String key = generateKey(name, scope);
        return table.containsKey(key);
    }


    private String generateKey(String name, String scope) {
        return name + ":" + scope;  // Combining name and scope with a delimiter
    }


    public void printTable() {
        System.out.println("\n--- Symbol Table ---");
        for (Symbol symbol : table.values()) {
            System.out.println(symbol);
        }
    }
}
