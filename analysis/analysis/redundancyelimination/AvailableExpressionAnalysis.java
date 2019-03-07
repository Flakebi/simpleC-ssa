package analysis.redundancyelimination;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import petter.cfg.AbstractPropagatingVisitor;
import petter.cfg.State;
import petter.cfg.edges.Assignment;
import petter.cfg.edges.GuardedTransition;
import petter.cfg.edges.ProcedureCall;
import petter.cfg.edges.Psi;
import petter.cfg.edges.Transition;
import petter.cfg.expression.Expression;
import petter.cfg.expression.IntegerConstant;
import petter.cfg.expression.StringLiteral;
import petter.cfg.expression.UnknownExpression;
import petter.cfg.expression.Variable;
import petter.cfg.expression.visitors.NoVal;

public class AvailableExpressionAnalysis extends AbstractPropagatingVisitor<Set<Expression>> {
    // lower upper bound
    static Set<Expression> lub(Set<Expression> s1, Set<Expression> s2) {
        if (s1 == null) return s2;
        if (s2 == null) return s1;
        HashSet<Expression> res = new HashSet<>(s1);
        // Intersection
        res.retainAll(s2);
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

    public static void applyTransition(Expression lhs, Expression rhs, Set<Expression> s) {
        // Do not add simple expressions
        if (!(rhs instanceof UnknownExpression) && !(rhs instanceof Variable)
            && !(rhs instanceof IntegerConstant) && !(rhs instanceof StringLiteral))
            s.add(rhs);

        if (!(lhs instanceof Variable)) {
            // Add left side
            s.add(lhs);
        } else {
            Variable v = (Variable) lhs;
            // Remove all expressions where the left side occurs
            s.removeIf(e -> {
                Optional<Set<Variable>> varOpt = e.accept(new UsedVariablesDFS(), new NoVal());
                if (varOpt.isPresent()) {
                    Set<Variable> vars = varOpt.get();
                    return vars.contains(v);
                }
                return false;
            });
        }
        // If there is a dereference on the left, remove all expressions with dereferences on the right
        Optional<Boolean> deref = lhs.accept(new ContainsDereferencesDFS(), new NoVal());
        if (deref.isPresent() && deref.get()) {
            s.removeIf(e -> {
                Optional<Boolean> derefOpt = e.accept(new ContainsDereferencesDFS(), new NoVal());
                return derefOpt.isPresent() && derefOpt.get();
            });
        }
    }

    public static void applyTransition(GuardedTransition a, Set<Expression> s) {
        s.add(a.getAssertion());
    }

    public static void applyTransition(Transition a, Set<Expression> s) {
        if (a instanceof Assignment) {
            var as = (Assignment) a;
            applyTransition(as.getLhs(), as.getRhs(), s);
        } else if (a instanceof Psi) {
            var as = (Psi) a;
            for (int i = 0; i < as.getLhs().size(); i++) {
                applyTransition(as.getLhs().get(i), as.getRhs().get(i), s);
            }
        } else if (a instanceof GuardedTransition)
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
            dataflowOf(a, newSet);
            return newSet;
        }
        return null;
    }

    @Override
    public Set<Expression> visit(Psi a, Set<Expression> d) {
        Set<Expression> newSet = new HashSet<>();
        if (d != null)
            newSet.addAll(d);
        applyTransition(a, newSet);

        Set<Expression> old = dataflowOf(a);
        if (!lessoreq(newSet, old)) {
            dataflowOf(a, newSet);
            return newSet;
        }
        return null;
    }

    @Override
    public Set<Expression> visit(GuardedTransition a, Set<Expression> d) {
        Set<Expression> newSet = new HashSet<>();
        if (d != null)
            newSet.addAll(d);
        applyTransition(a, newSet);

        Set<Expression> old = dataflowOf(a);
        if (!lessoreq(newSet, old)) {
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
}
