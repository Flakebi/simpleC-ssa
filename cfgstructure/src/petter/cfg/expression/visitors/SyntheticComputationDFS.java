package petter.cfg.expression.visitors;

import java.util.Optional;
import java.util.stream.Stream;
import petter.cfg.expression.BinaryExpression;
import petter.cfg.expression.FunctionCall;
import petter.cfg.expression.IntegerConstant;
import petter.cfg.expression.StringLiteral;
import petter.cfg.expression.UnaryExpression;
import petter.cfg.expression.UnknownExpression;
import petter.cfg.expression.Variable;
import petter.cfg.expression.visitors.NoVal;

/**
 * simplified interface for synthetically computed attributes, propagating from from leaves to root
 * @see PropagatingDFS
 * @author Michael Petter
 */
public interface SyntheticComputationDFS<up> extends PropagatingDFS<up, NoVal>{

    @Override public default Optional<NoVal> preVisit(BinaryExpression s, NoVal fromParent) { return Optional.empty(); }
    @Override public default Optional<NoVal> preVisit(FunctionCall s, NoVal fromParent) { return Optional.empty();   }
    @Override public default Optional<NoVal> preVisit(IntegerConstant s, NoVal fromParent) { return Optional.empty();    }
    @Override public default Optional<NoVal> preVisit(StringLiteral s, NoVal fromParent) {    return Optional.empty();    }
    @Override public default Optional<NoVal> preVisit(UnaryExpression s, NoVal fromParent) { return Optional.empty(); }
    @Override public default Optional<NoVal> preVisit(UnknownExpression s, NoVal fromParent) {  return Optional.empty();    }
    @Override public default Optional<NoVal> preVisit(Variable s, NoVal fromParent) { return Optional.empty();    }
    @Override public default up postVisit(IntegerConstant s, NoVal fromTop) {        return postVisit(s);    }
    @Override public default up postVisit(StringLiteral s, NoVal fromTop) {        return postVisit(s);    }
    @Override public default up postVisit(UnknownExpression s, NoVal fromParent) {        return postVisit(s);    }
    @Override public default up postVisit(FunctionCall m, NoVal s, Stream<up> it) {        return postVisit(m,it);    }

    public up postVisit(IntegerConstant s);
    public up postVisit(StringLiteral s);
    public up postVisit(UnknownExpression s);
    public up postVisit(FunctionCall m, Stream<up> it);
    
}
