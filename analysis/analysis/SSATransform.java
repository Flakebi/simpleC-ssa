package analysis;

import petter.cfg.*;
import petter.cfg.edges.Transition;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class SSATransform extends AbstractVisitor {
    private  Procedure procedure;
    public SSATransform(CompilationUnit cu) {
        super(true);
        procedure = cu.getProcedure("main");
    }
    SSATransform(Procedure p) {
        super(true);
        procedure = p;
    }

    void processJoins() {
        List<State> joins = new ArrayList<>();
        procedure.getStates().forEach(s -> {
            AtomicInteger num = new AtomicInteger();
            s.getIn().forEach((s1) -> num.getAndIncrement());
            if (num.get() > 1)
                joins.add(s);
        });
        for (State state : joins) {
            List<State> prec = new ArrayList<>();
            state.getIn().forEach(t -> prec.add(t.getSource()));
            for (State state1 : prec) insertNOP(state1, state);
        }
    }

    private void insertNOP(State s1, State s2) {
        var outEdges = s1.getOut();
        final boolean[] connected = {false};
        final Transition[] edges = new Transition[1];
        outEdges.forEach(s -> {
            if (s.getDest().equals(s2)) {
                connected[0] = true;
                edges[0] = s;
            }
        });
        if (connected[0]) {
            State newState = new State();
            edges[0].setDest(newState);
            TransitionFactory.createNop(newState, s2);
            s2.deleteInEdge(edges[0]);
            s1.getMethod().refreshStates();
        }
    }
}
