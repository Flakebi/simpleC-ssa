package analysis.constfolding;

import petter.cfg.edges.*;
import petter.cfg.expression.Expression;
import petter.cfg.expression.Operator;
import petter.cfg.expression.Variable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class EdgeEffects {

    static Values evalAssignment(Assignment assignment, Values d) {
        final Expression lhs = assignment.getLhs();
        if (lhs instanceof Variable) {
            final Value value = assignment.getRhs().accept(PartialExpressionEvaluator.visitor, d).get().b;
            return d.with((Variable) assignment.getLhs(), value);
        } else {
            return d;
        }
    }

    static Values evalNop(Nop nop, Values d) {
        return d;
    }

    static Values evalGuarded(GuardedTransition guarded, Values d) {
        final Expression expression = guarded.getAssertion();
        final Operator op = guarded.getOperator();
        return ConditionEvaluator.isConditionDefinitellyFalse(expression, op, d) ? Values.bottom : d;
    }

    static Values evalProcedureCall(ProcedureCall call, Values d) {
        throw new UnsupportedOperationException("No procedure calls allowed, intraprocedural analysis only");
    }

    static Values evalPsi(Psi psi, Values d) {
        final var effects = new HashMap<Variable, Value>();

        final var lhs = psi.getLhs();
        final var rhs = psi.getRhs();
        assert (lhs.size() == rhs.size());

        for (var r : rhs) {
            effects.put((Variable) r, Value.top);
        }

        for (var i = 0; i < lhs.size(); ++i) {
            var l = (Variable) lhs.get(i);
            var r = (Variable) rhs.get(i);

            effects.put(l, d.getValue(r));
        }

        return d.with(effects);
    }

    static Values evalTransition(Transition transition, Values d) {
        if (transition instanceof Assignment) {
            return evalAssignment((Assignment) transition, d);
        } else if (transition instanceof Nop) {
            return evalNop((Nop) transition, d);
        } else if (transition instanceof GuardedTransition) {
            return evalGuarded((GuardedTransition) transition, d);
        } else if (transition instanceof  ProcedureCall) {
            return evalProcedureCall((ProcedureCall) transition, d);
        } else if (transition instanceof Psi) {
            return evalPsi((Psi) transition, d);
        }

        throw new RuntimeException("Missing case for some transition");
    }
}
