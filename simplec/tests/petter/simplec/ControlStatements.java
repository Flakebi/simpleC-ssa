package petter.simplec;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.junit.Test;

import petter.cfg.CompilationUnit;
import petter.cfg.Procedure;
import petter.cfg.State;
import petter.cfg.edges.Assignment;
import petter.cfg.edges.GuardedTransition;
import petter.cfg.edges.Nop;
import petter.cfg.edges.Transition;
import petter.cfg.expression.BinaryExpression;
import petter.cfg.expression.IntegerConstant;
import petter.cfg.expression.Operator;
import petter.cfg.expression.Variable;
import petter.utils.AnnotatingSymbolFactory;

public class ControlStatements {
	private static String fromOneExpressionOnly(String expression){
		return "int a;"
				+ "int b;"
				+ "int c;"
				+ "int *p;"
				+ "int main(){"
				+ expression
				+ "}";
		
	}
	private static CompilationUnit compile(String programcode) throws Exception{
		InputStream is = new ByteArrayInputStream(programcode.getBytes(StandardCharsets.UTF_8));
        AnnotatingSymbolFactory sf = new AnnotatingSymbolFactory();
        Parser parser = new Parser(new Lexer(is,sf),sf);
        return (CompilationUnit)parser.parse().value;
	}
	private static Transition extractMainTransition(CompilationUnit cu){
		Procedure main = cu.getProcedure("main");
		return main.getBegin().getOut().iterator().next();
	}
	private static Transition nextNoNopTransition(Transition now){
		while ((now =directNextTransition(now)) instanceof Nop );
		return now;
	}
	private static Transition directNextTransition(Transition now){
		return now.getDest().getOut().iterator().next();
	}
	private static boolean isLastTransition(Transition now){
		return now.getDest().isEnd();
	}
	
