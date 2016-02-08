package petter.simplec;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

import org.junit.Test;


import petter.cfg.CompilationUnit;
import petter.cfg.Procedure;
import petter.cfg.edges.Assignment;
import petter.cfg.edges.Transition;
import petter.cfg.expression.BinaryExpression;
import petter.cfg.expression.Expression;
import petter.cfg.expression.IntegerConstant;
import petter.cfg.expression.FunctionCall;
import petter.cfg.expression.Operator;
import petter.cfg.expression.UnaryExpression;
import petter.cfg.expression.Variable;
import petter.utils.AnnotatingSymbolFactory;

/**
 * You find the translation scheme of C to the IR here in each test
 * @author petter
 *
 */

public class ExpressionEvaluation {

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
	private static Transition directNextTransition(Transition now){
		return now.getDest().getOut().iterator().next();
	}
	private static boolean isLastTransition(Transition now){
		return now.getDest().isEnd();
	}

	// a = main();
	@Test
	public void functionCall() {
		try {
			//$1 = main() 
			Transition transition = extractMainTransition(compile(fromOneExpressionOnly("a = main();")));
			assertTrue(transition instanceof Assignment);
			Assignment a = (Assignment)transition;
			assertTrue(a.getLhs() instanceof Variable);
			assertTrue(a.getRhs() instanceof FunctionCall);
			Variable temp = (Variable)a.getLhs();

			//a = $1
			transition = directNextTransition(transition);
			assertTrue(transition instanceof Assignment);
			a = (Assignment)transition;
			assertTrue(a.getLhs() instanceof Variable);
			assertTrue(a.getRhs() instanceof Variable);

			assertEquals(temp, a.getRhs()); // check for the correct flow of the expressions value 
			
			//$2 = a
			transition = directNextTransition(transition);
			assertTrue(transition instanceof Assignment);
			a = (Assignment)transition;
			assertTrue(a.getLhs() instanceof Variable);
			assertTrue(a.getRhs() instanceof Variable);

			assertTrue(isLastTransition(transition));
			
		}catch (Exception ex){
			fail("unexpected Exception "+ex);
		}
	}	
	
	// main(b++,c++);
	@Test
	public void parameterPassing() {
		try {
			//$1 = b 
			Transition transition = extractMainTransition(compile(fromOneExpressionOnly("main(b++,c++);")));
			assertTrue(transition instanceof Assignment);
			Assignment a = (Assignment)transition;
			assertTrue(a.getLhs() instanceof Variable);
			assertTrue(a.getRhs() instanceof Variable);
			Variable temp1 = (Variable)a.getLhs();

			// b = b+1
			transition = directNextTransition(transition);
			assertTrue(transition instanceof Assignment);
			a = (Assignment)transition;
			assertTrue(a.getLhs() instanceof Variable);
			assertTrue(a.getRhs() instanceof BinaryExpression);
			
			//$2 = c
			transition = directNextTransition(transition);
			assertTrue(transition instanceof Assignment);
			a = (Assignment)transition;
			assertTrue(a.getLhs() instanceof Variable);
			assertTrue(a.getRhs() instanceof Variable);
			Variable temp2 = (Variable)a.getLhs();

			// c = c+1
			transition = directNextTransition(transition);
			assertTrue(transition instanceof Assignment);
			a = (Assignment)transition;
			assertTrue(a.getLhs() instanceof Variable);
			assertTrue(a.getRhs() instanceof BinaryExpression);

			//$3 = main($1,$2)
			
			transition = directNextTransition(transition);
			assertTrue(transition instanceof Assignment);
			a = (Assignment)transition;
			assertTrue(a.getLhs() instanceof Variable);
			assertTrue(a.getRhs() instanceof FunctionCall);
			FunctionCall callex = (FunctionCall)a.getRhs();
			Iterator<Expression> it = callex.getParamsUnchanged().iterator();
			
			// check for the correct flow of the expressions value
			assertEquals(temp1, it.next());  
			assertEquals(temp2, it.next());
			assertFalse(it.hasNext());
			
			assertTrue(isLastTransition(transition));
			
		}catch (Exception ex){
			fail("unexpected Exception "+ex);
		}
	}	
	
