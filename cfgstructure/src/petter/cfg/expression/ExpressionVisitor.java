package petter.cfg.expression;
/**
 * provides a basic interface for all simple visitors of an expression
 * this interface has to be implemented to visit an expression 
 * @see AbstractExpressionVisitor
 * @author Michael Petter
 * @author Andrea Flexeder
 */
public interface ExpressionVisitor{
    /**
     * specific previsit method.
     * Override this method to provide custom actions when traversing an {@link IntegerConstant}
     * @param s the IntegerConstant which is visited
     * @return <code>true</true> when you want to continue, <code>false</code> otherwise
     */
    public boolean preVisit(IntegerConstant s);
    /**
     * specific previsit method.
     * Override this method to provide custom actions when traversing an {@link StringLiteral}
     * @param s the StringLiteral which is visited
     * @return <code>true</true> when you want to continue, <code>false</code> otherwise
     */
    public boolean preVisit(StringLiteral s);
    /**
     * specific previsit method.
     * Override this method to provide custom actions when traversing a {@link Variable}
     * @param s the Variable which is visited
     * @return <code>true</true> when you want to continue, <code>false</code> otherwise
     */
    public boolean preVisit(Variable s);
    /**
     * specific previsit method.
     * Override this method to provide custom actions when traversing a {@link MethodCall }
     * @param s the MethodCall which is visited
     * @return <code>true</true> when you want to continue, <code>false</code> otherwise
     */
    public boolean preVisit(MethodCall s);
    /**
     * specific previsit method.
     * Override this method to provide custom actions when traversing an {@link UnknownExpression }
     * @param s the UnknownExpression which is visited
     * @return <code>true</true> when you want to continue, <code>false</code> otherwise
     */
    public boolean preVisit(UnknownExpression s);
    /**
     * specific previsit method.
     * Override this method to provide custom actions when traversing an {@link UnaryExpression }
     * @param s the UnaryExpression which is visited
     * @return <code>true</true> when you want to continue, <code>false</code> otherwise
     */
    public boolean preVisit(UnaryExpression s);
    /**
     * specific previsit method.
     * Override this method to provide custom actions when traversing a {@link BinaryExpression }
     * @param s the BinaryExpression which is visited
     * @return <code>true</true> when you want to continue, <code>false</code> otherwise
     */
    public boolean preVisit(BinaryExpression s);

    /**
     * specific postvisit method.
     * Override this method to provide custom actions when traversing an {@link IntegerConstant}
     * @param s the IntegerConstant which is visited
     */
    public void postVisit(IntegerConstant s);
    /**
     * specific postvisit method.
     * Override this method to provide custom actions when traversing an {@link StringLiteral}
     * @param s the IntegerConstant which is visited
     */
    public void postVisit(StringLiteral s);
    /**
     * specific postvisit method.
     * Override this method to provide custom actions when traversing a {@link Variable}
     * @param s the Variable which is visited
     */
    public void postVisit(Variable s);
    /**
     * specific postvisit method.
     * Override this method to provide custom actions when traversing a {@link MethodCall }
     * @param s the MethodCall which is visited
     */
    public void postVisit(MethodCall s);
    /**
     * specific postvisit method.
     * Override this method to provide custom actions when traversing an {@link UnknownExpression }
     * @param s the UnknownExpression which is visited
     */
    public void postVisit(UnknownExpression s);
    /**
     * specific postisit method.
     * Override this method to provide custom actions when traversing an {@link UnaryExpression }
     * @param s the UnaryExpression which is visited
     */
    public void postVisit(UnaryExpression s);
    /**
     * specific postvisit method.
     * Override this method to provide custom actions when traversing a {@link BinaryExpression }
     * @param s the BinaryExpression which is visited
     */
    public void postVisit(BinaryExpression s);
}
