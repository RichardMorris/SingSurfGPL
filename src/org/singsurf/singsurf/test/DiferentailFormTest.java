package org.singsurf.singsurf.test;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.lsmp.djep.djep.DJep;
import org.nfunk.jep.Node;
import org.singsurf.singsurf.jep.DiffForm;
import org.singsurf.singsurf.jepwrapper.JepException;


public class DiferentailFormTest {
	DJep jep;
	@Before
	public void setUp() throws Exception {
		jep = new DJep();
		jep.addStandardDiffRules();
		jep.addFunction("Diff", new DiffForm());
		jep.getOperatorSet().getAssign().setPFMC(new XAssign());
//		jep.reinitializeComponents();
	}


	@Test
	public void test2D() throws JepException {
		Node n1 = jep.parse("S = [x,y,A x^2+B y^2+ a x^3 + b x^2 y + c x y^2 + d y^3]");
		Node p1 = jep.preprocess(n1);
		Node n2 = jep.parse("dS = Diff(S,x,y)");
		Node p2 = jep.preprocess(n2);
		Node rhs2 = p2.jjtGetChild(1);
		assertEquals(jep.getOperatorSet().getList(),rhs2.getOperator());
		assertEquals(2,rhs2.jjtGetNumChildren());
		assertEquals(((DVariable )jep.getVariable("S")).getDerivative("x"),rhs2.jjtGetChild(0).getVar());
		assertEquals(((DVariable )jep.getVariable("S")).getDerivative("y"),rhs2.jjtGetChild(1).getVar());
		
		Node n3 = jep.parse("ddS = Diff(dS,x,y)");
		Node p3 = jep.preprocess(n3);
		Node rhs3 = p3.jjtGetChild(1);
		assertEquals(jep.getOperatorSet().getList(),rhs3.getOperator());
		assertEquals(3,rhs3.jjtGetNumChildren());
		assertEquals(((DVariable )jep.getVariable("S")).getDerivative("x","x"),rhs3.jjtGetChild(0).getVar());
		assertEquals(((DVariable )jep.getVariable("S")).getDerivative("x","y"),rhs3.jjtGetChild(1).getVar());
		assertEquals(((DVariable )jep.getVariable("S")).getDerivative("y","y"),rhs3.jjtGetChild(2).getVar());

		Node n4 = jep.parse("dddS = Diff(ddS,x,y)");
		Node p4 = jep.preprocess(n4);
		Node rhs4 = p4.jjtGetChild(1);
		assertEquals(jep.getOperatorSet().getList(),rhs4.getOperator());
		assertEquals(4,rhs4.jjtGetNumChildren());
		assertEquals(((DVariable )jep.getVariable("S")).getDerivative("x","x","x"),rhs4.jjtGetChild(0).getVar());
		assertEquals(((DVariable )jep.getVariable("S")).getDerivative("x","x","y"),rhs4.jjtGetChild(1).getVar());
		assertEquals(((DVariable )jep.getVariable("S")).getDerivative("x","y","y"),rhs4.jjtGetChild(2).getVar());
		assertEquals(((DVariable )jep.getVariable("S")).getDerivative("y","y","y"),rhs4.jjtGetChild(3).getVar());
	}

	@Test
	public void test3D() throws JepException {
		Node n1 = jep.parse("S = A x^3 + B y^3 + C z^3");
		Node p1 = jep.preprocess(n1);
		Node n2 = jep.parse("dS = Diff(S,x,y,z)");
		Node p2 = jep.preprocess(n2);
		Node rhs2 = p2.jjtGetChild(1);
		assertEquals(jep.getOperatorSet().getList(),rhs2.getOperator());
		assertEquals(3,rhs2.jjtGetNumChildren());
		assertEquals(((DVariable )jep.getVariable("S")).getDerivative("x"),rhs2.jjtGetChild(0).getVar());
		assertEquals(((DVariable )jep.getVariable("S")).getDerivative("y"),rhs2.jjtGetChild(1).getVar());
		assertEquals(((DVariable )jep.getVariable("S")).getDerivative("z"),rhs2.jjtGetChild(2).getVar());
		
		Node n3 = jep.parse("ddS = Diff(dS,x,y,z)");
		Node p3 = jep.preprocess(n3);
		Node rhs3 = p3.jjtGetChild(1);
		assertEquals(jep.getOperatorSet().getList(),rhs3.getOperator());
		assertEquals(6,rhs3.jjtGetNumChildren());
		assertEquals(((DVariable )jep.getVariable("S")).getDerivative("x","x"),rhs3.jjtGetChild(0).getVar());
		assertEquals(((DVariable )jep.getVariable("S")).getDerivative("x","y"),rhs3.jjtGetChild(1).getVar());
		assertEquals(((DVariable )jep.getVariable("S")).getDerivative("x","z"),rhs3.jjtGetChild(2).getVar());
		assertEquals(((DVariable )jep.getVariable("S")).getDerivative("y","y"),rhs3.jjtGetChild(3).getVar());
		assertEquals(((DVariable )jep.getVariable("S")).getDerivative("y","z"),rhs3.jjtGetChild(4).getVar());
		assertEquals(((DVariable )jep.getVariable("S")).getDerivative("z","z"),rhs3.jjtGetChild(5).getVar());

		Node n4 = jep.parse("dddS = Diff(ddS,x,y,z)");
		Node p4 = jep.preprocess(n4);
		Node rhs4 = p4.jjtGetChild(1);
		assertEquals(jep.getOperatorSet().getList(),rhs4.getOperator());
		assertEquals(10,rhs4.jjtGetNumChildren());
		assertEquals(((DVariable )jep.getVariable("S")).getDerivative("x","x","x"),rhs4.jjtGetChild(0).getVar());
		assertEquals(((DVariable )jep.getVariable("S")).getDerivative("x","x","y"),rhs4.jjtGetChild(1).getVar());
		assertEquals(((DVariable )jep.getVariable("S")).getDerivative("x","x","z"),rhs4.jjtGetChild(2).getVar());
		assertEquals(((DVariable )jep.getVariable("S")).getDerivative("x","y","y"),rhs4.jjtGetChild(3).getVar());
		assertEquals(((DVariable )jep.getVariable("S")).getDerivative("x","y","z"),rhs4.jjtGetChild(4).getVar());
		assertEquals(((DVariable )jep.getVariable("S")).getDerivative("x","z","z"),rhs4.jjtGetChild(5).getVar());
		assertEquals(((DVariable )jep.getVariable("S")).getDerivative("y","y","y"),rhs4.jjtGetChild(6).getVar());
		assertEquals(((DVariable )jep.getVariable("S")).getDerivative("y","y","z"),rhs4.jjtGetChild(7).getVar());
		assertEquals(((DVariable )jep.getVariable("S")).getDerivative("y","z","z"),rhs4.jjtGetChild(8).getVar());
		assertEquals(((DVariable )jep.getVariable("S")).getDerivative("z","z","z"),rhs4.jjtGetChild(9).getVar());
	}

}
