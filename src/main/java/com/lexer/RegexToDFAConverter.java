package com.lexer;

import java.util.*;

class RegexToDFAConverter {

    // Convert a regular expression to a DFA
    public DFA convertRegexToDFA(String regex) {
        NFA nfa = regexToNFA(regex);
        DFA dfa = nfaToDFA(nfa);
        System.out.println("Transition Table for " + regex + ":");
        dfa.displayTable(); // Display the transition table for debugging
        return dfa;
    }

    // Convert a regular expression to an NFA using Thompson's construction
    private NFA regexToNFA(String regex) {
        Stack<NFA> stack = new Stack<>();
        System.out.println("Processing regex: " + regex);

        for (int i = 0; i < regex.length(); i++) {
            char c = regex.charAt(i);
            System.out.println("Processing character: " + c);

            switch (c) {
                case '[':
                    // Handle character class (e.g., [a-z] or [^"])
                    i = processCharacterClass(regex, i, stack);
                    break;
                case '*':
                    // Kleene star
                    if (stack.isEmpty()) {
                        throw new IllegalArgumentException("Invalid regex: Kleene star with no preceding expression");
                    }
                    stack.push(kleeneStar(stack.pop()));
                    break;
                case '+':
                    // One or more
                    if (stack.isEmpty()) {
                        throw new IllegalArgumentException("Invalid regex: '+' with no preceding expression");
                    }
                    NFA nfa = stack.pop();
                    stack.push(concatenate(nfa, kleeneStar(nfa)));
                    break;
                case '|':
                    // Union
                    if (stack.size() < 2) {
                        throw new IllegalArgumentException("Invalid regex: Union with less than 2 expressions");
                    }
                    NFA nfa2 = stack.pop();
                    NFA nfa1 = stack.pop();
                    stack.push(union(nfa1, nfa2));
                    break;
                case '(':
                    // Start of a group
                    i = processGroup(regex, i, stack);
                    break;
                case ')':
                    // End of a group (should be handled in processGroup)
                    throw new IllegalArgumentException("Invalid regex: Unmatched ')'");
                default:
                    // Single character
                    stack.push(singleCharNFA(c));
                    break;
            }
        }

        // Handle concatenation of remaining NFAs in the stack
        while (stack.size() > 1) {
            NFA nfa2 = stack.pop();
            NFA nfa1 = stack.pop();
            stack.push(concatenate(nfa1, nfa2));
        }

        if (stack.size() != 1) {
            System.out.println("Stack size at end: " + stack.size());
            throw new IllegalArgumentException("Invalid regex: Could not reduce to a single NFA");
        }

        return stack.pop();
    }


    // Process a character class (e.g., [a-z] or [^"])
    private int processCharacterClass(String regex, int index, Stack<NFA> stack) {
        StringBuilder charClass = new StringBuilder();
        index++; // Move past the '['
        boolean isNegated = false;

        if (index < regex.length() && regex.charAt(index) == '^') {
            isNegated = true;
            index++;
        }

        while (index < regex.length() && regex.charAt(index) != ']') {
            charClass.append(regex.charAt(index));
            index++;
        }

        if (index >= regex.length()) {
            throw new IllegalArgumentException("Invalid regex: Unclosed character class");
        }

        stack.push(characterClassNFA(charClass.toString(), isNegated));
        return index;
    }

    // Create an NFA for a character class (e.g., [a-z] or [^"])
    private NFA characterClassNFA(String charClass, boolean isNegated) {
        NFA result = null;
        for (int i = 0; i < charClass.length(); i++) {
            char c = charClass.charAt(i);
            if (i + 2 < charClass.length() && charClass.charAt(i + 1) == '-') {
                // Handle ranges (e.g., a-z)
                char start = c;
                char end = charClass.charAt(i + 2);
                for (char ch = start; ch <= end; ch++) {
                    NFA singleCharNFA = singleCharNFA(ch);
                    result = (result == null) ? singleCharNFA : union(result, singleCharNFA);
                }
                i += 2; // Skip the range characters
            } else {
                // Single character
                NFA singleCharNFA = singleCharNFA(c);
                result = (result == null) ? singleCharNFA : union(result, singleCharNFA);
            }
        }

        if (isNegated) {
            // Negate the character class
            NFA allCharsNFA = allCharsNFA();
            result = difference(allCharsNFA, result);
        }

        return result;
    }

