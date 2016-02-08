package petter.cfg.expression;

import petter.cfg.expression.visitors.PropagatingDFS;
import petter.cfg.expression.visitors.ExpressionVisitor;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import petter.cfg.Annotatable;
import petter.cfg.expression.types.Type;
import petter.cfg.expression.visitors.Substitution;
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
     * @see Expression#accept(petter.cfg.expression.PropagatingDFS, java.lang.Object) 
     */
    @Deprecated
    default void accept(ExpressionVisitor v) {throw new UnsupportedOperationException("naive accept is deprecated; use the propagating one instead"); }
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
     * substitute this variable with the following expression;
     * deprecated and removed in a few releases, however 
     * @see Substitution#subst(petter.cfg.expression.Expression, java.util.Map) 
     */
    @Deprecated
    default void substitute(Variable v, Expression ex) { throw new UnsupportedOperationException("Side-effect prone substitution is not decent; use the lazy copying variant!");}
    
    /**
     * Shiny new substitution method, creates a deep copy of the expression
     * @see Substitution
    */
    default Expression subst(Variable v,Expression ex){
        Map<Variable,Expression> map = new HashMap<>();
        map.put(v, ex);
        return Substitution.subst(this,map);
    }
  
    /**
     * @return the composite type of the expression
     */
    Type getType();
}
