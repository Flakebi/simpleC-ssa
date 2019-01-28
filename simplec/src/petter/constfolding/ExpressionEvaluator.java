package petter.constfolding;

import petter.cfg.expression.*;
import petter.cfg.expression.types.Int;
import petter.cfg.expression.visitors.PropagatingDFS;

import java.util.Optional;
import java.util.stream.Stream;

class ExpressionEvaluator implements PropagatingDFS<Value, Values> {

    @Override
    public Optional<Values> preVisit(IntegerConstant s, Values fromParent) {
        return Optional.of(fromParent);
    }

    @Override
    public Optional<Values> preVisit(StringLiteral s, Values fromParent) {
        return Optional.of(fromParent);
    }

    @Override
    public Optional<Values> preVisit(Variable s, Values fromParent) {
        return Optional.of(fromParent);
    }

    @Override
    public Optional<Values> preVisit(FunctionCall s, Values fromParent) {
        return Optional.of(fromParent);
    }

    @Override
    public Optional<Values> preVisit(UnknownExpression s, Values fromParent) {
        return Optional.of(fromParent);
    }

    @Override
    public Optional<Values> preVisit(UnaryExpression s, Values fromParent) {
        return Optional.of(fromParent);
    }

    @Override
    public Optional<Values> preVisit(BinaryExpression s, Values fromParent) {
        return Optional.of(fromParent);
    }

    @Override
    public Value postVisit(IntegerConstant s, Values fromTop) {
        if (s.getType() instanceof Int) {
            return Value.makeValue(s.getIntegerConst());
        } else {
            return Value.top;
        }
    }

    @Override
    public Value postVisit(StringLiteral s, Values fromTop) {
        return Value.top;
    }

    @Override
    public Value postVisit(Variable s, Values fromTop) {
        assert(!fromTop.isBottom());
        if (s.getType() instanceof Int) {
            return fromTop.getValue(s);
        } else {
            return Value.top;
        }
    }

    @Override
    public Value postVisit(FunctionCall m, Values s, Stream<Value> it) {
        return Value.top;
    }

    @Override
    public Value postVisit(UnknownExpression s, Values fromParent) {
        return Value.top;
    }

    @Override
    public Value postVisit(UnaryExpression s, Value fromChild) {
        if (fromChild.isTop() || !(s.getType() instanceof Int)) {
            return Value.top;
        } else {
            final int op = s.getOperator().getCode();
            final int v = fromChild.getValue();

            int r;
            if (op == Operator.PLUS) {
                r = v;
            } else if (op == Operator.MINUS) {
                r = -v;
            } else {
                return Value.top;
            }

            return Value.makeValue(r);
        }
    }

    @Override
    public Value postVisit(BinaryExpression s, Value lhs, Value rhs) {
        if (lhs.isTop() || rhs.isTop() || !(s.getType() instanceof Int)) {
            return Value.top;
        } else {
            final int op = s.getOperator().getCode();
            final int lv = lhs.getValue();
            final int rv = rhs.getValue();

            int r;
            if (op == Operator.PLUS) {
                r = lv + rv;
            } else if (op == Operator.MINUS) {
                r = lv - rv;
            } else if (op == Operator.MUL) {
                r = lv * rv;
            } else if (op == Operator.DIV && rv != 0) {
                r = lv / rv;
            } else {
                return Value.top;
            }

            return Value.makeValue(r);
        }
    }

    static final ExpressionEvaluator visitor = new ExpressionEvaluator();
}
