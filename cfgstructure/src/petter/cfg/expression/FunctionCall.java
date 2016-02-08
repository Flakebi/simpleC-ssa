package petter.cfg.expression;
import petter.cfg.expression.visitors.PropagatingDFS;
import petter.cfg.expression.visitors.ExpressionVisitor;
import java.util.ArrayList;
import java.util.List;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;
import java.util.stream.Stream;
import petter.cfg.expression.types.Type;
/**
 * represents a MethodCall as an Expression
 * @author Michael Petter
 * @author Andrea Flexeder
 */
public class FunctionCall implements Expression, java.io.Serializable{
    private String name;
    private List<Expression> params;

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
     * create a new MethodCall
     * @param name name of the method
     * @param params List of all the parameters of the method
     */
    public FunctionCall(String name, Type staticType, List<Expression> params){
	this.name = name;
	this.params=params;
        this.type=staticType;
    }
    /**
     * Collection of all the parameters of the method
     * @return guess what?
     */
    public Collection<Integer> getParams(){
        ArrayList<Integer> al = new ArrayList<Integer>();
        Iterator it = params.iterator();
        while (it.hasNext()) al.add(((Variable)it.next()).getId());
	return al;
    }
    public List<Expression> getParamsUnchanged() {
        return params;
    }
    /**
     * @return  the method's name
     */
    public String toString(){
        Iterator<Expression> it = params.iterator();
        StringBuffer sb = new StringBuffer();
        while(it.hasNext()){
            sb.append(it.next().toString());
            if (it.hasNext()) sb.append(" , ");
        }
	return name+"("+ sb.toString() + ")";
    }
    /**
     * get the name of the method
     * @return guess what?
     */
    public String getName(){
	return name;
    }
   /**
     * a MethodCall cannot contain a multiplication
     * @return false
     */
    public boolean hasMultiplication(){
	return false;
    }
    /**
     * a MethodCall cannot contain a division
     * @return false
     */
    public boolean hasDivision(){
	return false;
    }
    /**
     * a MethodCall is not invertible
     * @return false
     */
    public boolean isInvertible(Variable var){
	return false;
    }
    /**
     * a MethodCall is not linear
     * @return false
     */
    public boolean isLinear(){
	return true;
    }
   /**
    * @return guess what?
     */
    public boolean hasMethodCall(){
	return true;
    }
    /**
     * a MethodCall is sometimes handled as an UnknownExpression
     * @return true
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
        Iterator<Expression> it = params.iterator();
        while (it.hasNext()){
            it.next().accept(v);
        }
	v.postVisit(this);
    }

    @Override
    public <up, down> Optional<up> accept(PropagatingDFS<up, down> v, down parentValue) {
        return v.preVisit(this, parentValue)
                .flatMap(curr ->{
                    Stream<Optional<up>> abstractParams =
                            params.stream().
                                    map(param->param.accept(v, curr));
                    if(abstractParams.anyMatch(Optional.empty()::equals)) return Optional.empty();
                    else {
                        return Optional.of(v.postVisit(this,curr,abstractParams.map(o->o.get())));
                    }
                }
                );
    }
    
    /**
     * a MethodCall has no degree
     * @return -1
     */
    public int getDegree(){
	return -1;
    }
    public boolean equals(Object o){
        if (! (o instanceof FunctionCall)) return false;
        if (!name.equals(((FunctionCall)o).name)) return false;
        Iterator<Expression> it = params.iterator();
        Iterator<Expression> it2 = (((FunctionCall)o).params).iterator();
        while (it.hasNext()){
            if(!it2.hasNext()) return false;
            if (!it.next().equals(it2.next())) return false;
        }
        if(it2.hasNext()) return false;
        return true;
    }

    private Type type;
    /**
     * @return the static type of this call expression
     */
    @Override
    public Type getType() {
        return type;
    }
}

