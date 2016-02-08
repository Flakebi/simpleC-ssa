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
 * Simplified interface for Visitors that only propagate data from the root to the leaves
 * this interface has to be implemented to visit an expression 
 * @see PropagatingDFS
 * @author Michael Petter
 */
public interface InheritedDFS<down> extends PropagatingDFS<NoVal, down>{
    @Override default public NoVal postVisit(IntegerConstant s,down fromTop) { return null; }
    @Override default public NoVal postVisit(StringLiteral s,down fromTop) { return null; }
    @Override default public NoVal postVisit(Variable s,down fromTop) {return null; }
    @Override default public NoVal postVisit(FunctionCall m,down s,Stream<NoVal> it) {return null;}
    @Override default public NoVal postVisit(UnknownExpression s,down fromParent) {return null; }
    @Override default public NoVal postVisit(UnaryExpression s,NoVal fromChild) {return null; }
    @Override default public NoVal postVisit(BinaryExpression s,NoVal lhs, NoVal rhs) {return null; }
}
