package petter.cfg;

import java.util.*;


/**
 * @author Andrea Flexeder
 */
public class CompilationUnit implements Iterable<Procedure>,java.io.Serializable,Annotatable{
    private Map<Integer,String> varmapping;
    private Hashtable<String,Procedure> hashtab;
    private List<Integer> globals;
//    /**
//     *
//     */
//    public void setVariableMapping(Map<Integer,String> m){
//        this.varmapping = m;
//    }
    /**
     * @return the external name for this variable
     */
    public String getVariableName(int i){
        return varmapping.get(i);
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
     * create a new CFG for a class
     * @param hashtab Hashtable which contains all methods used in this class
     */
    public CompilationUnit(Map<Integer,String> m, Hashtable<String,Procedure> hashtab,List<Integer> fields){
        this.varmapping=m;
	this.hashtab=hashtab;
        this.globals=fields;
        for (Procedure meth : this){
            meth.setCompilationUnit(this);
        }
        
    }
    public List<Integer> getGlobals(){
        return globals;
    }
  
          
   /**
     * CFGMethod to given name
     * @return CFGMethode, or null if no Procedure of this name was defined yet
     */
    public Procedure getProcedure(String name){
	return hashtab.get(name);
    }
    /**
     * HashTable of all methods of this class
     * @return guess what?
     */
    public Map<String,Procedure> getProcedures(){
	return hashtab;
    }
    /**
     * search for the state with maximal number
     * @return guess what?
     */
    public int getStates(){
	long start = (long) Integer.MAX_VALUE;
	long end =0;
	int numberOfStates =0;
	for (Procedure meth : this){
	    end=(end < meth.getEnd().getId()) ? meth.getEnd().getId() : end;
	    start = (start > meth.getBegin().getId()) ? meth.getBegin().getId() : start;
	}
	numberOfStates = (int) end;
	return numberOfStates;
    }
    public State getState(long stateId){
        for (Procedure m : this){
            State s = m.getState(stateId);
            if (s!=null) return s;
        }
        return null;
    }
    /**
     * Iterator over all methods
     * @return guess what?
     */
    public Iterator<Procedure> iterator(){
	Collection col = hashtab.values();
	return col.iterator();
    }
}
