package com.lexer;

import java.util.*;

class RegexToDFAConverter {

    //regular expression to a DFA
    public DFA convertRegexToDFA(String regex) {
        NFA nfa = regexToNFA(regex);
        DFA dfa = nfaToDFA(nfa);
        if(regex!="\"[^\"]*\"") {
            System.out.println("Transition Table for " + regex + ":");
            dfa.displayTable();
        }
        return dfa;
    }


    private NFA regexToNFA(String regex) {
        Stack<NFA> stack = new Stack<>();

        for (int i = 0; i < regex.length(); i++) {
            char c = regex.charAt(i);

            switch (c) {
                case '\\':
                    if (i + 1 < regex.length()) {
                        i++;
                        char literal = regex.charAt(i);
                        stack.push(singleCharNFA(literal));
                    } else {
                        throw new IllegalArgumentException("Invalid regex: Trailing backslash");
                    }
                    break;
                case '[':
                    // Handle character class (e.g., [a-z])
                    i = processCharacterClass(regex, i, stack);
                    break;
                case '*':

                    if (stack.isEmpty()) {
                        throw new IllegalArgumentException("Invalid regex: Kleene star with no preceding expression");
                    }
                    stack.push(kleeneStar(stack.pop()));
                    break;
                case '+':

                    if (stack.isEmpty()) {
                        throw new IllegalArgumentException("Invalid regex: '+' with no preceding expression");
                    }
                    NFA nfa = stack.pop();
                    stack.push(concatenate(nfa, kleeneStar(nfa)));
                    break;
                case '|':

                    if (stack.size() < 2) {
                        throw new IllegalArgumentException("Invalid regex: Union with less than 2 expressions");
                    }
                    NFA nfa2 = stack.pop();
                    NFA nfa1 = stack.pop();
                    stack.push(union(nfa1, nfa2));
                    break;
                case '(':

                    i = processGroup(regex, i, stack);
                    break;
                case ')':

                    throw new IllegalArgumentException("Invalid regex: Unmatched ')'");
                default:

                    stack.push(singleCharNFA(c));
                    break;
            }
        }


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


    private int processCharacterClass(String regex, int index, Stack<NFA> stack) {
        StringBuilder charClass = new StringBuilder();
        index++; // Move past the '['
        while (index < regex.length() && regex.charAt(index) != ']') {
            charClass.append(regex.charAt(index));
            index++;
        }
        if (index >= regex.length()) {
            throw new IllegalArgumentException("Invalid regex: Unclosed character class");
        }
        stack.push(characterClassNFA(charClass.toString()));
        return index;
    }


    private int processGroup(String regex, int index, Stack<NFA> stack) {
        Stack<NFA> groupStack = new Stack<>();
        List<NFA> alternatives = new ArrayList<>(); // Store different alternates for union
        index++;

        while (index < regex.length() && regex.charAt(index) != ')') {
            char c = regex.charAt(index);

            switch (c) {
                case '\\':
                    if (index + 1 < regex.length()) {
                        index++;
                        char literal = regex.charAt(index);
                        groupStack.push(singleCharNFA(literal));
                    } else {
                        throw new IllegalArgumentException("Invalid regex: Trailing backslash in group");
                    }
                    break;
                case '[':
                    index = processCharacterClass(regex, index, groupStack);
                    break;
                case '*':
                    if (groupStack.isEmpty()) {
                        throw new IllegalArgumentException("Invalid regex: Kleene star with no preceding expression");
                    }
                    groupStack.push(kleeneStar(groupStack.pop()));
                    break;
                case '+':
                    if (groupStack.isEmpty()) {
                        throw new IllegalArgumentException("Invalid regex: '+' with no preceding expression");
                    }
                    NFA nfa = groupStack.pop();
                    groupStack.push(concatenate(nfa, kleeneStar(nfa)));
                    break;
                case '|':

                    if (!groupStack.isEmpty()) {
                        NFA expr = reduceStackToSingleNFA(groupStack);
                        alternatives.add(expr);
                    }
                    break;
                case '(':
                    index = processGroup(regex, index, groupStack);
                    break;
                default:
                    groupStack.push(singleCharNFA(c));
                    break;
            }
            index++;
        }

        if (index >= regex.length()) {
            throw new IllegalArgumentException("Invalid regex: Unclosed group");
        }


        if (!groupStack.isEmpty()) {
            alternatives.add(reduceStackToSingleNFA(groupStack));
        }

        NFA result = alternatives.get(0);
        for (int i = 1; i < alternatives.size(); i++) {
            result = union(result, alternatives.get(i));
        }

        stack.push(result);
        return index;
    }


    private NFA reduceStackToSingleNFA(Stack<NFA> stack) {
        while (stack.size() > 1) {
            NFA nfa2 = stack.pop();
            NFA nfa1 = stack.pop();
            stack.push(concatenate(nfa1, nfa2));
        }
        return stack.pop();
    }


    private NFA characterClassNFA(String charClass) {
        boolean isNegated = false;
        if (charClass.startsWith("^")) {
            isNegated = true;
            charClass = charClass.substring(1); // Remove the '^' symbol
        }

        NFA result = null;
        Set<Character> includedChars = new HashSet<>();

        for (int i = 0; i < charClass.length(); i++) {
            char c = charClass.charAt(i);
            if (i + 2 < charClass.length() && charClass.charAt(i + 1) == '-') {
                // Handle ranges (e.g., a-z)
                char start = c;
                char end = charClass.charAt(i + 2);
                for (char ch = start; ch <= end; ch++) {
                    includedChars.add(ch);
                }
                i += 2; // Skip the range characters
            } else {
                includedChars.add(c);
            }
        }

        if (isNegated) {
            // Create an NFA that accepts any character except the ones in includedChars
            for (char ch = 32; ch <= 126; ch++) { // ASCII printable range
                if (!includedChars.contains(ch)) {
                    NFA singleCharNFA = singleCharNFA(ch);
                    result = (result == null) ? singleCharNFA : union(result, singleCharNFA);
                }
            }
        } else {
            // Create an NFA for only the included characters
            for (char ch : includedChars) {
                NFA singleCharNFA = singleCharNFA(ch);
                result = (result == null) ? singleCharNFA : union(result, singleCharNFA);
            }
        }

        return result;
    }


    private NFA singleCharNFA(char c) {
        State start = new State(State.getNextId());
        State end = new State(State.getNextId());
        end.isFinal = true;

        start.addTransition(c, end);

        Set<State> finalStates = new HashSet<>();
        finalStates.add(end);

        return new NFA(start, finalStates);
    }


    private NFA concatenate(NFA nfa1, NFA nfa2) {
        for (State state : nfa1.finalStates) {
            state.isFinal = false;
            state.addTransition('\0', nfa2.startState); // Epsilon transition
        }

        Set<State> finalStates = new HashSet<>(nfa2.finalStates);
        return new NFA(nfa1.startState, finalStates);
    }


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
