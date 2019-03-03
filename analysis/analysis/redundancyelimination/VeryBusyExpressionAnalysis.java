package analysis.redundancyelimination;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import petter.cfg.AbstractPropagatingVisitor;
import petter.cfg.State;
import petter.cfg.edges.Assignment;
import petter.cfg.edges.GuardedTransition;
import petter.cfg.edges.ProcedureCall;
import petter.cfg.expression.Expression;
import petter.cfg.expression.UnknownExpression;
import petter.cfg.expression.Variable;
import petter.cfg.expression.visitors.NoVal;

public class VeryBusyExpressionAnalysis extends AbstractPropagatingVisitor<Set<Expression>> {
    // lower upper bound
    static Set<Expression> lub(Set<Expression> s1, Set<Expression> s2) {
        if (s1 == null) return s2;
        if (s2 == null) return s1;
        // Intersection
        HashSet<Expression> res = new HashSet<>(s1);
        res.retainAll(s2);
        return res;
    }

    static boolean lessoreq(Set<Expression> s1, Set<Expression> s2) {
        if (s1 == null) return true;
        if (s2 == null) return false;
        return s1.containsAll(s2);
    }

    public VeryBusyExpressionAnalysis() {
        super(false);
    }

    @Override
    public Set<Expression> visit(ProcedureCall ae, Set<Expression> d) {
        return d;
    }

    @Override
    public Set<Expression> visit(Assignment a, Set<Expression> d) {
        Set<Expression> newSet = new HashSet<>();
        if (d != null)
            newSet.addAll(d);
        if (!(a.getLhs() instanceof Variable)) {
            // Add left side
            newSet.add(a.getLhs());
        } else {
            Variable v = (Variable) a.getLhs();
            // Remove all expressions where the left side occurs
            newSet.removeIf(e -> {
                // Do not add unknown expressions
                if (e instanceof UnknownExpression) {
                    return true;
                }
                Optional<Set<Variable>> varOpt = e.accept(new UsedVariablesDFS(), new NoVal());
                if (varOpt.isPresent()) {
                    Set<Variable> vars = varOpt.get();
                    return vars.contains(v);
                }
                System.err.println("Failed to evaluate variables for " + e);
                return false;
            });
        }
        if (!(a.getRhs() instanceof UnknownExpression))
            newSet.add(a.getRhs());

        Set<Expression> old = dataflowOf(a);
        if (!lessoreq(newSet, old)) {
            newSet = lub(newSet, old);
            dataflowOf(a, newSet);
            return newSet;
        }
        return null;
    }

    @Override
    public Set<Expression> visit(State s, Set<Expression> d) {
        if (d != null) {
            dataflowOf(s, lub(d, dataflowOf(s)));
        } else if (dataflowOf(s) == null) {
            dataflowOf(s, new HashSet<>());
        }
        return d;
    }

    @Override
    public Set<Expression> visit(GuardedTransition a, Set<Expression> d) {
        Set<Expression> newSet = new HashSet<>();
        if (d != null)
            newSet.addAll(d);
        newSet.add(a.getAssertion());

        Set<Expression> old = dataflowOf(a);
        if (!lessoreq(newSet, old)) {
            newSet = lub(newSet, old);
            dataflowOf(a, newSet);
            return newSet;
        }
        return null;
    }
}
