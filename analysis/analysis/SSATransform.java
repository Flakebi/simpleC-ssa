package analysis;

import petter.cfg.*;
import petter.cfg.edges.Assignment;
import petter.cfg.edges.GuardedTransition;
import petter.cfg.edges.Psi;
import petter.cfg.edges.Transition;
import petter.cfg.expression.BinaryExpression;
import petter.cfg.expression.Expression;
import petter.cfg.expression.IntegerConstant;
import petter.cfg.expression.Variable;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class SSATransform {
    public Procedure procedure;
    SSATransform() {
    }

    private List<State> getJoins() {
        List<State> joins = new ArrayList<>();
        procedure.getStates().forEach(s -> {
            AtomicInteger num = new AtomicInteger();
            s.getIn().forEach((s1) -> num.getAndIncrement());
            if (num.get() > 1)
                joins.add(s);
        });
        return joins;
    }

    private  List<Transition> getJoinEdges() {
        List<Transition> joinEdges = new ArrayList<>();
        for (State s : procedure.getStates()) {
            AtomicInteger num = new AtomicInteger();
            s.getIn().forEach(t -> num.getAndIncrement());
            if (num.get() > 1)
                s.getIn().forEach(joinEdges::add);
        }
        return joinEdges;
    }

    void processJoins() {
        var joins = getJoinEdges();
        for (Transition transition : joins)
            insertNOP(transition);
    }

    void insertAssignments(ReachingDefinitionsAnalysis rda) {
        var joins = getJoins();
        for (State state: joins) {
            var definitions = rda.getStateRepresentation(state);
            List<Variable> variables = definitions
                    .stream()
                    .map(reachingDefinition -> reachingDefinition.a)
                    .collect(Collectors.toList());
            Map<Variable, Long> counts = variables
                    .stream()
                    .collect(
                            Collectors.groupingBy(v -> v, Collectors.counting())
                    );
            counts.forEach((v,c) -> {
                if (c >= 2) {
                    state.getIn().forEach(t -> {
                        Psi psi = (Psi)t;
                        psi.addExpr(v, v);
                    });
                }
            });
        }
    }

    void replaceVars(ReachingDefinitionsAnalysis rda) {
        var transitions = procedure.getTransitions();
        Map<String, Variable> addedVars = new HashMap<>();
        int id = 1;
        for (Transition transition: transitions) {
            if (transition instanceof Assignment) {
                Assignment assignment = (Assignment)transition;
                if (assignment.getLhs() instanceof Variable) {
                    Variable lhs = (Variable)assignment.getLhs();
                    String name = (String)lhs.getAnnotation("external name");
                    if (name != "return") {
                        name += transition.getDest().getId();
                        if (addedVars.get(name) == null) {
                            Variable newVar = new Variable(id, name, lhs.getType());
                            assignment.setLhs(newVar);
                            id++;
                            addedVars.put(name, newVar);
                        }
                        else {
                            assignment.setLhs(addedVars.get(name));
                        }
                    }
                }
            }
            if (transition instanceof Psi) {
                Psi psi = (Psi)transition;
                System.out.println("psi");
                for (int i = 0; i < psi.getLhs().size(); i++) {
                    var newLhs = psi.getLhs().get(i);
                    if (newLhs instanceof Variable) {
                        Variable lhs = (Variable)newLhs;
                        String name = (String)lhs.getAnnotation("external name");
                        if (name != "return") {
                            name += transition.getDest().getId();
                            if (addedVars.get(name) == null) {
                                Variable newVar = new Variable(id, name, lhs.getType());
                                psi.setOneLhs(newVar, i);
                                id++;
                                addedVars.put(name, newVar);
                            }
                            else
                            {
                                psi.setOneLhs(addedVars.get(name), i);
                            }
                        }
                    }
                }
            }
        }

        for (Transition transition: transitions) {
            if (transition instanceof Assignment) {
                Assignment assignment = (Assignment)transition;
                assignment.setRhs(recursiveReplace(rda, addedVars, assignment.getRhs(), transition.getSource()));
            }
            if (transition instanceof Psi) {
                Psi psi = (Psi)transition;
                for (int i = 0; i < psi.getRhs().size(); i++) {
                    psi.setOneRhs(recursiveReplace(rda, addedVars, psi.getRhs().get(i), psi.getSource()), i);
                }
            }
            if (transition instanceof GuardedTransition) {
                GuardedTransition gT = (GuardedTransition)transition;
                gT.setAssertion(recursiveReplace(rda, addedVars, gT.getAssertion(), gT.getSource()));
            }
        }
    }

    private Expression recursiveReplace(ReachingDefinitionsAnalysis rda,
                                        Map<String, Variable> addedVars, Expression e, State state) {
        if (e instanceof Variable) {
            Variable eVar = (Variable)e;
            String name = (String)eVar.getAnnotation("external name");
            Variable finalEVar = eVar;
            var defs = rda.getStateRepresentation(state)
                    .stream()
                    .filter(r -> r.a.equals(finalEVar))
                    .findFirst();
            if (defs.isPresent()) {
                name += defs.get().b.toString();
                Variable newVar = addedVars.get(name);
                eVar = newVar;
            }
            return eVar;
        }
        else if (e instanceof BinaryExpression) {
            BinaryExpression binExpr = (BinaryExpression)e;
            binExpr.setLeft(recursiveReplace(rda, addedVars, binExpr.getLeft(), state));
            binExpr.setRight(recursiveReplace(rda, addedVars, binExpr.getRight(), state));
        }
        else return e;
        return e;
    }

    private void insertNOP(Transition transition) {
        State s1 = transition.getSource();
        State s2 = transition.getDest();
        State newState = new State();
        transition.setDest(newState);
        TransitionFactory.createPsi(newState, s2, new ArrayList<>(), new ArrayList<>());
        s2.deleteInEdge(transition);
        s1.getMethod().refreshStates();
    }
}
