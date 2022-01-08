package org.singsurf.singsurf.test;

import static org.junit.Assert.*;

import org.junit.Test;
import org.singsurf.singsurf.calculators.Calculator;
import org.singsurf.singsurf.calculators.ProductCalculator;
import org.singsurf.singsurf.calculators.RidgeCalculator;
import org.singsurf.singsurf.definitions.DefType;
import org.singsurf.singsurf.definitions.DefVariable;
import org.singsurf.singsurf.definitions.Definition;

public class CalculatorSetup {

	@Test
	public void testProductCalculator() {
		Definition curve = new Definition("Curve",DefType.pcurve,"t");
		curve.add(new DefVariable("t",DefType.localVar,-1,1,10));
		Calculator c1 = new Calculator(curve,0);
		
		Definition def = new Definition("Sqrt",DefType.genExtrude,"diff(sqrt(t x),x)");
		def.add(new DefVariable("t",DefType.localVar,-1,1,10));
		def.add(new DefVariable("S",DefType.pcurve,-1,1,10));
		def.add(new DefVariable("x",DefType.ingrVar,-1,1,10));
		def.setOpType(DefType.pcurve);
		ProductCalculator pc = new ProductCalculator(def, 1);
		pc.setIngredient(c1);
		pc.build();
		assertTrue(pc.getMsg(),pc.isGood());
	}

	@Test
	public void testRidgeCalculator() {
		Definition curve = new Definition("Curve",DefType.pcurve,"t");
		curve.add(new DefVariable("t",DefType.localVar,-1,1,10));
		Calculator c1 = new Calculator(curve,0);
		
		Definition def = new Definition("Sqrt",DefType.genExtrude,"diff(sqrt(t x),x)");
		def.add(new DefVariable("t",DefType.localVar,-1,1,10));
		def.add(new DefVariable("S",DefType.pcurve,-1,1,10));
		def.add(new DefVariable("x",DefType.ingrVar,-1,1,10));
		def.setOpType(DefType.pcurve);
		RidgeCalculator pc = new RidgeCalculator(def, 1);
		pc.setIngredient1(c1);
		pc.build();
		assertTrue(pc.getMsg(),pc.isGood());
		fail("Expect this to fail as constructor is wrong, need better mock");
		
	}

}