	// a = ++c;
	@Test
	public void preIncrement() {
		try {
			//c = c + 1
			Transition transition = extractMainTransition(compile(fromOneExpressionOnly("a = ++c;")));
			assertTrue(transition instanceof Assignment);
			Assignment a = (Assignment)transition;
			assertTrue(a.getLhs() instanceof Variable);
			assertTrue(a.getRhs() instanceof BinaryExpression);
			BinaryExpression be = (BinaryExpression)a.getRhs();
			assertTrue(be.getLeft() instanceof Variable);
			assertTrue(be.getOperator().is(Operator.PLUS));
			assertTrue(be.getRight() instanceof IntegerConstant);

			//$1 = c
			transition = directNextTransition(transition);
			assertTrue(transition instanceof Assignment);
			a = (Assignment)transition;
			assertTrue(a.getLhs() instanceof Variable);
			assertTrue(a.getRhs() instanceof Variable);
			Variable temp = (Variable)a.getLhs();
			
			// a = $1
			transition = directNextTransition(transition);
			assertTrue(transition instanceof Assignment);
			a = (Assignment)transition;
			assertTrue(a.getLhs() instanceof Variable);
			assertTrue(a.getRhs() instanceof Variable);

			assertEquals(temp, a.getRhs()); // check for the correct flow of the expressions value 

			// $2 = a
			transition = directNextTransition(transition);
			assertTrue(transition instanceof Assignment);
			a = (Assignment)transition;
			assertTrue(a.getLhs() instanceof Variable);
			assertTrue(a.getRhs() instanceof Variable);			
			assertTrue(isLastTransition(transition));
			
		}catch (Exception ex){
			fail("unexpected Exception "+ex);
		}
	}	

	// a = c++;
	@Test
	public void postIncrement() {
		try {
			
			// $1 = c
			
			Transition transition = extractMainTransition(compile(fromOneExpressionOnly("a=c++;")));
			assertTrue(transition instanceof Assignment);
			Assignment a = (Assignment)transition;
			assertTrue(a.getLhs() instanceof Variable);
			assertTrue(a.getRhs() instanceof Variable);
			Variable temp = (Variable)a.getLhs();
			
			// c = c+1
			transition = directNextTransition(transition);
			assertTrue(transition instanceof Assignment);
			a = (Assignment)transition;
			assertTrue(a.getLhs() instanceof Variable);
			assertTrue(a.getRhs() instanceof BinaryExpression);
			BinaryExpression be = (BinaryExpression)a.getRhs();
			assertTrue(be.getLeft() instanceof Variable);
			assertTrue(be.getOperator().is(Operator.PLUS));
			assertTrue(be.getRight() instanceof IntegerConstant);

			// a = $1
			transition = directNextTransition(transition);
			assertTrue(transition instanceof Assignment);
			a = (Assignment)transition;
			assertTrue(a.getLhs() instanceof Variable);
			assertTrue(a.getRhs() instanceof Variable);

			assertEquals(temp, a.getRhs()); // check for the correct flow of the expressions value 

			
			// $2 = a
			transition = directNextTransition(transition);
			assertTrue(transition instanceof Assignment);
			a = (Assignment)transition;
			assertTrue(a.getLhs() instanceof Variable);
			assertTrue(a.getRhs() instanceof Variable);

			assertTrue(isLastTransition(transition));
			
		}catch (Exception ex){
			fail("unexpected Exception "+ex);
		}
	}
	
	// a = c+=1;
	@Test
	public void assignIncrement() {
		try {
			
			// c = c +1
			
			Transition transition = extractMainTransition(compile(fromOneExpressionOnly("a=c+=1;")));
			assertTrue(transition instanceof Assignment);
			Assignment a = (Assignment)transition;
			assertTrue(a.getLhs() instanceof Variable);
			assertTrue(a.getRhs() instanceof BinaryExpression);
			BinaryExpression be = (BinaryExpression)a.getRhs();
			assertTrue(be.getLeft() instanceof Variable);
			assertTrue(be.getOperator().is(Operator.PLUS));
			assertTrue(be.getRight() instanceof IntegerConstant);
			
			// $1 = c
			transition = directNextTransition(transition);
			assertTrue(transition instanceof Assignment);
			a = (Assignment)transition;
			assertTrue(a.getLhs() instanceof Variable);
			assertTrue(a.getRhs() instanceof Variable);
			Variable temp = (Variable)a.getLhs();
			
			// a = $1
			transition = directNextTransition(transition);
			assertTrue(transition instanceof Assignment);
			a = (Assignment)transition;
			assertTrue(a.getLhs() instanceof Variable);
			assertTrue(a.getRhs() instanceof Variable);

			assertEquals(temp, a.getRhs()); // check for the correct flow of the expressions value 

			
			// $2 = a
			transition = directNextTransition(transition);
			assertTrue(transition instanceof Assignment);
			a = (Assignment)transition;
			assertTrue(a.getLhs() instanceof Variable);
			assertTrue(a.getRhs() instanceof Variable);

			assertTrue(isLastTransition(transition));
			
		}catch (Exception ex){
			fail("unexpected Exception "+ex);
		}
	}	
	
