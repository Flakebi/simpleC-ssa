package analysis;

import petter.cfg.AbstractPropagatingVisitor;
import petter.cfg.AbstractVisitor;
import petter.cfg.CompilationUnit;
import petter.cfg.State;
import petter.cfg.edges.Assignment;
import petter.cfg.edges.GuardedTransition;
import petter.cfg.edges.ProcedureCall;
import petter.cfg.edges.Transition;
import petter.cfg.expression.Variable;
import petter.cfg.expression.visitors.AbstractExpressionVisitor;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Stream;

public class TrueLivenessAnalysis extends AbstractPropagatingVisitor<Set<Variable>> {
    class VariableVisitor extends AbstractExpressionVisitor {
        Set<Variable> exprs = new HashSet<>();

        @Override
        public void postVisit(Variable s) {
            exprs.add(s);
            super.postVisit(s);
        }
    }

    class CustomExpressionVisitor extends AbstractVisitor {
        Set<Variable> exprs;

        CustomExpressionVisitor(Set<Variable> liveVars) {
            super(false);
            exprs = new HashSet<>(liveVars);
        }

        @Override
        public boolean visit(GuardedTransition s) {
            VariableVisitor variableVisitor = new VariableVisitor();
            s.getAssertion().accept(variableVisitor);
            exprs.addAll(variableVisitor.exprs);
            return super.visit(s);
        }

        @Override
        public boolean visit(Assignment s) {
            VariableVisitor variableVisitor = new VariableVisitor();

            if (exprs.contains(s.getLhs())) {
                s.getRhs().accept(variableVisitor);
            }

            exprs.remove(s.getLhs());
            exprs.addAll(variableVisitor.exprs);

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
        Iterator<Transition> iterator = state.getInIterator();

        Set<Variable> oldFlow = dataflowOf(state);
        Set<Variable> newFlow = Stream
                .generate(() -> null)
                .takeWhile(x -> iterator.hasNext())
                .map(n -> {
                    Transition transition = iterator.next();
                    CustomExpressionVisitor visitor = new CustomExpressionVisitor(parentFlow);

                    transition.backwardAccept(visitor);
                    return visitor.exprs;
                })
                .reduce((s1, s2) -> {
                    Set<Variable> intersection = new HashSet<>(s1);
                    intersection.retainAll(s2);
                    return intersection;
                })
                .orElse(new HashSet<>());

        if (!lessOrEq(parentFlow, oldFlow)) {
            dataflowOf(state, lub(oldFlow, parentFlow));
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