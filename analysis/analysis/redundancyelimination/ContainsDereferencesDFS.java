package analysis.redundancyelimination;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;
import petter.cfg.expression.BinaryExpression;
import petter.cfg.expression.FunctionCall;
import petter.cfg.expression.IntegerConstant;
import petter.cfg.expression.Operator;
import petter.cfg.expression.StringLiteral;
import petter.cfg.expression.UnaryExpression;
import petter.cfg.expression.UnknownExpression;
import petter.cfg.expression.Variable;
import petter.cfg.expression.visitors.NoVal;
import petter.cfg.expression.visitors.SyntheticComputationDFS;

public class ContainsDereferencesDFS implements SyntheticComputationDFS<Boolean> {

    @Override
    public Boolean postVisit(IntegerConstant s) {
        return false;
    }

    @Override
    public Boolean postVisit(StringLiteral s) {
        return false;
    }

    @Override
    public Boolean postVisit(UnknownExpression s) {
        return false;
    }

    @Override
    public Boolean postVisit(FunctionCall m, Stream<Boolean> it) {
        return it.reduce(false, (a, b) -> {
            return a || b;
        });
    }

    @Override
    public Boolean postVisit(Variable s, NoVal fromTop) {
        return false;
    }

    @Override
    public Boolean postVisit(UnaryExpression s, Boolean fromChild) {
        return fromChild || s.getOperator().is(Operator.DEREF);
    }

    @Override
    public Boolean postVisit(BinaryExpression s, Boolean lhs, Boolean rhs) {
        return lhs || rhs;
    }

}
