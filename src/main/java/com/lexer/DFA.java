package com.lexer;

import java.util.*;

class DFA {
    private final Map<String, Integer> transitionTable;
    private final Set<Integer> finalStates;
    private final int startState;
    private int currentState;

    public DFA(int startState) {
        this.startState = startState;
        this.currentState = startState;
        this.transitionTable = new HashMap<>();
        this.finalStates = new HashSet<>();
    }

    // Add a transition (currentState, inputChar) -> nextState
    public void addTransition(int currentState, char inputChar, int nextState) {
        String key = currentState + "," + inputChar;
        transitionTable.put(key, nextState);
    }

    // Define final/accepting states
    public void addFinalState(int state) {
        finalStates.add(state);
    }

    // Function to reset DFA to start state
    public void reset() {
        currentState = startState;
    }

    // Function to get the next state based on input character
    public int getNextState(int currentState, char inputChar) {
        String key = currentState + "," + inputChar;
        return transitionTable.getOrDefault(key, -1); // Return -1 for invalid transitions
    }

    // Process an input string to check if it is a valid token
    public boolean validate(String input) {
        reset(); // Start from the initial state
        for (char c : input.toCharArray()) {
            if (c == ' ') continue; // Ignore spaces
            int nextState = getNextState(currentState, c);
            if (nextState == -1) return false; // Invalid transition
            currentState = nextState;
        }
        return finalStates.contains(currentState); // Return true if final state reached
    }

    // Display transition table for debugging
    public void displayTable() {
        System.out.println("Transition Table:");
        System.out.println("------------------");
        System.out.println("Current State | Input | Next State");
        for (Map.Entry<String, Integer> entry : transitionTable.entrySet()) {
            System.out.println(entry.getKey().replace(",", "      |   ") + "   |   " + entry.getValue());
        }
    }
}