	// if(a==1) s1; else s2;
	@Test
	public void ifThenElse() {
		try {

			State beforeIf = compile(fromOneExpressionOnly("if(a==1) a*=a; else a/=a;")).getProcedure("main").getBegin();
		
			boolean pos=false,neg=false;
			State endState=null;
			for (Transition trans :beforeIf.getOut()){
				assertTrue(trans instanceof GuardedTransition);
				// (a-1) ?=0
				GuardedTransition guard = (GuardedTransition)trans;

				Operator op = guard.getOperator();
				int code=0;
				if (op.is(Operator.EQ)){
					assertTrue(!pos);
					pos=true;
					code=Operator.MUL;
				}
				else if (op.is(Operator.NEQ)){
					assertTrue(!neg);
					neg=true;
					code=Operator.DIV;
				}
				else fail("Unexpected BinaryExpression found "+op);
				
				// a = a ? a;
				Assignment next = (Assignment)directNextTransition(guard);
				assertTrue(((BinaryExpression)next.getRhs()).getOperator().is(code));
				
				// $1 = a;
				next = (Assignment)directNextTransition(next);
				if (endState==null) endState=next.getDest();
				else assertTrue(next.getDest().equals(endState));
			}
			assertTrue(pos&&neg);
			assertTrue(endState.isEnd());
		}catch (Exception ex){
			fail("Unexpected Exception "+ex);
			ex.printStackTrace();
		}
	}
	// if(a) s1;
	@Test
	public void ifThen() {
		try {

			State beforeIf = compile(fromOneExpressionOnly("if(a==1) a*=a;")).getProcedure("main").getBegin();
		
			boolean pos=false,neg=false;
			State endState=null;
			for (Transition trans :beforeIf.getOut()){
				assertTrue(trans instanceof GuardedTransition);
				// (a-1) ?=0
				GuardedTransition guard = (GuardedTransition)trans;

				Operator op = guard.getOperator();
				int code=0;
				Assignment next = null;
				if (op.is(Operator.EQ)){
					assertTrue(!pos);
					pos=true;
					code=Operator.MUL;
					// a = a ? a;
					next= (Assignment)directNextTransition(guard);
					assertTrue(((BinaryExpression)next.getRhs()).getOperator().is(code));
					
					// $1 = a;
					next = (Assignment)directNextTransition(next);
					trans=next;
				}
				else if (op.is(Operator.NEQ)){
					assertTrue(!neg);
					neg=true;
				}
				else fail("Unexpected BinaryExpression found "+op);
				
				if (endState==null) endState=trans.getDest();
				else assertTrue(trans.getDest().equals(endState));
			}
			assertTrue(pos&&neg);
			assertTrue(endState.isEnd());
		}catch (Exception ex){
			fail("Unexpected Exception "+ex);
			ex.printStackTrace();
		}
	}
	// while(a==1) s1; s2;
	@Test
	public void whileTest() {
		try {

			State beforeIf = compile(fromOneExpressionOnly("while(a==1) a*=a;")).getProcedure("main").getBegin();
		
			boolean pos=false,neg=false;
			for (Transition trans :beforeIf.getOut()){
				assertTrue(trans instanceof GuardedTransition);
				// (a-1) ?=0
				GuardedTransition guard = (GuardedTransition)trans;

				Operator op = guard.getOperator();
				int code=0;
				Assignment next = null;
				if (op.is(Operator.EQ)){
					assertTrue(!pos);
					pos=true;
					code=Operator.MUL;
					// a = a ? a;
					next= (Assignment)directNextTransition(guard);
					assertTrue(((BinaryExpression)next.getRhs()).getOperator().is(code));
					
					// $1 = a;
					next = (Assignment)directNextTransition(next);
					assertTrue(next.getDest().equals(trans.getSource()));
				}
				else if (op.is(Operator.NEQ)){
					assertTrue(!neg);
					neg=true;
					assertTrue(trans.getDest().isEnd());
				}
				else fail("Unexpected BinaryExpression found "+op);
				
				
				
			}
			assertTrue(pos&&neg);
		}catch (Exception ex){
			fail("Unexpected Exception "+ex);
			ex.printStackTrace();
		}
	}
	// do s1; while(a==1);
	@Test
	public void doWhileTest() {
		try {

			CompilationUnit cu = compile(fromOneExpressionOnly("do a*=a; while(a==1) ;"));
			State jumptarget = extractMainTransition(cu).getDest();

			// a = a*a
			Transition transition = jumptarget.getOutIterator().next();
			assertTrue(transition instanceof Assignment);
			Assignment a = (Assignment)transition;
			assertTrue(a.getLhs() instanceof Variable);
			assertTrue(a.getRhs() instanceof BinaryExpression);
			
			// $1 = a
			transition = directNextTransition(transition);
			assertTrue(transition instanceof Assignment);
			a = (Assignment)transition;
			assertTrue(a.getLhs() instanceof Variable);
			assertTrue(a.getRhs() instanceof Variable);
			
			State branch = transition.getDest();
			boolean pos=false,neg=false;
			for (Transition trans :branch.getOut()){
				assertTrue(trans instanceof GuardedTransition);
				// (a-1) ?=0
				GuardedTransition guard = (GuardedTransition)trans;

				Operator op = guard.getOperator();
				if (op.is(Operator.EQ)){
					assertTrue(!pos);
					pos=true;

					assertTrue(trans.getDest().equals(jumptarget));
				}
				else if (op.is(Operator.NEQ)){
					assertTrue(!neg);
					neg=true;
					assertTrue(trans.getDest().isEnd());
				}
				else fail("Unexpected BinaryExpression found "+op);
			}
			assertTrue(pos&&neg);
		}catch (Exception ex){
			fail("Unexpected Exception "+ex);
			ex.printStackTrace();
		}
	}
	// return 0;
	@Test
	public void returnTest(){
		try {
			// return = 0
			Transition main = extractMainTransition(compile(fromOneExpressionOnly("return 0; a+=a;")));
			assertTrue(main instanceof Assignment);
			Assignment a = (Assignment)main;
			assertTrue(a.getRhs() instanceof IntegerConstant);
			
			assertTrue(main.getDest().isEnd());
		}catch (Exception ex){
			fail("Unexpected Exception "+ex);
		}
	}
	