	// c = 5;
	@Test
	public void assignment() {
		try {
			Transition main = extractMainTransition(compile(fromOneExpressionOnly("c = 5;")));
			assertTrue(main instanceof Assignment);
			Assignment a = (Assignment)main;
			assertTrue(a.getLhs() instanceof Variable);
			assertTrue(a.getRhs() instanceof IntegerConstant);
			
			main = directNextTransition(main);
			assertTrue(main instanceof Assignment);
			a = (Assignment)main;
			assertTrue(a.getLhs() instanceof Variable);
			assertTrue(a.getRhs() instanceof Variable);

			assertTrue(isLastTransition(main));			
		}catch (Exception ex){
			fail("unexpected Exception "+ex);
		}
	}

	// c = &a;
	@Test
	public void addrofRHS() {
		try {
			Transition main = extractMainTransition(compile(fromOneExpressionOnly("c = &a;")));
			assertTrue(main instanceof Assignment);
			Assignment a = (Assignment)main;
			assertTrue(a.getLhs() instanceof Variable);
			assertTrue(a.getRhs() instanceof UnaryExpression);
			assertTrue( ((UnaryExpression)a.getRhs()).getOperator().is(Operator.ADDRESSOF) );
			
			
			main = directNextTransition(main);
			assertTrue(main instanceof Assignment);
			a = (Assignment)main;
			assertTrue(a.getLhs() instanceof Variable);
			assertTrue(a.getRhs() instanceof Variable);

			assertTrue(isLastTransition(main));			
		}catch (Exception ex){
			fail("unexpected Exception "+ex);
		}
	}

	// c = *a;
	@Test
	public void derefRHS() {
		try {
			Transition main = extractMainTransition(compile(fromOneExpressionOnly("c = *p;")));
			assertTrue(main instanceof Assignment);
			Assignment a = (Assignment)main;
			assertTrue(a.getLhs() instanceof Variable);
			assertTrue(a.getRhs() instanceof UnaryExpression);
			assertTrue( ((UnaryExpression)a.getRhs()).getOperator().is(Operator.DEREF) );
			
			
			main = directNextTransition(main);
			assertTrue(main instanceof Assignment);
			a = (Assignment)main;
			assertTrue(a.getLhs() instanceof Variable);
			assertTrue(a.getRhs() instanceof Variable);

			assertTrue(isLastTransition(main));			
		}catch (Exception ex){
			fail("unexpected Exception "+ex);
		}
	}

	// *c = a;
	@Test
	public void derefLHS() {
		try {
			Transition main = extractMainTransition(compile(fromOneExpressionOnly("*p = a;")));
			assertTrue(main instanceof Assignment);
			Assignment a = (Assignment)main;
			assertTrue(a.getRhs() instanceof Variable);
			assertTrue(a.getLhs() instanceof UnaryExpression);
			assertTrue( ((UnaryExpression)a.getLhs()).getOperator().is(Operator.DEREF) );
			
			
			main = directNextTransition(main);
			assertTrue(main instanceof Assignment);
			a = (Assignment)main;
			assertTrue(a.getLhs() instanceof Variable);
			assertTrue(a.getRhs() instanceof UnaryExpression);

			assertTrue(isLastTransition(main));			
		}catch (Exception ex){
			fail("unexpected Exception "+ex);
		}
	}

