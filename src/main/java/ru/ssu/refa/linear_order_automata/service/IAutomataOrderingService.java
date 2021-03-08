package ru.ssu.refa.linear_order_automata.service;

import ru.ssu.refa.linear_order_automata.model.Automaton;
import ru.ssu.refa.linear_order_automata.model.LinearOrder;

public interface IAutomataOrderingService {

    LinearOrder orderAutomaton(Automaton automaton);

}
