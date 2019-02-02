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

public class SSATransform extends AbstractVisitor {
    private Procedure procedure;
    private ReachingDefinitionsAnalysis rda;
    public SSATransform(CompilationUnit cu) {
        super(true);
        procedure = cu.getProcedure("main");
    }
    SSATransform(Procedure p) {
        super(true);
        procedure = p;
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

    void processJoins() {
        var joins = getJoins();
        for (State state : joins) {
            List<State> prec = new ArrayList<>();
            state.getIn().forEach(t -> prec.add(t.getSource()));
            for (State state1 : prec) insertNOP(state1, state);
        }
    }

    void insertAssignments(ReachingDefinitionsAnalysis rda) {
        var joins = getJoins();
        for (State state: joins) {
            var definitions = rda.getStateRepresentation(state);
            List<Variable> variables = definitions
                    .stream()
                    .map(reachingDefinition -> reachingDefinition.a)
                    .collect(Collectors.toList());
            List<Variable> used = new ArrayList<>();
            for (int i = 0; i < variables.size(); i++)
                for (int j = i + 1; j < variables.size(); j++)
                    if (variables.get(i).equals(variables.get(j)) && !used.contains(variables.get(i))) {
                        Variable variable = variables.get(i);
                        used.add(variable);
                        state.getIn().forEach(t -> {
                            Psi psi = ((Psi)t);
                            psi.addExpr(variable, variable);
                        });
                    }
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
            TransitionFactory.createPsi(newState, s2, new ArrayList<>(), new ArrayList<>());
            s2.deleteInEdge(edges[0]);
            s1.getMethod().refreshStates();
        }
    }
}
