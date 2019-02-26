package analysis.constfolding;

import petter.cfg.expression.*;
import petter.cfg.expression.types.Int;
import petter.cfg.expression.visitors.PropagatingDFS;
import petter.cfg.expression.Expression;
import petter.utils.Tupel;

import java.util.Optional;
import java.util.stream.Stream;

class PartialExpressionEvaluator implements PropagatingDFS<Tupel<Expression, Value>, Values> {

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
    public Tupel<Expression, Value> postVisit(IntegerConstant s, Values fromTop) {
        if (s.getType() instanceof  Int) {
            return Tupel.create(s, Value.makeValue(s.getIntegerConst()));
        } else {
            return Tupel.create(s, Value.top);
        }
    }

    @Override
    public Tupel<Expression, Value> postVisit(StringLiteral s, Values fromTop) {
        return Tupel.create(s, Value.top);
    }

    @Override
    public Tupel<Expression, Value> postVisit(Variable s, Values fromTop) {
        assert(!fromTop.isBottom());
        final Value value = fromTop.getValue(s);
        if (s.getType() instanceof Int && !value.isTop()) {
            return Tupel.create(new IntegerConstant(value.getValue()), value);
        } else {
            return Tupel.create(s, Value.top);
        }
    }

    @Override
    public Tupel<Expression, Value> postVisit(FunctionCall m, Values s, Stream<Tupel<Expression, Value>> it) {
        throw new UnsupportedOperationException("No procedure calls allowed, intraprocedural analysis only");
    }

    @Override
    public Tupel<Expression, Value> postVisit(UnknownExpression s, Values fromParent) {
        return Tupel.create(s, Value.top);
    }

    @Override
    public Tupel<Expression, Value> postVisit(UnaryExpression s, Tupel<Expression, Value> fromChild) {
        final Value valueFromChild = fromChild.b;

        if (valueFromChild.isTop() || !(s.getType() instanceof Int)) {
            return Tupel.create(fromChild.a, Value.top);
        } else {
            final int op = s.getOperator().getCode();
            final int v = valueFromChild.getValue();

            int r;
            if (op == Operator.PLUS) {
                r = v;
            } else if (op == Operator.MINUS) {
                r = -v;
            } else {
                return Tupel.create(fromChild.a, Value.top);
            }

            return Tupel.create(new IntegerConstant(r), Value.makeValue(r));
        }
    }

    @Override
    public Tupel<Expression, Value> postVisit(BinaryExpression s, Tupel<Expression, Value> lhs, Tupel<Expression, Value> rhs) {
        final Value lhsValue = lhs.b;
        final Value rhsValue = rhs.b;

        if (!(s.getType() instanceof Int)) {
            return Tupel.create(s, Value.top);
        }

        if (lhsValue.isTop() && rhsValue.isTop()) {
            return Tupel.create(new BinaryExpression(lhs.a, s.getOperator(), rhs.a), Value.top);
        }

        if (!lhsValue.isTop() && rhsValue.isTop()) {
            final int lv = lhsValue.getValue();
            return Tupel.create(new BinaryExpression(new IntegerConstant(lv), s.getOperator(), rhs.a), Value.top);
        }

        if (lhsValue.isTop() && !rhsValue.isTop()) {
            final int rv = rhsValue.getValue();
            return Tupel.create(new BinaryExpression(lhs.a, s.getOperator(), new IntegerConstant(rv)), Value.top);
        }

        final int op = s.getOperator().getCode();
        final int lv = lhsValue.getValue();
        final int rv = rhsValue.getValue();

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
            return Tupel.create(new BinaryExpression(lhs.a, s.getOperator(), rhs.a), Value.top);
        }

        return Tupel.create(new IntegerConstant(r), Value.makeValue(r));
    }

    static final PartialExpressionEvaluator visitor = new PartialExpressionEvaluator();
}