    // Create an NFA that matches any character
    private NFA allCharsNFA() {
        NFA nfa = new NFA(new State(State.getNextId()), new HashSet<>());
        State endState = new State(State.getNextId());
        endState.isFinal = true;

        for (char c = 0; c < 128; c++) { // ASCII range
            nfa.startState.addTransition(c, endState);
        }

        nfa.finalStates.add(endState);
        return nfa;
    }

    // Create an NFA that matches the difference between two NFAs
    private NFA difference(NFA nfa1, NFA nfa2) {
        // Implement difference operation (e.g., using complement and intersection)
        throw new UnsupportedOperationException("Difference operation not implemented yet");
    }

    // Process a group (e.g., (true|false))
    private int processGroup(String regex, int index, Stack<NFA> stack) {
        Stack<NFA> groupStack = new Stack<>();
        index++; // Move past the '('
        while (index < regex.length() && regex.charAt(index) != ')') {
            char c = regex.charAt(index);

            switch (c) {
                case '[':
                    // Handle character class (e.g., [a-z])
                    index = processCharacterClass(regex, index, groupStack);
                    break;
                case '*':
                    // Kleene star
                    if (groupStack.isEmpty()) {
                        throw new IllegalArgumentException("Invalid regex: Kleene star with no preceding expression");
                    }
                    groupStack.push(kleeneStar(groupStack.pop()));
                    break;
                case '+':
                    // One or more
                    if (groupStack.isEmpty()) {
                        throw new IllegalArgumentException("Invalid regex: '+' with no preceding expression");
                    }
                    NFA nfa = groupStack.pop();
                    groupStack.push(concatenate(nfa, kleeneStar(nfa)));
                    break;
                case '|':
                    // Union
                    if (groupStack.size() < 2) {
                        throw new IllegalArgumentException("Invalid regex: Union with less than 2 expressions");
                    }
                    NFA nfa2 = groupStack.pop();
                    NFA nfa1 = groupStack.pop();
                    groupStack.push(union(nfa1, nfa2));
                    break;
                case '(':
                    // Nested group
                    index = processGroup(regex, index, groupStack);
                    break;
                default:
                    // Single character
                    groupStack.push(singleCharNFA(c));
                    break;
            }
            index++;
        }
        if (index >= regex.length()) {
            throw new IllegalArgumentException("Invalid regex: Unclosed group");
        }

        // Handle concatenation of remaining NFAs in the group stack
        while (groupStack.size() > 1) {
            NFA nfa2 = groupStack.pop();
            NFA nfa1 = groupStack.pop();
            groupStack.push(concatenate(nfa1, nfa2));
        }

        if (groupStack.size() != 1) {
            throw new IllegalArgumentException("Invalid regex: Could not reduce group to a single NFA");
        }

        stack.push(groupStack.pop());
        return index;
    }

    // Create an NFA for a character class (e.g., [a-z])
    private NFA characterClassNFA(String charClass) {
        NFA result = null;
        for (int i = 0; i < charClass.length(); i++) {
            char c = charClass.charAt(i);
            if (i + 2 < charClass.length() && charClass.charAt(i + 1) == '-') {
                // Handle ranges (e.g., a-z)
                char start = c;
                char end = charClass.charAt(i + 2);
                for (char ch = start; ch <= end; ch++) {
                    NFA singleCharNFA = singleCharNFA(ch);
                    result = (result == null) ? singleCharNFA : union(result, singleCharNFA);
                }
                i += 2; // Skip the range characters
            } else {
                // Single character
                NFA singleCharNFA = singleCharNFA(c);
                result = (result == null) ? singleCharNFA : union(result, singleCharNFA);
            }
        }
        return result;
    }

    // Create an NFA for a single character
    private NFA singleCharNFA(char c) {
        State start = new State(State.getNextId());
        State end = new State(State.getNextId());
        end.isFinal = true;

        start.addTransition(c, end);

        Set<State> finalStates = new HashSet<>();
        finalStates.add(end);

        return new NFA(start, finalStates);
    }

