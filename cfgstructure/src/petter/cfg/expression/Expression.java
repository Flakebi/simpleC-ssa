package petter.cfg.expression;

import java.util.Optional;
import petter.cfg.Annotatable;
import petter.cfg.expression.types.Type;
/**
 * provides an interface to constructing an expression
 * @author Michael Petter
 * @author Andrea Flexeder
 */
public interface Expression extends Annotatable {
    /**
     * check if there exists a subexpression, that accesses an array
     */
    default boolean hasArrayAccess() { return false; }
    /**
     * check if an expression contains a multiplication
     */
    boolean hasMultiplication();
   /**
     * check if an expression contains a division
     */
    boolean hasDivision();
    /**
     * check if a variable is invertible
     */
    boolean isInvertible(Variable var);
   /**
     * check if an expression is linear
     */
    boolean isLinear();
   /**
     * check if an expression contains a method call
     */
    boolean hasMethodCall();
   /**
     * check if an expression has an unknown expression
     */
    boolean hasUnknown();
    /**
     * analysis of an expression
     * @param v the analysing ExpressionVisitor
     */
    void accept(ExpressionVisitor v);
    /**
     * analysis of an expression
     * @param v the analyzing ExpressionVisitor
     * @param S value propagated from parent
     */
    <up, down> Optional<up> accept(PropagatingDFS<up, down> v, down parentValue);

    /**
     * get the degree of an expression
     */
    int getDegree();
    /**
     * substitute this variable with the following expression
     */
    void substitute(Variable v, Expression ex);
  
    /**
     * @return the composite type of the expression
     */
    Type getType();
}
