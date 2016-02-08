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

/**
 * provides a basic interface for all simple visitors of an expression
 * this interface has to be implemented to visit an expression 
 * @see AbstractExpressionVisitor
 * @author Michael Petter
 * @author Daniel Stuewe
 */
public interface PropagatingDFS<up,down>{
    /**
     * specific visit method.
     * Override this method to provide custom actions when traversing an {@link IntegerConstant}
     * @param s the IntegerConstant which is visited
     * @return <code>true</true> when you want to continue, <code>false</code> otherwise
     */
    public Optional<down> preVisit(IntegerConstant s,down fromParent);
    /**
     * specific visit method.
     * Override this method to provide custom actions when traversing an {@link StringLiteral}
     * @param s the StringLiteral which is visited
     * @return <code>true</true> when you want to continue, <code>false</code> otherwise
     */
    public Optional<down> preVisit(StringLiteral s,down fromParent);
    /**
     * specific previsit method.
     * Override this method to provide custom actions when traversing a {@link Variable}
     * @param s the Variable which is visited
     * @return <code>true</true> when you want to continue, <code>false</code> otherwise
     */
    public Optional<down> preVisit(Variable s,down fromParent);
    /**
     * specific previsit method.
     * Override this method to provide custom actions when traversing a {@link FunctionCall }
     * @param s the MethodCall which is visited
     * @return <code>true</true> when you want to continue, <code>false</code> otherwise
     */
    public Optional<down> preVisit(FunctionCall s,down fromParent);
    /**
     * specific previsit method.
     * Override this method to provide custom actions when traversing an {@link UnknownExpression }
     * @param s the UnknownExpression which is visited
     * @return <code>true</true> when you want to continue, <code>false</code> otherwise
     */
    public Optional<down> preVisit(UnknownExpression s,down fromParent);
    /**
     * specific previsit method.
     * Override this method to provide custom actions when traversing an {@link UnaryExpression }
     * @param s the UnaryExpression which is visited
     * @return <code>true</true> when you want to continue, <code>false</code> otherwise
     */
    public Optional<down> preVisit(UnaryExpression s,down fromParent);
    /**
     * specific previsit method.
     * Override this method to provide custom actions when traversing a {@link BinaryExpression }
     * @param s the BinaryExpression which is visited
     * @return <code>true</true> when you want to continue, <code>false</code> otherwise
     */
    public Optional<down> preVisit(BinaryExpression s, down fromParent);

    /**
     * specific postvisit method.
     * Override this method to provide custom actions when traversing an {@link IntegerConstant}
     * @param s the IntegerConstant which is visited
     */
    public up postVisit(IntegerConstant s,down fromTop);
    /**
     * specific postvisit method.
     * Override this method to provide custom actions when traversing an {@link StringLiteral}
     * @param s the IntegerConstant which is visited
     */
    public up postVisit(StringLiteral s,down fromTop);
    /**
     * specific postvisit method.
     * Override this method to provide custom actions when traversing a {@link Variable}
     * @param s the Variable which is visited
     */
    public up postVisit(Variable s,down fromTop);
    /**
     * specific postvisit method.
     * Override this method to provide custom actions when traversing a {@link FunctionCall }
     * @param s the MethodCall which is visited
     */
    public up postVisit(FunctionCall m,down s,Stream<up> it);
    /**
     * specific postvisit method.
     * Override this method to provide custom actions when traversing an {@link UnknownExpression }
     * @param s the UnknownExpression which is visited
     */
    public up postVisit(UnknownExpression s,down fromParent);
    /**
     * specific postisit method.
     * Override this method to provide custom actions when traversing an {@link UnaryExpression }
     * @param s the UnaryExpression which is visited
     */
    public up postVisit(UnaryExpression s,up fromChild);
    /**
     * specific postvisit method.
     * Override this method to provide custom actions when traversing a {@link BinaryExpression }
     * @param s the BinaryExpression which is visited
     */
    public up postVisit(BinaryExpression s,up lhs, up rhs);
}
