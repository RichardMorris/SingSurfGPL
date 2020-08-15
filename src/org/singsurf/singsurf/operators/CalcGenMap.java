/*
Created 25-Apr-2006 - Richard Morris
*/
package org.singsurf.singsurf.operators;

import org.singsurf.singsurf.calculators.Evaluator;

import org.singsurf.singsurf.jepwrapper.EvaluationException;

import jv.vecmath.PdVector;

/**
 * @author Richard Morris
 *
 */
public class CalcGenMap extends SimpleCalcMap {
	
	public CalcGenMap(Evaluator calc,boolean doNorms) {
	    super(calc,doNorms);
	}
	/* (non-Javadoc)
	 * @see org.singsurf.singsurf.operators.SimpleMap#map(jv.vecmath.PdVector)
	 */
	@Override
	public PdVector map(PdVector vec) throws EvaluationException {
		double topRes[] = calc.evalTop(vec.getEntries());
		PdVector out = new PdVector(topRes);
		return out;
	}
	@Override
	public PdVector[] map(PdVector vert, PdVector norm) throws EvaluationException {
		double topRes[] = calc.evalTop(vert.getEntries());
		PdVector out = new PdVector(topRes);
		double mat[][] = new double[2][];
		mat[0] = calc.evalDerivative(0);
		mat[1] = calc.evalDerivative(1);

		PdVector u = new PdVector(mat[0]);
		PdVector v = new PdVector(mat[1]);
		PdVector norm3 = PdVector.crossNew(u, v);
		norm3.normalize();
	
		System.out.print("pt"+vert.toShortString());
//		System.out.println(norm2.toShortString() + " = "+norm3.toShortString());
//		double det = dx[0] * dy[1] * dz[2] 
//			+ dx[1] * dy[2] * dz[0]
//			+ dx[2] * dy[0] * dz[1]
//			- dx[0] * dy[2] * dz[1] 
//			- dx[1] * dy[0] * dz[2]
//			- dx[2] * dy[1] * dz[0];
//
//		u.
//		double normRes[] = new double[] {
//			det * dx[0] 		
		
	    return new PdVector[] { out, norm3 };
	}
	
	

}
