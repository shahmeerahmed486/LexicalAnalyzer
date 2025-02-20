package com.lexer;

public class Symbol {
    private String name;
    private String type;
    private String scope;
    private String value;

    public Symbol(String name, String type, String scope, String value) {
        this.name = name;
        this.type = type;
        this.scope = scope;
        this.value = value;
    }

    public String getName() { return name; }
    public String getType() { return type; }
    public String getScope() { return scope; }
    public String getValue() { return value; }

    @Override
    public String toString() {
        return "Symbol{name='" + name + "', type='" + type + "', scope='" + scope + "', value='" + value + "'}";
    }
}
