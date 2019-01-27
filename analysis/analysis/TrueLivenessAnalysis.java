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

        CustomExpressionVisitor(HashSet<Expression> liveVars) {
            super(false);
            exprs = liveVars;
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

    public Set<Expression> visit(State s, Set<Expression> newFlow) {
        // let the new-flow have the vals of the previous state
        HashSet<Expression> liveVars = new HashSet<>(newFlow);

        Iterator<Transition> iterator = s.getInIterator();
        Set<Expression> union = Stream.generate(() -> null)
                .takeWhile(x -> iterator.hasNext())
                .map(n -> {
                    CustomExpressionVisitor visitor = new CustomExpressionVisitor(liveVars);
                    Transition transition = iterator.next();

                    transition.backwardAccept(visitor);

                    System.out.println("State id:" + s.getId());
                    System.out.println(visitor.exprs);
                    return visitor.exprs;
                })
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());

        dataflowOf(s, newFlow);
        return union;
    }

    String annotationRepresentationOfState(State s) {
        return dataflowOf(s).toString();
    }
}