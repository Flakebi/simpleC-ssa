package petter.cfg.expression;
import petter.cfg.expression.visitors.PropagatingDFS;
import petter.cfg.expression.visitors.ExpressionVisitor;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;
import petter.cfg.expression.types.Char;
import petter.cfg.expression.types.PointerTo;
import petter.cfg.expression.types.Type;

/**
 * represents a constant integer value
 * @author Michael Petter
 * @author Andrea Flexeder
 */
public class StringLiteral implements Expression, java.io.Serializable{
    private String value;
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
     * create a new constant integer value 
     * @param value integer value
     */
    public StringLiteral(String value){
	this.value=value;
    }
    /**
     * @return string representation of integer value
     */
    public String toString(){
	return "\""+value+"\"";
    }
    /**
     * get the value of the StringLiteral
     * @return guess what?
     */
    public String getStringLiteral(){
	return value;
    }
    /**
     * an IntegerConstant cannot contain a multiplication
     * @return false
     */
    public boolean hasMultiplication(){
	return false;
    }
   /**
     * an IntegerConstant cannot contain a division
     * @return false
     */
    public boolean hasDivision(){
	return false;
    }
   /**
     * an IntegerConstant is not invertible
     * @return false
     */
    public boolean isInvertible(Variable var){
	return false;
    }
    /**
     * an IntegerConstant is not linear
     * @return false
     */
    public boolean isLinear(){
	return false;
    }
    /**
     * an IntegerConstant cannot contain a method call
     * @return false
     */
    public boolean hasMethodCall(){
	return false;
    }
    /**
     * an IntegerConstant cannot contain an UnknownExpression
     * @return false
     */
    public boolean hasUnknown(){
	return false;
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
     * degree of an integer value is always 0
     * @return guess what?
     */
    public int getDegree(){
	return 0;
    }
    public boolean equals(Object o){
        if (! (o instanceof StringLiteral)) return false;
        return (((StringLiteral)o).value == value);
    }

    @Override
    public Type getType() {
        return new PointerTo(Char.create());
    }
}

