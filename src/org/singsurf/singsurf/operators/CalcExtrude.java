package org.singsurf.singsurf.operators;

import org.singsurf.singsurf.calculators.Evaluator;
import org.singsurf.singsurf.definitions.DefVariable;

import org.singsurf.singsurf.jepwrapper.EvaluationException;

import jv.vecmath.PdVector;

public class CalcExtrude extends AbstractExtrude {
	Evaluator calc;
	public CalcExtrude(Evaluator calc, DefVariable varY) {
		super(varY);
		this.calc = calc;
	}

	@Override
	PdVector extrude(PdVector vec, double y) throws EvaluationException {
		double in[] = new double[] {y,vec.getEntry(0),vec.getEntry(1),vec.getEntry(2)};
		double[] res = calc.evalTop(in);
		return new PdVector(res);
	}

}
