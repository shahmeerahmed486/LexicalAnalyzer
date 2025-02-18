package com.lexer;

import java.util.*;

class State {
    int id;
    Map<Character, Set<State>> transitions;
    boolean isFinal;

    private static int nextId = 0;

    public State(int id) {
        this.id = id;
        this.transitions = new HashMap<>();
        this.isFinal = false;
    }

    public void addTransition(char symbol, State state) {
        transitions.putIfAbsent(symbol, new HashSet<>());
        transitions.get(symbol).add(state);
    }

    public static int getNextId() {
        return nextId++;
    }
}