package org.singsurf.singsurf.operators.vectorfields;

import org.singsurf.singsurf.calculators.Evaluator;

import org.singsurf.singsurf.jepwrapper.EvaluationException;

import jv.vecmath.PdVector;

public class SimpleCalcField extends AbstractVectorField {

	Evaluator calc;
	
	public SimpleCalcField(Evaluator calc) {
		this.calc = calc;
	}

	
	@Override
	public PdVector calcVector(PdVector vert) throws EvaluationException {
		double topRes[] = calc.evalTop(vert.getEntries());
		
		for(double val:topRes) {
			if(!Double.isFinite(val)) {
				return new PdVector(topRes.length);
			}
		}
		PdVector out = new PdVector(topRes);
		out.multScalar(length);

		return out;
	}

}
