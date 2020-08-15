/*
Created 27-Apr-2006 - Richard Morris
*/
package org.singsurf.singsurf.operators;

import org.singsurf.singsurf.calculators.Evaluator;

import org.singsurf.singsurf.jepwrapper.EvaluationException;

import jv.vecmath.PdVector;

public class SimpleCalcIntersection extends SimpleIntersect {
	Evaluator calc;
	public SimpleCalcIntersection(Evaluator calc, int nitts) {
		this.calc = calc;
		this.nItterations = nitts;
	}
	@Override
	public double findValue(PdVector vec) throws EvaluationException {
		try {
            double[] v = calc.evalTop(vec.getEntries());
            return v[0];
        } catch (NullPointerException e) {
            e.printStackTrace();
            throw e;
        }
	}

}
