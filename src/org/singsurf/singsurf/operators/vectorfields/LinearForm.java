package org.singsurf.singsurf.operators.vectorfields;

import jv.vecmath.PdVector;

public class LinearForm {
	double a,b;
	
	public LinearForm(double a, double b) {
		super();
		this.a = a;
		this.b = b;
	}

	double apply(PdVector vec) {
		return a * vec.getEntry(0) + b * vec.getEntry(1);
	}

	public PdVector conj() {
		return new PdVector(-b,a);
	}
	
}
