package org.singsurf.singsurf.operators;

import org.singsurf.singsurf.calculators.Evaluator;

import org.singsurf.singsurf.jepwrapper.EvaluationException;

import jv.vecmath.PdVector;

public class CalcClip extends IntersectionClip {

    
	Evaluator calc;
    boolean invert=false;
    public CalcClip(Evaluator calc,int nItt, boolean invert) {
        super(nItt);
        this.calc = calc;
        this.invert = invert;
    }

    @Override
    public boolean testClip(PdVector vec) throws EvaluationException {
        double[] v = calc.evalTop(vec.getEntries());
        return invert ? v[0] <= 0 : v[0] >= 0;
    }

    @Override
    public double findValue(PdVector vec) throws EvaluationException {
        double[] v = calc.evalTop(vec.getEntries());
        return v[0];
    }


	public void setInvert(boolean state) {
		invert = state;
	}

}
