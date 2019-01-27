package analysis;

import petter.cfg.AbstractPropagatingVisitor;
import petter.cfg.AbstractVisitor;
import petter.cfg.CompilationUnit;
import petter.cfg.State;
import petter.cfg.edges.Assignment;
import petter.cfg.edges.GuardedTransition;
import petter.cfg.edges.ProcedureCall;
import petter.cfg.edges.Transition;
import petter.cfg.expression.Expression;
import petter.cfg.expression.Variable;
import petter.cfg.expression.visitors.AbstractExpressionVisitor;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Stream;

public class TrueLivenessAnalysis extends AbstractPropagatingVisitor<Set<Expression>> {
    class VariableVisitor extends AbstractExpressionVisitor {
        Set<Expression> exprs = new HashSet<>();

        @Override
        public void postVisit(Variable s) {
            exprs.add(s);
            super.postVisit(s);
        }
    }

    class CustomExpressionVisitor extends AbstractVisitor {
        Set<Expression> exprs;

        CustomExpressionVisitor(Set<Expression> liveVars) {
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

    public Set<Expression> visit(ProcedureCall procedureCall, Set<Expression> expressionSet) {
        return null;
    }

    public Set<Expression> visit(State s, Set<Expression> liveVars) {
        Iterator<Transition> iterator = s.getInIterator();
        Set<Expression> union = Stream
                .generate(() -> null)
                .takeWhile(x -> iterator.hasNext())
                .map(n -> {
                    Transition transition = iterator.next();
                    CustomExpressionVisitor visitor = new CustomExpressionVisitor(liveVars);

                    transition.backwardAccept(visitor);
                    return visitor.exprs;
                })
                .reduce((s1, s2) -> {
                    Set<Expression> intersection = new HashSet<>(s1);
                    intersection.retainAll(s2);
                    return intersection;
                })
                .orElse(new HashSet<>());

        Set<Expression> oldVal = dataflowOf(s);
        Set<Expression> newVal;

        if (oldVal == null) {
            newVal = liveVars;
        } else {
            newVal = new HashSet<>(oldVal);
            newVal.addAll(liveVars);
        }

        dataflowOf(s, newVal);

        if (oldVal != null && oldVal.equals(newVal)) { return null; }

        return union;
    }

    String annotationRepresentationOfState(State s) {
        return "L[" + s.getId() + "]=" + dataflowOf(s).toString();
    }
}