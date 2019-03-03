package analysis.redundancyelimination;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;
import petter.cfg.expression.BinaryExpression;
import petter.cfg.expression.FunctionCall;
import petter.cfg.expression.IntegerConstant;
import petter.cfg.expression.StringLiteral;
import petter.cfg.expression.UnaryExpression;
import petter.cfg.expression.UnknownExpression;
import petter.cfg.expression.Variable;
import petter.cfg.expression.visitors.NoVal;
import petter.cfg.expression.visitors.SyntheticComputationDFS;

public class UsedVariablesDFS implements SyntheticComputationDFS<Set<Variable>> {

    @Override
    public Set<Variable> postVisit(IntegerConstant s) {
        return new HashSet<>();
    }

    @Override
    public Set<Variable> postVisit(StringLiteral s) {
        return new HashSet<>();
    }

    @Override
    public Set<Variable> postVisit(UnknownExpression s) {
        return new HashSet<>();
    }

    @Override
    public Set<Variable> postVisit(FunctionCall m, Stream<Set<Variable>> it) {
        return it.reduce(new HashSet<>(), (a, b) -> {
            HashSet<Variable> res = new HashSet<>();
            res.addAll(a);
            res.addAll(b);
            return res;
        });
    }

    @Override
    public Set<Variable> postVisit(Variable s, NoVal fromTop) {
        HashSet<Variable> res = new HashSet<>();
        res.add(s);
        return res;
    }

    @Override
    public Set<Variable> postVisit(UnaryExpression s, Set<Variable> fromChild) {
        return fromChild;
    }

    @Override
    public Set<Variable> postVisit(BinaryExpression s, Set<Variable> lhs, Set<Variable> rhs) {
        HashSet<Variable> res = new HashSet<>();
        res.addAll(lhs);
        res.addAll(rhs);
        return res;
    }

}
