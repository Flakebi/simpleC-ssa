package petter.constfolding;

import petter.cfg.edges.*;
import petter.cfg.expression.Expression;
import petter.cfg.expression.Operator;
import petter.cfg.expression.Variable;

import java.util.Set;

public class EdgeEffects {

    static Values evalAssignment(Set<Integer> gv, Assignment assignment, Values d) {
        final Expression lhs = assignment.getLhs();
        if (lhs instanceof Variable && !(gv.contains(((Variable) lhs).getId()))) {
            final Value value = assignment.getRhs().accept(ExpressionEvaluator.visitor, d).get();
            return d.with((Variable) assignment.getLhs(), value);
        } else {
            return d;
        }
    }

    static Values evalNop(Set<Integer> gv, Nop nop, Values d) {
        return d;
    }

    static Values evalGuarded(Set<Integer> gv, GuardedTransition guarded, Values d) {
        final Expression expression = guarded.getAssertion();
        final Operator op = guarded.getOperator();
        return ConditionEvaluator.isConditionDefinitellyFalse(expression, op, d) ? Values.bottom : d;
    }

    static Values evalProcedureCall(Set<Integer> gv, ProcedureCall call, Values d) {
        return d;
    }

    static Values evalTransition(Set<Integer> gv, Transition transition, Values d) {
        if (transition instanceof Assignment) {
            return evalAssignment(gv, (Assignment) transition, d);
        } else if (transition instanceof Nop) {
            return evalNop(gv, (Nop) transition, d);
        } else if (transition instanceof GuardedTransition) {
            return evalGuarded(gv, (GuardedTransition) transition, d);
        } else if (transition instanceof  ProcedureCall) {
            return evalProcedureCall(gv, (ProcedureCall) transition, d);
        }

        throw new RuntimeException("Missing case for some transition");
    }
}
