package petter.cfg.expression;
import petter.cfg.expression.visitors.PropagatingDFS;
import petter.cfg.expression.visitors.ExpressionVisitor;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;
import petter.cfg.Annotatable;
import petter.cfg.expression.types.Type;
/**
 * represents a Variable in an expression
 * @author Michael Petter
 * @author Andrea Flexeder
 */
public class Variable implements Expression, Annotatable, java.io.Serializable{
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


    private int id;
    /**
     * create a new Variable     
     * @param id integer value of variable
     */
    private Variable(int id, Type typ){
	this.id=id;
        this.type=typ;
    }
    public Variable(int id, String externalname, Type typ
                    //, String scope
        ){
        this(id,typ);
        putAnnotation("external name",externalname);
        //putAnnotation("scope",scope);
    }
    
    private Type type;
    public Type getType(){
        return type;
    }
    
    /**
     * the Id of the variable
     * @return guess what?
     */
    public int getId(){
	return id;
    }
    /**
     *
     */
    public int hashCode(){
      return (new Integer(id)).hashCode();
    }
    /**
     * check if two variables are equal
     * @return guess what?
     */
    public boolean equals(Object o){
	if (!(o instanceof Variable)) return false;
   	Variable other= (Variable)o;
	return id==other.id;
    }
    /**
     * the variables name
     * @return guess what?
     */
    public String toString(){
        String name = (String)getAnnotation("external name");
        if (name==null) return "(Variable: #"+id+")";
        else return name;
    }
    /**
     * a variable cannot contain a multiplication
     * @return false
     */
    public boolean hasMultiplication(){
	return false;
    }
    /**
     * a variable cannot contain a division
     * @return false
     */
    public boolean hasDivision(){
	return false;
    }
    /**
     * check if the variable is invertible 
     * @return guess what?
     */
    public boolean isInvertible(Variable var){
	if(this.equals(var)) return true;
	    return false;
    }
   /**
     * a variable is always linear
     * @return true
     */
    public boolean isLinear(){
	return true;
    }
  /**
     * a variable cannot contain a methodCall
     * @return false
     */
    public boolean hasMethodCall(){
	return false;
    }
/**
     * a variable cannot contain an unknown expression
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
     * get the degree of a variable
     * degree of a single variable's always 1
     * @return 1
     */
    public int getDegree(){
	return 1;
    }
}

