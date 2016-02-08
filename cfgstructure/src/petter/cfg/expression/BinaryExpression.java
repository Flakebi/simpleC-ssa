package petter.cfg.expression;
import petter.cfg.expression.visitors.PropagatingDFS;
import petter.cfg.expression.visitors.ExpressionVisitor;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;
import petter.cfg.expression.types.Int;
import petter.cfg.expression.types.PointerTo;
import petter.cfg.expression.types.Struct;
import petter.cfg.expression.types.Type;
/**
 * represents a BinaryExpression
 * @author Michael Petter
 * @author Andrea Flexeder
 */
public class BinaryExpression implements Expression, java.io.Serializable{
    private Expression left;
    private Expression right;
    private Operator sign;
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
     * create a new BinaryExpression   
     * @param left expression on the lefthandside
     * @param sign operator that connect left and right to a BinaryExpression
     * @param right expression on the righthandside
     */
    public BinaryExpression (Expression left, Operator sign, Expression right){
    assert left!=null : "beware: lefthand side of expression is empty";
    assert right!=null : "beware: righthand side of expression is empty";
    assert sign!=null : "beware: sign is null!";
    
	this.left = left;
	this.right = right;
	this.sign = sign;
    }
    /**
     * @return string representation of the BinaryExpression
     */
    public String toString(){
	if (sign.is(Operator.ARRAY)) return left.toString()+"["+right.toString()+"]";
        if (sign.isMultiplicative()) return "("+left.toString()+")"+sign.toString()+"("+right.toString()+")";
	if (sign.is(Operator.MINUS)) return left.toString()+sign.toString()+"("+right.toString()+")";
	return left.toString()+sign.toString()+right.toString();
    }
    /**
     * get the operator of the BinaryExpression
     * @return guess what?
     */
    public Operator getOperator(){
	return sign;
    }
    /**
     * get the expression on the lefthandside
     * @return guess what?
     */
    public Expression getLeft(){
	return left;	
    }
   /**
     * get the expression on the righthandside
     * @return guess what?
     */
    public Expression getRight(){
	return right;
    }
    /**
     * set the operator of the BinaryExpression
     */
    public void setOperator(Operator op){
	this.sign = op;
    }
    /**
     * set the expression on the lefthandside
     */
    public void setLeft(Expression l){
	this.left = l;
    }
    /**
     * set the expression on the lefthandside
     */
    public void setRight(Expression r){
	this.right = r;
    }
    /**
     * check if the BinaryExpression contains a multiplication
     * @return guess what?
     */
    public boolean hasMultiplication(){
	if(sign.is(Operator.MUL)) return true;
	return (left.hasMultiplication() || right.hasMultiplication());
    }
    /**
     * check if the BinaryExpression contains a division
     * @return guess what?
     */
    public boolean hasDivision(){
	if(sign.is(Operator.DIV)) return true;
	return (left.hasDivision() || right.hasDivision());
    }
    /**
     * check if the BinaryExpression is invertible
     * @return guess what?
     */
    public boolean isInvertible(Variable var){
	if((sign.is( Operator.MUL)) || (sign.is(Operator.DIV))) return (left.isInvertible(var) ^ right.isInvertible(var));
	if((sign.is(Operator.PLUS)) || (sign.is(Operator.MINUS))) return (left.isInvertible(var) || right.isInvertible(var));
	return false;
    }
    /**
     * check if the BinaryExpression is linear
     * @return guess what?
     */
    public boolean isLinear(){
	int deg = getDegree();
	return ( deg== 1 || deg==0);
    }
    /**
     * check if the BinaryExpression contains a method call
     * @return guess what?
     */
    public boolean hasMethodCall(){
	return (left.hasMethodCall() || right.hasMethodCall());
    } 
    /**
     * check if the BinaryExpression contains an UnknownExpression
     * @return guess what?
     */
    public boolean hasUnknown(){
	//if (sign == Operator.MOD || sign == Operator.LSHIFT || sign == Operator.RSHIFT) return true;
	return ((left.hasUnknown() || right.hasUnknown()) || sign==null);
    }
    /**
     * analysis of an expression
     * @param v the analysing ExpressionVisitor
     */
    public void accept(ExpressionVisitor v){
	if (v.preVisit(this)){
	    left.accept(v);
	    right.accept(v);
	}
	v.postVisit(this);
    }

    @Override
    public <up, down> Optional<up> accept(PropagatingDFS<up, down> v, down parentValue) {
        return v.preVisit(this, parentValue)
                .flatMap(curr->this.left.accept(v,curr)
                        .flatMap(aLhs-> right.accept(v,curr)
                                .map(aRhs-> v.postVisit(this,aLhs,aRhs))));
                        
    }
    
    /**
     * get the degree of the BinaryExpression 
     * @return guess what?
     */
    public int getDegree(){
	if(sign.is(Operator.MINUS) || sign.is(Operator.PLUS)) return Math.max(left.getDegree(), right.getDegree());
	if (sign.is(Operator.MUL)) return (right.getDegree() + left.getDegree());
	if(sign.is(Operator.DIV)) return ( (right.getDegree() == 0)?left.getDegree():-1);
	
	return -1;
    }
    public boolean equals(Object o){
        if (! (o instanceof BinaryExpression)) return false;
        if (!left.equals(((BinaryExpression)o).left)) return false;
        if (!right.equals(((BinaryExpression)o).right)) return false;
        if (!sign.equals(((BinaryExpression)o).sign)) return false;
        return true;
    }

    @Override
    public boolean hasArrayAccess() {
        if (sign.equals(Operator.ARRAY)) return true;
        else return left.hasArrayAccess()||right.hasArrayAccess();
    }

    @Override
    public Type getType() {
        if (sign.is(Operator.ARRAY)){
            if(right.getType()!= Int.create()) throw new UnsupportedOperationException("Array index is not of type integer."); 
            if(!(left.getType() instanceof PointerTo )) throw new UnsupportedOperationException("Array base is not of type pointer."); 
            return ((PointerTo)left.getType()).getInner();
        }
        if (sign.isComparator()){
            if (!right.getType().equals(left.getType()))
                throw new UnsupportedOperationException("two different types in boolean expression; real type generalization is not supported yet."); 
            return Int.create();
        }
        if (sign.is(Operator.SELECT)){
            if (!(left.getType() instanceof Struct))
                throw new UnsupportedOperationException("Trying to select from non-struct variable "+left+" of type "+left.getType());
            Struct st = (Struct)left.getType();
            String accessor = (String)right.getAnnotation("external name");
            Type innert = st.getInner().get(accessor);
            if (innert==null){
                throw new UnsupportedOperationException("Trying to access a "+left.getType()+" typed struct with accessor "+accessor);
            }
            else return innert;
        }
        // Operator is multiplicative or additive
        if (!right.getType().equals(left.getType()))
            throw new UnsupportedOperationException("two different types in binary expression:\n "
                    + left.getType()+" "+sign+" " + right.getType()+"\n"
                    + "real type generalization is not supported yet."); 
        return right.getType();
    }

}