	// p[a--] = a++;
	@Test
	public void arrayAccLHS() {
		try {
			Transition transition = extractMainTransition(compile(fromOneExpressionOnly("p[a--] = a++;")));

			// $1 = a
			assertTrue(transition instanceof Assignment);
			Assignment a = (Assignment)transition;
			assertTrue(a.getLhs() instanceof Variable);
			assertTrue(a.getRhs() instanceof Variable);
			Variable temp1 = (Variable)a.getLhs();
			
			// a = a-1
			transition = directNextTransition(transition);
			assertTrue(transition instanceof Assignment);
			a = (Assignment)transition;
			assertTrue(a.getLhs() instanceof Variable);
			assertTrue(a.getRhs() instanceof BinaryExpression);
			BinaryExpression be = (BinaryExpression)a.getRhs();
			assertTrue(be.getLeft() instanceof Variable);
			assertTrue(be.getOperator().is(Operator.MINUS));
			assertTrue(be.getRight() instanceof IntegerConstant);
			
			// $2 = b
			transition = directNextTransition(transition);
			assertTrue(transition instanceof Assignment);
			a = (Assignment)transition;
			assertTrue(a.getLhs() instanceof Variable);
			assertTrue(a.getRhs() instanceof Variable);
			Variable temp2 = (Variable)a.getLhs();
			
			// b = b+1
			transition = directNextTransition(transition);
			assertTrue(transition instanceof Assignment);
			a = (Assignment)transition;
			assertTrue(a.getLhs() instanceof Variable);
			assertTrue(a.getRhs() instanceof BinaryExpression);
			be = (BinaryExpression)a.getRhs();
			assertTrue(be.getLeft() instanceof Variable);
			assertTrue(be.getOperator().is(Operator.PLUS));
			assertTrue(be.getRight() instanceof IntegerConstant);
			
			// p[$1] = $2
			transition = directNextTransition(transition);
			assertTrue(transition instanceof Assignment);
			a = (Assignment)transition;
			assertTrue(a.getLhs() instanceof BinaryExpression);
			be = (BinaryExpression)a.getLhs();
			assertTrue(be.getLeft() instanceof Variable);
			assertTrue(be.getOperator().is(Operator.ARRAY));
			assertTrue(be.getRight() instanceof Variable);			
			assertTrue(a.getRhs() instanceof Variable);

			assertEquals(temp2, a.getRhs()); // check for the correct flow of the expressions value 
			assertEquals(temp1, be.getRight()); // check for the correct flow of the expressions value 

			
			// $3 = p[$1]
			transition = directNextTransition(transition);
			assertTrue(transition instanceof Assignment);
			a = (Assignment)transition;
			assertTrue(a.getLhs() instanceof Variable);
			assertTrue(a.getRhs() instanceof BinaryExpression);
			assertTrue(((BinaryExpression)a.getRhs()).getLeft() instanceof Variable );

			assertTrue(isLastTransition(transition));			


		}catch (Exception ex){
			fail("unexpected Exception "+ex);
		}
	}


	// a = p[a--];
	@Test
	public void arrayAccRHS() {
		try {
			Transition transition = extractMainTransition(compile(fromOneExpressionOnly("a= p[a--];")));

			// $1 = a
			assertTrue(transition instanceof Assignment);
			Assignment a = (Assignment)transition;
			assertTrue(a.getLhs() instanceof Variable);
			assertTrue(a.getRhs() instanceof Variable);
			Variable temp1 = (Variable)a.getLhs();
			
			// a = a-1
			transition = directNextTransition(transition);
			assertTrue(transition instanceof Assignment);
			a = (Assignment)transition;
			assertTrue(a.getLhs() instanceof Variable);
			assertTrue(a.getRhs() instanceof BinaryExpression);
			BinaryExpression be = (BinaryExpression)a.getRhs();
			assertTrue(be.getLeft() instanceof Variable);
			assertTrue(be.getOperator().is(Operator.MINUS));
			assertTrue(be.getRight() instanceof IntegerConstant);
			
			// a = p[$1]
			transition = directNextTransition(transition);
			assertTrue(transition instanceof Assignment);
			a = (Assignment)transition;
			assertTrue(a.getLhs() instanceof Variable);

			assertTrue(a.getRhs() instanceof BinaryExpression);
			be = (BinaryExpression)a.getRhs();
			assertTrue(be.getLeft() instanceof Variable);
			assertTrue(be.getOperator().is(Operator.ARRAY));
			assertTrue(be.getRight() instanceof Variable);			
			
			assertEquals(temp1, be.getRight()); // check for the correct flow of the expressions value 
			
			// $3 = a
			transition = directNextTransition(transition);
			assertTrue(transition instanceof Assignment);
			a = (Assignment)transition;
			assertTrue(a.getLhs() instanceof Variable);
			assertTrue(a.getRhs() instanceof Variable);

			assertTrue(isLastTransition(transition));			


		}catch (Exception ex){
			fail("unexpected Exception "+ex);
		}
	}
}
