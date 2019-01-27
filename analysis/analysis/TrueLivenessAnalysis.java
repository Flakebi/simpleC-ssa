package analysis;

import petter.cfg.*;
import petter.cfg.edges.*;
import petter.cfg.expression.BinaryExpression;
import petter.cfg.expression.Expression;
import petter.cfg.expression.Variable;
import petter.cfg.expression.visitors.AbstractExpressionVisitor;
import petter.cfg.expression.visitors.ExpressionVisitor;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TrueLivenessAnalysis extends AbstractPropagatingVisitor<Set<Expression>> {
    class CustomExpressionVisitor extends AbstractVisitor {
        Set<Expression> exprs;

        ExpressionVisitor variableVisitor = new AbstractExpressionVisitor() {
            @Override
            public void postVisit(Variable s) {
                exprs.add(s);
                super.postVisit(s);
            }
        };

        CustomExpressionVisitor(Set<Expression> liveVars) {
            super(false);
            exprs = new HashSet<>(liveVars);
        }

        @Override
        public boolean visit(GuardedTransition s) {
            s.getAssertion().accept(variableVisitor);
            return super.visit(s);
        }

        @Override
        public boolean visit(Assignment s) {
            if (exprs.contains(s.getLhs())) {
                s.getRhs().accept(variableVisitor);
            }

            exprs.remove(s.getLhs());
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
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());

        Set<Expression> oldVal = dataflowOf(s);
        if (oldVal == null) {
            dataflowOf(s, liveVars);
        } else {
            oldVal.addAll(liveVars);
        }

        return union;
    }

    String annotationRepresentationOfState(State s) {
        return "L[" + s.getId() + "]=" + dataflowOf(s).toString();
    }
}