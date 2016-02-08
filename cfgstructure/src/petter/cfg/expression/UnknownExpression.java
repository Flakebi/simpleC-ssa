package petter.cfg.expression;
import petter.cfg.expression.visitors.PropagatingDFS;
import petter.cfg.expression.visitors.ExpressionVisitor;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;
import petter.cfg.expression.types.Type;
/**
 * represents an UnknownExpression 
 * @author Michael Petter
 * @author Andrea Flexeder
 */
public class UnknownExpression implements Expression, java.io.Serializable{

    private final Type type;
    public Type getType() { return type; }
    public UnknownExpression(Type t){
        this.type=t;
    }
    /**
     * Arbitrary annotations identified by key.
     */
    private Map<Object, Object> annotations;
    public Object getAnnotation(Object key) {
        if (annotations == null) return null;
        return annotations.get(key);
    }
    public Object putAnnotation(Object key, Object value) {
        if (annotations == null)
            annotations = new HashMap<Object, Object>();
        return annotations.put(key, value);
    }
    public <T> T getAnnotation(Class<T> key) throws ClassCastException {
        if (annotations == null) return null;
        return key.cast(annotations.get(key));
    }
    public <T> T putAnnotation(Class<T> key, T value) throws ClassCastException {
        if (annotations == null)
            annotations = new HashMap<Object, Object>();
        return key.cast(annotations.put(key, value));
    }
    public Map<Object, Object> getAnnotations() {
        return annotations;
    }
    public void putAnnotations(Map<?, ?> a) {
        annotations.putAll(a);
    }
    /**
     * the UnknownExpression
     * @return "?"
     */
    public String toString(){
	return "?";
    }
    /**
     * an UnknownExpression cannot contain a multiplication
     * @return false
     */
    public boolean hasMultiplication(){
	return false;
    }
    /**
     * an UnknownExpression cannot contain a division
     * @return false
     */
    public boolean hasDivision(){
	return false;
    }
    /**
     * an UnknownExpression is not invertible
     * @return false
     */
    public boolean isInvertible(Variable var){
	return false;
    }
    /**
     * an UnknownExpression is not linear
     * @return false
     */
    public boolean isLinear(){
	return false;
    }
    /**
     * an UnknownExpression cannot contain a methodcall
     * @return false
     */
    public boolean hasMethodCall(){
	return false;
    }
    /**
     * @return guess what?
     */
    public boolean hasUnknown(){
	return true;
    }
    /**
     * analysis of an expression
     * @param v the analysing ExpressionVisitor
     */
    public void accept(ExpressionVisitor v){
	v.preVisit(this);
	v.postVisit(this);
    }
    public <up,down> Optional<up> accept(PropagatingDFS<up,down> pv,down fromParent){
        return pv.preVisit(this,fromParent).map(curr->pv.postVisit(this,curr));
    }
    /**
     * an UnknownExpression has no degree
     * @return -1
     */
    public int getDegree(){
	return -1;
    }

}

