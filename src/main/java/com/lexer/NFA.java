package com.lexer;

import java.util.*;

class NFA {
    State startState;
    Set<State> finalStates;

    public NFA(State startState, Set<State> finalStates) {
        this.startState = startState;
        this.finalStates = finalStates;
    }
}