	// for(a=1;a<42;a++) b++; 
	@Test
	public void forTest() {
		try {

			Transition main = extractMainTransition(compile(fromOneExpressionOnly("for(a=1;a<42;a++) b*=b; ")));
			// a = 1
			assertTrue(main instanceof Assignment);
			Assignment a = (Assignment)main;
			assertTrue(a.getLhs() instanceof Variable);
			assertTrue(a.getRhs() instanceof IntegerConstant);

			// $1 = a
			main = directNextTransition(main);
			assertTrue(main instanceof Assignment);
			a = (Assignment)main;
			assertTrue(a.getLhs() instanceof Variable);
			assertTrue(a.getRhs() instanceof Variable);
						
		
			State beforeIf = main.getDest();
			boolean pos=false,neg=false;
			for (Transition trans :beforeIf.getOut()){
				assertTrue(trans instanceof GuardedTransition);
				// (a-42) ?=0
				GuardedTransition guard = (GuardedTransition)trans;

				Operator op = guard.getOperator();

				if (op.is(Operator.LE)){
					assertTrue(!pos);
					pos=true;
					
					// b = b *b
					Assignment next = (Assignment)directNextTransition(guard);
					assertTrue(next instanceof Assignment);
					assertTrue(a.getLhs() instanceof Variable);
					assertTrue(((BinaryExpression)next.getRhs()).getOperator().is(Operator.MUL));
										
					// $2 = b
					next= (Assignment)directNextTransition(next);
					assertTrue(next instanceof Assignment);
					assertTrue(a.getLhs() instanceof Variable);
					assertTrue(a.getRhs() instanceof Variable);
					
					// $3 = a
					next = (Assignment)directNextTransition(next);
					assertTrue(next instanceof Assignment);
					assertTrue(a.getLhs() instanceof Variable);
					assertTrue(a.getRhs() instanceof Variable);

					// a= a + 1
					next = (Assignment)directNextTransition(next);
					assertTrue(next instanceof Assignment);
					assertTrue(a.getLhs() instanceof Variable);
					assertTrue(((BinaryExpression)next.getRhs()).getOperator().is(Operator.PLUS));
					
					assertTrue(next.getDest().equals(trans.getSource()));
				}
				else if (op.is(Operator.GTQ)){
					assertTrue(!neg);
					neg=true;
					assertTrue(trans.getDest().isEnd());
				}
				else fail("Unexpected BinaryExpression found "+op);
				
				
				
			}
			assertTrue(pos&&neg);
		}catch (Exception ex){
			fail("Unexpected Exception "+ex);
			ex.printStackTrace();
		}
	}

