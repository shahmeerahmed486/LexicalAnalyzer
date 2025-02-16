package com.lexer;

import java.util.HashMap;

public class SymbolTable {
    private HashMap<String, Symbol> table;

    public SymbolTable() {
        this.table = new HashMap<>();
    }

    public void insert(String name, String type, String scope, String value) {
        if (!table.containsKey(name)) {
            Symbol symbol = new Symbol(name, type, scope, value);
            table.put(name, symbol);
        }
    }

    public Symbol lookup(String name) {
        return table.get(name);
    }

    public boolean exists(String name) {
        return table.containsKey(name);
    }

    public void printTable() {
        System.out.println("\n--- Symbol Table ---");
        for (Symbol symbol : table.values()) {
            System.out.println(symbol);
        }
    }
}
