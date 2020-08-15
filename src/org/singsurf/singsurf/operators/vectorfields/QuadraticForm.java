package org.singsurf.singsurf.operators.vectorfields;

import jv.vecmath.PdVector;

public class QuadraticForm {
	double a,b,c;
	
	public QuadraticForm(double a, double b, double c) {
		super();
		this.a = a;
		this.b = b;
		this.c = c;
	}

	public double apply(PdVector u,PdVector v) {
		return a * u.getEntry(0) * v.getEntry(0)
			 + b * (u.getEntry(0) * v.getEntry(1) + u.getEntry(1) * v.getEntry(0))
			 + c * u.getEntry(1) * v.getEntry(1);
	}
	
	LinearForm apply(PdVector u) {
		return new LinearForm(a* u.getEntry(0) + b*u.getEntry(1),
				b*u.getEntry(0) + c*u.getEntry(1) );
	}

	public PdVector orthogonal(PdVector p) {
		LinearForm lin = apply(p);
		PdVector q = lin.conj();

		return q;
	}
	
	
}
