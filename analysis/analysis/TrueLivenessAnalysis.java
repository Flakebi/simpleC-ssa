package analysis;

import petter.cfg.AbstractPropagatingVisitor;
import petter.cfg.AbstractVisitor;
import petter.cfg.CompilationUnit;
import petter.cfg.State;
import petter.cfg.edges.*;
import petter.cfg.expression.Variable;
import petter.cfg.expression.visitors.AbstractExpressionVisitor;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Stream;

public class TrueLivenessAnalysis extends AbstractPropagatingVisitor<Set<Variable>> {
    class VariableVisitor extends AbstractExpressionVisitor {
        Set<Variable> vars = new HashSet<>();

        @Override
        public void postVisit(Variable s) {
            vars.add(s);
            super.postVisit(s);
        }
    }

    class CustomTransitionVisitor extends AbstractVisitor {
        Set<Variable> vars;

        CustomTransitionVisitor(Set<Variable> liveVars) {
            super(false);
            vars = new HashSet<>(liveVars);
        }

        @Override
        public boolean visit(GuardedTransition s) {
            VariableVisitor variableVisitor = new VariableVisitor();
            s.getAssertion().accept(variableVisitor);
            vars.addAll(variableVisitor.vars);
            return super.visit(s);
        }

        @Override
        public boolean visit(Assignment s) {
            VariableVisitor variableVisitor = new VariableVisitor();

            if (vars.contains(s.getLhs())) {
                s.getRhs().accept(variableVisitor);
            }

            vars.remove(s.getLhs());
            vars.addAll(variableVisitor.vars);

            return super.visit(s);
        }

        @Override
        public boolean visit(Psi s) {
            Set<Variable> toBeAdded = new HashSet<>();
            Set<Variable> toBeRemoved = new HashSet<>();

            for (int i = 0; i < s.getLhs().size(); i++) {
                VariableVisitor variableVisitor = new VariableVisitor();

                if (vars.contains(s.getLhs().get(i))) {
                    s.getRhs().get(i).accept(variableVisitor);
                }

                toBeRemoved.add((Variable) s.getLhs().get(i));
                toBeAdded.addAll(variableVisitor.vars);
            }

            vars.removeAll(toBeRemoved);
            vars.addAll(toBeAdded);

            return super.visit(s);
        }
    }

    private CompilationUnit cu;

    public TrueLivenessAnalysis(CompilationUnit cu) {
        super(false);
        this.cu = cu;
    }

    public Set<Variable> visit(ProcedureCall procedureCall, Set<Variable> expressionSet) {
        return expressionSet;
    }

    public Set<Variable> visit(State state, Set<Variable> parentFlow) {
        Iterator<Transition> iterator = state.getOutIterator();

        Set<Variable> oldFlow = dataflowOf(state);
        Set<Variable> newFlow = Stream
                .generate(() -> null)
                .takeWhile(x -> iterator.hasNext())
                // map every outgoing edge to live vars
                .map(n -> {
                    Transition transition = iterator.next();
                    CustomTransitionVisitor visitor = new CustomTransitionVisitor(parentFlow);
                    transition.backwardAccept(visitor);
                    return visitor.vars;
                })
                // union them together
                .reduce((s1, s2) -> {
                    s1.addAll(s2);
                    return s1;
                })
                .orElse(parentFlow);

        if (!lessOrEq(newFlow, oldFlow)) {
            dataflowOf(state, lub(oldFlow, newFlow));
            return newFlow;
        }

        return null;
    }

    private static Set<Variable> lub(Set<Variable> oldFlow, Set<Variable> newFlow) {
        if (oldFlow == null) {
            return newFlow;
        }

        newFlow.addAll(oldFlow);
        return newFlow;
    }

    private static boolean lessOrEq(Set<Variable> s1, Set<Variable> s2) {
        if (s1 == null) return true;
        if (s2 == null) return false;
        return s2.containsAll(s1);
    }

    String annotationRepresentationOfState(State s) {
        return "L[" + s.getId() + "]=" + dataflowOf(s).toString();
    }
}