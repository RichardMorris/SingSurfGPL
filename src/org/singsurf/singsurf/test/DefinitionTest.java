/*
Created 23-Apr-2006 - Richard Morris
*/
package org.singsurf.singsurf.test;

import org.junit.Test;
import org.singsurf.singsurf.definitions.Definition;
import org.singsurf.singsurf.calculators.Calculator;
import org.singsurf.singsurf.calculators.Evaluator;
import org.singsurf.singsurf.definitions.DefVariable;
import org.singsurf.singsurf.definitions.DefinitionReader;

import junit.framework.TestCase;

public class DefinitionTest extends TestCase {

	public DefinitionTest(String name) {
		super(name);
	}

	@Test
	public void testDefs() throws Exception
	{
		Definition def = new Definition("test","psurf","(x,y,A x^2,B y^2);");
		System.out.println(def.toString());
		def.setParamNames(new String[]{"A","B"});
		def.getParam(0).setVal(1.1);
		def.getParam(1).setVal(2.2);
		System.out.println(def.toString());
		def.setParamNames(new String[]{"A","C"});
		System.out.println(def.toString());
		def.setParamNames(new String[]{"B","C"});
		System.out.println(def.toString());
	}
	
	@Test
	public void testCalc() throws Exception
	{
		Definition def = new Definition("test","psurf","[x,y,A x+B y];");
		def.setVariable(0,new DefVariable("x","Normal"));
		def.setVariable(1,new DefVariable("y","Normal"));
		System.out.println(def.toString());
		Calculator calc = new Calculator(def,0);
		calc.build();
		System.out.println(def.toString());
		calc.setParamValue("A",1.1);
		calc.setParamValue("B",2.2);
		calc.setVarBounds(0,3,4,5);
		System.out.println(def.toString());
		//calc.setVarValue(0,5);
		//calc.setVarValue(1,3);
		Evaluator eval = calc.createEvaluator();
		double res[] = eval.evalTop(new double[]{5,3});
		System.out.println("res ["+res[0]+","+res[1]+","+res[2]+"]");
	}

	@Test
	public void testSphere() throws Exception
	{
		Definition def = DefinitionReader.createLsmpDef(
		"<definition name=\"Sphere\" type=\"psurf\">\n"+
		"[x0,y0,z0]+ r * [l,m,n];\n"+
		"l = cos(pi th) cos(pi phi);\n"+
		"m = cos(pi th) sin(pi phi);\n"+
		"n = sin(pi th);\n"+
		"th = x; phi = y;\n"+
		"<parameter name=\"r\" value=\"1.\">\n"+
		"<variable name=\"x\" min=\"-0.5\" max=\"0.5\">\n"+
		"<variable name=\"y\" min=\"-1\" max=\"1\">\n"+
		"</definition>");

		def.setVariable(0,new DefVariable("x","Normal"));
		def.setVariable(1,new DefVariable("y","Normal"));
		System.out.println(def.toString());
		Calculator calc = new Calculator(def,1);
		calc.build();
	}
}
