/*
Created 29 Nov 2006 - Richard Morris
*/
package org.singsurf.singsurf.calculators;

import org.singsurf.singsurf.definitions.Definition;

public class PolynomialCalculator extends Calculator {

	public PolynomialCalculator(Definition def, int nderiv) {
		super(def, nderiv);
	}

	@Override
	public void setParamValue(String name, double val) {
		super.setParamValue(name, val);
		
//		mj.setVariable(name, val);
	}

}
