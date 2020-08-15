package org.singsurf.singsurf.operators.vectorfields;

import org.singsurf.singsurf.jepwrapper.EvaluationException;

import jv.vecmath.PdVector;

public class SelectVectorField extends AbstractVectorField {
	final MultipleVectorField mvf;
	final int num;
	
	public SelectVectorField(MultipleVectorField mvf, int num) {
		super();
		this.mvf = mvf;
		this.num = num;
	}

	@Override
	public PdVector calcVector(PdVector pt) throws EvaluationException {
		PdVector[] vects = mvf.calcVectors(pt);
		return vects[num];
	}

}