    // Concatenate two NFAs
    private NFA concatenate(NFA nfa1, NFA nfa2) {
        for (State state : nfa1.finalStates) {
            state.isFinal = false;
            state.addTransition('\0', nfa2.startState); // Epsilon transition
        }

        Set<State> finalStates = new HashSet<>(nfa2.finalStates);
        return new NFA(nfa1.startState, finalStates);
    }

    // Union of two NFAs
    private NFA union(NFA nfa1, NFA nfa2) {
        State start = new State(State.getNextId());
        State end = new State(State.getNextId());
        end.isFinal = true;

        start.addTransition('\0', nfa1.startState); // Epsilon transition
        start.addTransition('\0', nfa2.startState); // Epsilon transition

        for (State state : nfa1.finalStates) {
            state.isFinal = false;
            state.addTransition('\0', end); // Epsilon transition
        }

        for (State state : nfa2.finalStates) {
            state.isFinal = false;
            state.addTransition('\0', end); // Epsilon transition
        }

        Set<State> finalStates = new HashSet<>();
        finalStates.add(end);

        return new NFA(start, finalStates);
    }

    // Kleene star of an NFA
    private NFA kleeneStar(NFA nfa) {
        State start = new State(State.getNextId());
        State end = new State(State.getNextId());
        end.isFinal = true;

        start.addTransition('\0', nfa.startState); // Epsilon transition
        start.addTransition('\0', end); // Epsilon transition

        for (State state : nfa.finalStates) {
            state.isFinal = false;
            state.addTransition('\0', nfa.startState); // Epsilon transition
            state.addTransition('\0', end); // Epsilon transition
        }

        Set<State> finalStates = new HashSet<>();
        finalStates.add(end);

        return new NFA(start, finalStates);
    }

    // Convert an NFA to a DFA using subset construction
    private DFA nfaToDFA(NFA nfa) {
        Map<Set<State>, Integer> stateMap = new HashMap<>();
        Queue<Set<State>> queue = new LinkedList<>();
        DFA dfa = new DFA(0);

        Set<State> startSet = epsilonClosure(Collections.singleton(nfa.startState));
        stateMap.put(startSet, 0);
        queue.add(startSet);

        int nextStateId = 1;

        while (!queue.isEmpty()) {
            Set<State> currentSet = queue.poll();
            int currentStateId = stateMap.get(currentSet);

            // Check if this set contains a final state
            for (State state : currentSet) {
                if (nfa.finalStates.contains(state)) {
                    dfa.addFinalState(currentStateId);
                    break;
                }
            }

            // Compute transitions for each input symbol
            Map<Character, Set<State>> transitions = new HashMap<>();
            for (State state : currentSet) {
                for (Map.Entry<Character, Set<State>> entry : state.transitions.entrySet()) {
                    char symbol = entry.getKey();
                    if (symbol != '\0') { // Ignore epsilon transitions
                        transitions.putIfAbsent(symbol, new HashSet<>());
                        transitions.get(symbol).addAll(entry.getValue());
                    }
                }
            }

            // Create new states and transitions in the DFA
            for (Map.Entry<Character, Set<State>> entry : transitions.entrySet()) {
                char symbol = entry.getKey();
                Set<State> nextSet = epsilonClosure(entry.getValue());

                if (!stateMap.containsKey(nextSet)) {
                    stateMap.put(nextSet, nextStateId);
                    queue.add(nextSet);
                    nextStateId++;
                }

                int nextState = stateMap.get(nextSet);
                dfa.addTransition(currentStateId, symbol, nextState);
            }
        }

        return dfa;
    }

    // Compute the epsilon closure of a set of states
    private Set<State> epsilonClosure(Set<State> states) {
        Set<State> closure = new HashSet<>(states);
        Queue<State> queue = new LinkedList<>(states);

        while (!queue.isEmpty()) {
            State state = queue.poll();
            for (State nextState : state.transitions.getOrDefault('\0', Collections.emptySet())) {
                if (!closure.contains(nextState)) {
                    closure.add(nextState);
                    queue.add(nextState);
                }
            }
        }

        return closure;
    }
}