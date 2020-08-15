package org.singsurf.singsurf.operators.vectorfields;

import jv.vecmath.PdVector;

public class CubicForm {
	double a,b,c,d;

	public CubicForm(double a, double b, double c, double d) {
		super();
		this.a = a;
		this.b = b;
		this.c = c;
		this.d = d;
	}
	
	public double apply(PdVector u,PdVector v,PdVector w) {
		return a * u.getEntry(0) * v.getEntry(0) * w.getEntry(0)
			 + b * (u.getEntry(0) * v.getEntry(1) * w.getEntry(0) 
					 + u.getEntry(1) * v.getEntry(0) * w.getEntry(0)
			 		+ u.getEntry(0) * v.getEntry(0) * w.getEntry(1)) 
			 + c * (u.getEntry(0) * v.getEntry(1) * w.getEntry(1) 
					 + u.getEntry(1) * v.getEntry(0) * w.getEntry(1)
			 		+ u.getEntry(1) * v.getEntry(1) * w.getEntry(0)) 
			 + d * u.getEntry(1) * v.getEntry(1) * w.getEntry(1);
	}

	public QuadraticForm apply(PdVector u) {
		return new QuadraticForm(
				a * u.getEntry(0) + b * u.getEntry(1),
				b * u.getEntry(0) + c * u.getEntry(1),
				c * u.getEntry(0) + d * u.getEntry(1));
	}
	
}
