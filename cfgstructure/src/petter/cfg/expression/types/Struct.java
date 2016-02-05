/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package petter.cfg.expression.types;

import java.util.Map;

/**
 *
 * @author petter
 */
public class Struct extends Type {
    private String name;
    private Map<String,Type> inner;
    public Struct(String name,Map<String,Type> inner){
        this.inner=inner;
        this.name=name.intern();
    }
    public Struct(Map<String,Type> inner){
        this.inner=inner;
        this.name=null;
    }

    public void bind(Map<String,Type> m){
        if (inner==null) inner=m;
        else throw new UnsupportedOperationException("Map is already bound to "+inner);
    }


    public Map<String,Type> getInner(){ 
        return inner; 
    }
    public String getName(){
        return name;
    }

    @Override
    public boolean isCallable() {
        return false;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Struct)) return false;
        return inner.equals(((Struct)obj).inner) && name==((Struct)obj).name;
    }

    @Override
    public boolean isBasicType() {
        return false;
    }

    @Override
    public boolean hasPointer() {
        return false;
    }

    @Override
    public String toString() {
        String tostring="struct ";
        if (name!=null) tostring+=name+" ";
        return tostring;
    }
    
    public String toDetailedString() {
        String tostring="struct ";
        if (name!=null) tostring+=name+" ";
        return tostring+inner;
    }
    
    
    
}
