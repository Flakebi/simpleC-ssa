package analysis.redundancyelimination;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import petter.cfg.AbstractPropagatingVisitor;
import petter.cfg.State;
import petter.cfg.edges.Assignment;
import petter.cfg.edges.GuardedTransition;
import petter.cfg.edges.ProcedureCall;
import petter.cfg.edges.Transition;
import petter.cfg.expression.Expression;
import petter.cfg.expression.UnknownExpression;
import petter.cfg.expression.Variable;
import petter.cfg.expression.visitors.NoVal;

public class AvailableExpressionAnalysis extends AbstractPropagatingVisitor<Set<Expression>> {
    // lower upper bound
    static Set<Expression> lub(Set<Expression> s1, Set<Expression> s2) {
        if (s1 == null) return s2;
        if (s2 == null) return s1;
        HashSet<Expression> res = new HashSet<>();
        // Intersection
        for (Expression e : s1) {
            if (s2.contains(e))
                res.add(e);
        }
        return res;
    }

    static boolean lessoreq(Set<Expression> s1, Set<Expression> s2) {
        if (s1 == null) return true;
        if (s2 == null) return false;
        return s1.containsAll(s2);
    }

    public AvailableExpressionAnalysis() {
        super(true);
    }

    @Override
    public Set<Expression> visit(ProcedureCall ae, Set<Expression> d) {
        // Arguments are only variables, so do nothing
        return d;
    }

    public static void applyTransition(Assignment a, Set<Expression> s) {
        s.add(a.getRhs());
        if (!(a.getLhs() instanceof Variable)) {
            // Add left side
            s.add(a.getLhs());
        } else {
            Variable v = (Variable) a.getLhs();
            // Remove all expressions where the left side occurs
            s.removeIf(e -> {
                // Do not add unknown expressions
                if (e instanceof UnknownExpression) {
                    return true;
                }
                Optional<Set<Variable>> varOpt = e.accept(new UsedVariablesDFS(), new NoVal());
                if (varOpt.isPresent()) {
                    Set<Variable> vars = varOpt.get();
                    return vars.contains(v);
                }
                return false;
            });
        }
        // TODO: If there is a dereference on the left, remove all expressions with dereferences on the right
    }

    public static void applyTransition(GuardedTransition a, Set<Expression> s) {
        s.add(a.getAssertion());
    }

    public static void applyTransition(Transition a, Set<Expression> s) {
        if (a instanceof Assignment)
            applyTransition((Assignment)a, s);
        else if (a instanceof GuardedTransition)
            applyTransition((GuardedTransition)a, s);
    }

    @Override
    public Set<Expression> visit(Assignment a, Set<Expression> d) {
        Set<Expression> newSet = new HashSet<>();
        if (d != null)
            newSet.addAll(d);
        applyTransition(a, newSet);

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
            dataflowOf(s, d);
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
        applyTransition(a, newSet);

        Set<Expression> old = dataflowOf(a);
        if (!lessoreq(newSet, old)) {
            newSet = lub(newSet, old);
            dataflowOf(a, newSet);
            return newSet;
        }
        return null;
    }
}
