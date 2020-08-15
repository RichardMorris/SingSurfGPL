package org.singsurf.singsurf.operators.vectorfields;

import org.singsurf.singsurf.jepwrapper.EvaluationException;

import jv.vecmath.PdVector;

public interface MultipleVectorField {

	PdVector[] calcVectors(PdVector vert) throws EvaluationException;

}
