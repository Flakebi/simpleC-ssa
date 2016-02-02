# SimpleC

This is a simplified C-like frontend, which produces Compilation Units, which
hold Control Flow Graphs.

Both projects come with a Netbeans Project file -- but You can of course just
use the command line build tool ANT, whose build.xml files come with the
projects.

```bash
git clone git@versioncontrolseidl.in.tum.de:petter/simpleC.git
cd simplec
ant
```

After compilation, you find the usable Frontend in simpleC/simplec/dist/Compiler.jar

It works on a simpleC file like:

```c
void* malloc(int i);
int foo(){
  int i = 42;
  return i;
}

int (*g)();

void main(){
  int i;
  int* j;
  j = malloc(10);
  for (i=0;i<10;i++){
      if ((i/2)*2==i) continue;
      j[i]=i+1;
  }
  i = j[9];
  j = &i;
  i = *j;
  if (i==5) 
    foo(); // triggers a MethodCall Transition
  else    
    i=foo(); // triggers an Assignment with an embedded MethodCall Expression!
  while(i==4711) {
    i=42-42;
  }
}
```

## simplec - subproject

The project is split in two subprojects, the SimpleC Frontend and the
Intermediate Representation CFGStructure. SimpleC contains the complete
Frontend of the SimpleC language; You can obtain a SimpleC Controlflowgraph via

```java
CompilationUnit cu = petter.simplec.Compiler.parse(File f);
```

SimpleC supports the following features:
- Definitions of functions/procedures with parameters and local variables
- Datatypes int, void, function and pointer to a type; arrays are atm. just pointers
- Prototypes for functions
- Types for (sub-) expressions
- Function pointers
- TODO: Weird initializers for arrays
- TODO: initializers/memoryallocation for structs/arrays
- TODO: Sizes for types; 
- TODO: type checking
- TODO: Casts
- basic arithmetics like +/-/*//
- address operators &/* 
- Prefix/Postfix operators work, just not for array expresssions
- control flow structures: if/while/for/goto/switch-case/do-while incl. break/continue
- Procedure call as (sub-) expressions
- TODO: break/continue-labels


## cfgstructure - subproject

The data structures for the IR can be found in the cfgstructure subproject. The
root for all IR related data is petter.cfg.CompilationUnit . You can obtain
all procedures and fields from a compilation unit, as well as general information
like all states and translations of variable-ids (integers) to 
source-level-variable-names. A petter.cfg.Procedure is a model of a particular CFG.
It offers informations like programstates, beginstate, endstate, and transitions
between the states. Normally, You do not have to touch this one directly, instead
rely on the fixpoint engine:

## Fixpoint engine

For the fixpoint-engine, you need to create your own class:

```java
import petter.cfg.*;
import petter.cfg.edges.*;
import java.io.*;


public class ReachabilityAnalysis extends AbstractPropagatingVisitor<Boolean>{

    static Boolean lub(Boolean b1,Boolean b2){
	    if (b1==null) return b2;
	    if (b2==null) return b1;
	    return b1||b2;
    }
    static boolean lessoreq(Boolean b1,Boolean b2){
	    if (b1==null) return true;
	    if (b2==null) return false;
	    return ((!b1) || b2);
    }

    CompilationUnit cu;
    public ReachabilityAnalysis(CompilationUnit cu){
        super(true); // forward reachability
	    this.cu=cu;
    }
    public Boolean visit(MethodCall m,Boolean b){
        // method calls need special attention; in this case, we just 
        // continue with analysing the next state and triggering the analysis
        // of the callee
	    enter(cu.getProcedure(m.getCallExpression().getName()),true);
	    return b;
    }
    public Boolean visit(State s,Boolean newflow){
	    Boolean oldflow = dataflowOf(s);
	    if (!lessoreq(newflow,oldflow)){
	        Boolean newval = lub(oldflow,newflow);
	        dataflowOf(s,newval);
	        return newval;
	    }
	    return null;
    }
   
    public static void main(String[] args) throws Exception {
        CompilationUnit cu = petter.simplec.Compiler.parse(new File(args[0]));
        ReachabilityAnalysis ra = new ReachabilityAnalysis(cu);
        Procedure foo = cu.getProcedure("main");
	    DotLayout layout = new DotLayout("jpg","main.jpg");
        ra.enter(foo,true);
        ra.fullAnalysis();
	    for (State s: foo.getStates()){
	        layout.highlight(s,(ra.dataflowOf(s))+"");
	    }
	    layout.callDot(foo);
    }
}
```
#### Abstract Data Flow Control
The AbstractPropagatingVisitor class already has a queue and evaluates
every node it finds within this queue by calling the particular visit(a,d)
method on the visitor itself. The value, returned by the visitor is given
to the next graph node, according to the Analysis' direction, either forward
along the transition or backward. This goes on until the a visit method 
returns null, which means, that vor this call, nothing will be entered in the
queue.

#### Abstract transitions via overwritten visits
It is thus common in particular analyses to overwrite the appropriate visit(_,d)
methods in order to manipulate the dataflow value d according to the CFG 
transition, that is visited at that time. All not overwritten methods call by
default the defaultBehaviour(a,d) method, in which you can implement generic 
transitions behaviour if desired. 

#### Abstractly interpreting expressions
In most cases, you will probably also want to
inspect expressions, that occur at a certain transition. For that, you will have
to either manually traverse the expression tree with lots of instanceofs or
use the petter.cfg.expression.AbstractExpressionVisitor . 

```java
Expression e =..
e.accept(new AbstractExpressionVisitor(){// silly visitor printing all constants
    public void postVisit(IntegerConstant s){
        System.out.println("Found constant "+s.getIntegerConst());
    }
});
```
#### Accumulating dataflow values

In order to implement a Fixpoint iteration, all that remains, is to store
the current dataflowvalue d in the CFG's state s via dataflowOf(s,d) and update
it in case that the newly arriving value in the visit(State,newval) method
is not already captured by it. You can see this in the example's implementation
of visit(State,Boolean).


## Graphical output

You can use the petter.cfg.DotLayout class to generate nice output of your
Analysis project; you create it with a file-format-specifier and a file name, and
it creates an OS-process of dot, from the graph layout software Graphviz. If you
then call callDot(p) with a cfgstructure-Procedure, then the tool will feed dot
with a graphical output of the CFG. If you call highlight(a,s) on the DotLayout
object with an Analyzable from the CFG and a custom string, then a speech bubble
with the string s pointing to a will be added to the graph.

![CFG example](/cfg.png "example output")

The upper graph is the graphical output for
```c
int b=42;
int a[42];
int **d;
int f();
int main(int a,int **f()){
  a=42;
  int c[42];
  while (a!=0 ){
    for(a=0;a<b;a++)
      a--;
    f();
    
  }
  switch(b){
  case 42: return 0;
  case 0: d[a]=*f();
  case 4711: f();
  defaut: break;
  }
  return a[0];
}
```


You can even visualize your fixpoint iteration, by overwriting the 
AbstractPropagatingVisitor.enter(a,d) method, to highlight the currently propagated
dataflow value at the appropriate graph component, and draw each step into a
different file, creating a sequence which visualizes your whole fixpoint computation.