	// s1; goto C; B:  s2; return 0; C:s3; goto B; s4;
	@Test
	public void gotoTest() {
		try {
			Transition main = extractMainTransition(compile(fromOneExpressionOnly("a+=a; goto C; B:  b*=b; return 0;	C:  c/=c; goto B; a-=a;")));
			// a = a+a
			assertTrue(main instanceof Assignment);
			Assignment a = (Assignment)main;
			assertTrue(a.getRhs() instanceof BinaryExpression);
			assertTrue(((BinaryExpression)a.getRhs()).getOperator().is(Operator.PLUS));

			// $1 = a
			main = directNextTransition(main);
			assertTrue(main instanceof Assignment);
			a = (Assignment)main;
			assertTrue(a.getRhs() instanceof Variable);
			
			// c = c / c
			main = directNextTransition(main);
			assertTrue(main instanceof Assignment);
			a = (Assignment)main;
			assertTrue(a.getRhs() instanceof BinaryExpression);
			assertTrue(((BinaryExpression)a.getRhs()).getOperator().is(Operator.DIV));
			
			// $2 = c
			main = directNextTransition(main);
			assertTrue(main instanceof Assignment);
			a = (Assignment)main;
			assertTrue(a.getRhs() instanceof Variable);
			
			// b = b * b
			main = directNextTransition(main);
			assertTrue(main instanceof Assignment);
			a = (Assignment)main;
			assertTrue(a.getRhs() instanceof BinaryExpression);
			assertTrue(((BinaryExpression)a.getRhs()).getOperator().is(Operator.MUL));
			
			// $3 = b
			main = directNextTransition(main);
			assertTrue(main instanceof Assignment);
			a = (Assignment)main;
			assertTrue(a.getRhs() instanceof Variable);
			
			// return = 0
			main = directNextTransition(main);
			assertTrue(main instanceof Assignment);
			a = (Assignment)main;
			assertTrue(a.getRhs() instanceof IntegerConstant);
			
			assertTrue(main.getDest().isEnd());

		}catch (Exception ex){
			fail("Unexpected Exception "+ex);
		}
	}
	// while (a==1) if(b==1) break;
	@Test
	public void breakLoopTest() {
		try {
			State whilestart = compile(fromOneExpressionOnly("while (a==1) if(b==1) break;")).getProcedure("main").getBegin();
			
			boolean pos=false,neg=false;
			for (Transition trans :whilestart.getOut()){
				assertTrue(trans instanceof GuardedTransition);
				// (a-1) ?=0
				GuardedTransition guard = (GuardedTransition)trans;

				Operator op = guard.getOperator();

				if (op.is(Operator.EQ)){
					assertTrue(!pos);
					pos=true;
					
					for (Transition tt :trans.getDest().getOut()){
						assertTrue(tt instanceof GuardedTransition);
						// (b-1)?=0
						GuardedTransition guard2 = (GuardedTransition)tt;
						
						Operator op2= guard2.getOperator();
						
						if (op2.is(Operator.NEQ))
							assertTrue(tt.getDest().equals(whilestart));
						if (op2.is(Operator.EQ)){
							tt = directNextTransition(tt);
							assertTrue(tt instanceof Nop);
							assertTrue(tt.getDest().isEnd());
						}
							
					}
					
				}
				else if (op.is(Operator.NEQ)){
					assertTrue(!neg);
					neg=true;
					assertTrue(trans.getDest().isEnd());
				}
				else fail("Unexpected BinaryExpression found "+op);

				
			}
			assertTrue(neg&&pos);
		} catch (Exception ex){
			fail("Unexpected Exception "+ex);
		}
	}
	// while (a==1) if(b==1) continue;
	@Test
	public void continueTest() {
		try {
			State whilestart = compile(fromOneExpressionOnly("while (a==1) if(b==1) continue;")).getProcedure("main").getBegin();
			
			boolean pos=false,neg=false;
			for (Transition trans :whilestart.getOut()){
				assertTrue(trans instanceof GuardedTransition);
				// (a-1) ?=0
				GuardedTransition guard = (GuardedTransition)trans;

				Operator op = guard.getOperator();

				if (op.is(Operator.EQ)){
					assertTrue(!pos);
					pos=true;
					for (Transition tt :trans.getDest().getOut()){
						assertTrue(tt instanceof GuardedTransition);
						// (b-1)?=0
						GuardedTransition guard2 = (GuardedTransition)tt;
						
						Operator op2= guard2.getOperator();
						
						if (op2.is(Operator.NEQ))
							assertTrue(tt.getDest().equals(whilestart));
						if (op2.is(Operator.EQ)){
							tt = directNextTransition(tt);
							assertTrue(tt instanceof Nop);
							assertTrue(tt.getDest().equals(whilestart));
						}
							
					}
					
				}
				else if (op.is(Operator.NEQ)){
					assertTrue(!neg);
					neg=true;
					assertTrue(trans.getDest().isEnd());
				}
				else fail("Unexpected BinaryExpression found "+op);

				
			}
			assertTrue(neg&&pos);
		} catch (Exception ex){
			fail("Unexpected Exception "+ex);
		}
	}
	// switch(a) {  case 1: c*=c;  case 2: c+=c; }
	@Test
	public void switchTest() {
		try {
			State switchstart = compile(fromOneExpressionOnly("switch(a) {  case 1: c*=c;  case 2: c+=c; }")).getProcedure("main").getBegin();
			boolean pos=false,neg=false;
			State joinpoint = null;
			State splitpoint = null;
			for (Transition trans :switchstart.getOut()){
				assertTrue(trans instanceof GuardedTransition);
				// (a-1) ?=0
				GuardedTransition guard = (GuardedTransition)trans;

				Operator op = guard.getOperator();

				if (op.is(Operator.NEQ)){
					assertTrue(!pos);
					pos=true;

					splitpoint = guard.getDest();
					
				}
				else if (op.is(Operator.EQ)){
					assertTrue(!neg);
					neg=true;
					

					Transition main = directNextTransition(trans);
					// c = c*c
					assertTrue(main instanceof Assignment);
					Assignment a = (Assignment)main;
					assertTrue(a.getRhs() instanceof BinaryExpression);
					assertTrue(((BinaryExpression)a.getRhs()).getOperator().is(Operator.MUL));

					// $1 = c
					main = directNextTransition(main);
					assertTrue(main instanceof Assignment);
					a = (Assignment)main;
					assertTrue(a.getRhs() instanceof Variable);
					
					joinpoint = main.getDest();
					
					// c = c + c
					main = directNextTransition(main);
					assertTrue(main instanceof Assignment);
					a = (Assignment)main;
					assertTrue(a.getRhs() instanceof BinaryExpression);
					assertTrue(((BinaryExpression)a.getRhs()).getOperator().is(Operator.PLUS));

					// $2 = c
					main = directNextTransition(main);
					assertTrue(main instanceof Assignment);
					a = (Assignment)main;
					assertTrue(a.getRhs() instanceof Variable);
					
					assertTrue(isLastTransition(main));
					
					
				}
				else fail("Unexpected BinaryExpression found "+op);
			}
			
			
			
			assertTrue(neg&&pos);
			neg=pos=false;
			
			for (Transition trans :splitpoint.getOut()){
				assertTrue(trans instanceof GuardedTransition);
				// (a-2) ?=0
				GuardedTransition guard = (GuardedTransition)trans;

				Operator op = guard.getOperator();

				if (op.is(Operator.NEQ)){
					assertTrue(!pos);
					pos=true;

					trans=directNextTransition(trans);
					assertTrue(trans.getDest().isEnd());
				}
				else if (op.is(Operator.EQ)){
					assertTrue(!neg);
					neg=true;					
					assertTrue(guard.getDest().equals(joinpoint));
				}
			}
			

		} catch (Exception ex){
			fail("Unexpected Exception "+ex);
		}
	}
	// switch(a) {  case 1: c*=c;  default:c/=c; case 2: c+=c; }
	@Test
	public void defaultTest() {
		try {
			State switchstart = compile(fromOneExpressionOnly("switch(a) {  case 1: c*=c;  default:c/=c; case 2: c+=c; }")).getProcedure("main").getBegin();
			boolean pos=false,neg=false;
			State joinpoint = null;
			State defaultpoint = null;
			State splitpoint = null;
			for (Transition trans :switchstart.getOut()){
				assertTrue(trans instanceof GuardedTransition);
				// (a-1) ?=0
				GuardedTransition guard = (GuardedTransition)trans;

				Operator op = guard.getOperator();

				if (op.is(Operator.NEQ)){
					assertTrue(!pos);
					pos=true;

					splitpoint = guard.getDest();
					
				}
				else if (op.is(Operator.EQ)){
					assertTrue(!neg);
					neg=true;
					

					Transition main = directNextTransition(trans);
					// c = c*c
					assertTrue(main instanceof Assignment);
					Assignment a = (Assignment)main;
					assertTrue(a.getRhs() instanceof BinaryExpression);
					assertTrue(((BinaryExpression)a.getRhs()).getOperator().is(Operator.MUL));

					// $1 = c
					main = directNextTransition(main);
					assertTrue(main instanceof Assignment);
					a = (Assignment)main;
					assertTrue(a.getRhs() instanceof Variable);
					
					defaultpoint = main.getDest();
					
					// c = c / c
					main = directNextTransition(main);
					assertTrue(main instanceof Assignment);
					a = (Assignment)main;
					assertTrue(a.getRhs() instanceof BinaryExpression);
					assertTrue(((BinaryExpression)a.getRhs()).getOperator().is(Operator.DIV));

					// $2 = c
					main = directNextTransition(main);
					assertTrue(main instanceof Assignment);
					a = (Assignment)main;
					assertTrue(a.getRhs() instanceof Variable);

					joinpoint = main.getDest();

					// c = c + c
					main = directNextTransition(main);
					assertTrue(main instanceof Assignment);
					a = (Assignment)main;
					assertTrue(a.getRhs() instanceof BinaryExpression);
					assertTrue(((BinaryExpression)a.getRhs()).getOperator().is(Operator.PLUS));

					// $2 = c
					main = directNextTransition(main);
					assertTrue(main instanceof Assignment);
					a = (Assignment)main;
					assertTrue(a.getRhs() instanceof Variable);
					
					assertTrue(isLastTransition(main));
					
					
				}
				else fail("Unexpected BinaryExpression found "+op);
			}
			
			
			
			assertTrue(neg&&pos);
			neg=pos=false;
			
			for (Transition trans :splitpoint.getOut()){
				assertTrue(trans instanceof GuardedTransition);
				// (a-2) ?=0
				GuardedTransition guard = (GuardedTransition)trans;

				Operator op = guard.getOperator();

				if (op.is(Operator.NEQ)){
					assertTrue(!pos);
					pos=true;

					trans=directNextTransition(trans);
					assertTrue(trans.getDest().equals(defaultpoint));
				}
				else if (op.is(Operator.EQ)){
					assertTrue(!neg);
					neg=true;					
					assertTrue(guard.getDest().equals(joinpoint));
				}
			}
			

		} catch (Exception ex){
			fail("Unexpected Exception "+ex);
		}
	}
	// switch(a) {  case 1: c*=c;   default:c/=c; break; case 2: c+=c;}
	@Test
	public void switchBreakTest() {
		try {
			State switchstart = compile(fromOneExpressionOnly("switch(a) {  case 1: c*=c; default:c/=c; break; case 2: c+=c; }")).getProcedure("main").getBegin();
			boolean pos=false,neg=false;
			State joinpoint = null;
			State splitpoint = null;
			for (Transition trans :switchstart.getOut()){
				assertTrue(trans instanceof GuardedTransition);
				// (a-1) ?=0
				GuardedTransition guard = (GuardedTransition)trans;

				Operator op = guard.getOperator();

				if (op.is(Operator.NEQ)){
					assertTrue(!pos);
					pos=true;

					splitpoint = guard.getDest();
					
				}
				else if (op.is(Operator.EQ)){
					assertTrue(!neg);
					neg=true;
					

					Transition main = directNextTransition(trans);
					// c = c*c
					assertTrue(main instanceof Assignment);
					Assignment a = (Assignment)main;
					assertTrue(a.getRhs() instanceof BinaryExpression);
					assertTrue(((BinaryExpression)a.getRhs()).getOperator().is(Operator.MUL));

					// $1 = c
					main = directNextTransition(main);
					assertTrue(main instanceof Assignment);
					a = (Assignment)main;
					assertTrue(a.getRhs() instanceof Variable);
					
					joinpoint = main.getDest();
					
					// c = c + c
					main = directNextTransition(main);
					assertTrue(main instanceof Assignment);
					a = (Assignment)main;
					assertTrue(a.getRhs() instanceof BinaryExpression);
					assertTrue(((BinaryExpression)a.getRhs()).getOperator().is(Operator.DIV));

					// $2 = c
					main = directNextTransition(main);
					assertTrue(main instanceof Assignment);
					a = (Assignment)main;
					assertTrue(a.getRhs() instanceof Variable);

					// skip
					main = directNextTransition(main);
					assertTrue(main instanceof Nop);

					assertTrue(isLastTransition(main));
					
					
				}
				else fail("Unexpected BinaryExpression found "+op);
			}
			
			
			
			assertTrue(neg&&pos);
			neg=pos=false;
			
			for (Transition trans :splitpoint.getOut()){
				assertTrue(trans instanceof GuardedTransition);
				// (a-2) ?=0
				GuardedTransition guard = (GuardedTransition)trans;

				Operator op = guard.getOperator();

				if (op.is(Operator.NEQ)){
					assertTrue(!pos);
					pos=true;

					trans=directNextTransition(trans);
					assertTrue(trans instanceof Nop);
					assertTrue(trans.getDest().equals(joinpoint));
				}
				else if (op.is(Operator.EQ)){
					assertTrue(!neg);
					neg=true;
					
					Transition main = directNextTransition(trans);
					// c = c*c
					assertTrue(main instanceof Assignment);
					Assignment a = (Assignment)main;
					assertTrue(a.getRhs() instanceof BinaryExpression);
					assertTrue(((BinaryExpression)a.getRhs()).getOperator().is(Operator.PLUS));

					// $1 = c
					main = directNextTransition(main);
					assertTrue(main instanceof Assignment);
					a = (Assignment)main;
					assertTrue(a.getRhs() instanceof Variable);
					
					assertTrue(main.getDest().isEnd());
				}
			}
			

		} catch (Exception ex){
			fail("Unexpected Exception "+ex);
		}
	}
	
	
}
