package org.singsurf.singsurf.operators;

import org.singsurf.singsurf.jepwrapper.EvaluationException;

import jv.vecmath.PdVector;

public class SphereIntersectionClip extends IntersectionClip {

	double radSq;
	public SphereIntersectionClip(int nItterations,double radius) {
		super(nItterations);
		radSq = radius*radius;
	}

	@Override
	public double findValue(PdVector c) throws EvaluationException {
		return c.sqrLength() - radSq;
	}

	@Override
	public boolean testClip(PdVector vec) throws EvaluationException {
		return (vec.sqrLength() <= radSq) ;
	}

}
