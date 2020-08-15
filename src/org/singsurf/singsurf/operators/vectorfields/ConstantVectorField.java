package org.singsurf.singsurf.operators.vectorfields;

import org.singsurf.singsurf.jepwrapper.EvaluationException;

import jv.vecmath.PdVector;

public class ConstantVectorField extends AbstractVectorField {
	public PdVector dir;
	
	public ConstantVectorField(PdVector dir) {
		super();
		this.dir = dir;
	}

	@Override
	public PdVector calcVector(PdVector vec) throws EvaluationException {
		return dir;
	}

}
