package analysis.constfolding;

import petter.cfg.expression.Expression;
import petter.cfg.expression.Operator;

class ConditionEvaluator {
    static boolean isConditionDefinitellyFalse(Expression e, Operator op_, Values d) {
        final int op = op_.getCode();

        final Value v = e.accept(PartialExpressionEvaluator.visitor, d).get().b;
        if (v.isTop()) {
            return false;
        }

        final int vv = v.getValue();
        if (op == Operator.EQ) {
            return vv != 0;
        } else if (op == Operator.NEQ) {
            return vv == 0;
        } else if (op == Operator.LE) {
            return vv >= 0;
        } else if (op == Operator.LEQ) {
            return vv > 0;
        } else if (op == Operator.GT) {
            return vv <= 0;
        } else if (op == Operator.GTQ) {
            return vv < 0;
        } else {
            return false;
        }
    }

    static boolean isConditionDefinitellyTrue(Expression e, Operator op, Values d) {
        return isConditionDefinitellyFalse(e, op.invert(), d);
    }
}
