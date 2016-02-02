package petter.cfg.expression;

public class Operator implements java.io.Serializable { //java.io.Externalizable{

    private static int[] properties;
    private static int[] invert;
    private static String[] names;
    private int code;
    private static final int COMPARATOR = 1;
    private static final int ADDITIVE = 2;
    private static final int MULTIPLICATIVE = 4;
    private static final int STRICT = 8;
    private static final int ADDRESSOP = 16;
    
    public static final int PLUS  ; 
    public static final int MINUS ; 
    public static final int DIV   ;
    public static final int MUL   ;
    public static final int EQ    ; 
    public static final int NEQ   ; 
    public static final int LE    ;
    public static final int LEQ   ; 
    public static final int GT    ;
    public static final int GTQ   ;  
    public static final int ASS   ;
    public static final int ARRAY ;
    public static final int ADDRESSOF;
    public static final int DEREF;
    public static final int SELECT;

    public Operator(int code){
        this.code = code;
    }

    static{

	PLUS  = 1; 
	MINUS = 2; 
	DIV   = 4;
	MUL   = 5;
	EQ    = 6; 
	NEQ   = 7; 
	LE    = 8;
	LEQ   = 9; 
	GT    =10;
	GTQ   =11;  
        ASS   =12;
        ARRAY =13;
        ADDRESSOF = 14;
        DEREF = 15;
        SELECT = 16;
        
	invert = new int[ 17 ];
	properties = new int [ 17 ];
	names = new String [ 17 ];

	invert[PLUS]=MINUS;
	invert[MINUS]= PLUS;
	invert[DIV]=MUL;
	invert[MUL]=DIV;
	invert[EQ]=NEQ;
	invert[NEQ]=EQ;
	invert[LE]=GTQ;
	invert[LEQ]=GT;
	invert[GT]=LEQ;
	invert[GTQ]=LE;
        invert[ADDRESSOF]=DEREF;
        invert[DEREF]=ADDRESSOF;
	
	

	properties[PLUS]=ADDITIVE;
	properties[(MINUS)]=ADDITIVE;
	properties[(DIV)]=MULTIPLICATIVE;
	properties[(MUL)]=MULTIPLICATIVE;
	properties[(EQ)]=COMPARATOR;
	properties[(NEQ)]=COMPARATOR;
	properties[(LE)]=(COMPARATOR | STRICT);
	properties[(LEQ)]=COMPARATOR;
	properties[(GT)]=(COMPARATOR | STRICT);
	properties[(GTQ)]=COMPARATOR;
        properties[ARRAY]=0;
        properties[ADDRESSOF]=ADDRESSOP;
        properties[DEREF]=ADDRESSOP;
        properties[SELECT]=0;
        
	names[PLUS]="+";
	names[MINUS]="-";
	names[DIV]="/";
	names[MUL]="*";
	names[EQ]="==";
	names[NEQ]="!=";
	names[LE]="<";
	names[LEQ]="<=";
	names[GT]=">";
	names[GTQ]=">=";
        names[ASS]="=";
        names[ARRAY]=" [ . ] ";
        names[ADDRESSOF]="&";
        names[DEREF]="*";
        names[SELECT]=".";
    }

    
    public Operator invert(){
	return new Operator(invert[code]);
    }
   
    public int getCode(){
	return this.code;
	}
    
    public int makeUnstrict(){
	if(!isStrict()) return 0;
	if(this.is(GT)){
	    code = GTQ;
	    return -1;
		}
	if(this.is(LE)){
	    code = LEQ;
	    return +1;
		}
	return 0;
    }
    public boolean isStrict(){
	if((properties[code] & STRICT) != 0) return true;
	return false;
    }
    public boolean compare(int lhs, int rhs){
	assert isComparator(); // test of  precondition
	if(this.is(GTQ)) return lhs >= rhs;
	if(this.is(LEQ)) return lhs <=rhs;
	if(this.is(LE)) return lhs <rhs;
	if(this.is(GT)) return lhs >rhs;
	if(this.is(EQ)) return lhs ==rhs;
	System.err.println("VORSICHT sollte nicht vorkommen!!");
	return false;
    }
 public boolean compare(double lhs, double rhs){
	assert isComparator(); // test of  precondition
	if(this.is(GTQ)) return lhs >= rhs;
	if(this.is(LEQ)) return lhs <=rhs;
	if(this.is(LE)) return lhs <rhs;
	if(this.is(GT)) return lhs >rhs;
	if(this.is(EQ)) return lhs ==rhs;
	System.err.println("VORSICHT sollte nicht vorkommen!!");
	return false;
    }



    public boolean isComparator(){
       return ((properties[code] & COMPARATOR)!= 0);
    }
    public boolean isAdditive(){
	return ((properties[code] & ADDITIVE) != 0);
    }
    public boolean isMultiplicative(){
	return ((properties[code] & MULTIPLICATIVE)!= 0);
    }
    public boolean isAddressOp(){
        return ((properties[code] & ADDRESSOP) != 0);
    }
    public boolean is(int code){
	return this.code==code;
    }
    public boolean equals(Operator op){
	if (this.code == op.code ) return true;
	return false;
	
    }
    public String toString(){
	return names[code];
    }
}
