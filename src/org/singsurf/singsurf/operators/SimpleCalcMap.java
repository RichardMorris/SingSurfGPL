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
public class SimpleCalcMap extends SimpleMap {
	Evaluator calc;

	boolean doNormals;
	
	public SimpleCalcMap(Evaluator calc, boolean doNorms) {
		this.calc = calc;
		this.doNormals = doNorms;
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
		double mat[][] = new double[3][];
		mat[0] = calc.evalDerivative(0);
		mat[1] = calc.evalDerivative(1);
		mat[2] = calc.evalDerivative(2);

		double nx = norm.getEntry(0);
		double ny = norm.getEntry(1);
		double nz = norm.getEntry(2);
/*
		double l0 = Math.abs(nx);
		double l1 = Math.abs(ny);
		double l2 = Math.abs(nz);
		PdVector u,v,a;
		if( l0 <= l1 && l0 <= l2) {
		    a = new PdVector(1,0,0);
		}
		else if(l1 <= l0 && l1 <= l2) {
		    a = new PdVector(0,1,0);
		}
		else {
		    a = new PdVector(0,0,1);
		}
		u = PdVector.crossNew(norm, a);
		v = PdVector.crossNew(norm, u);
				
		PdMatrix Mat = new PdMatrix(mat);
		Mat.transpose();
		u.leftMultMatrix(Mat);
		v.leftMultMatrix(Mat);
		
		PdVector norm2 = PdVector.crossNew(u, v);
		norm2.normalize();
*/		
		// norm_x = | nx ny nz |
		//          |  d  e  f |
		//          |  g  h  i |
		// norm_y = |  a  b  c |
		//          | nx ny nz |
		//          |  g  h  i |
		// norm_z = |  a  b  c |
		//          |  d  e  f |
		//          | nx ny nz |
		double norm_x = nx * (mat[1][1] * mat[2][2] - mat[2][1] * mat[1][2] )
			     +  ny * (mat[2][1] * mat[0][2] - mat[0][1] * mat[2][2] )
			     +  nz * (mat[0][1] * mat[1][2] - mat[1][1] * mat[0][2] );		
		double norm_y = nx * (mat[1][2] * mat[2][0] - mat[2][2] * mat[1][0] )
			     +  ny * (mat[2][2] * mat[0][0] - mat[0][2] * mat[2][0] )
			     +  nz * (mat[0][2] * mat[1][0] - mat[1][2] * mat[0][0] );		
		double norm_z = nx * (mat[1][0] * mat[2][1] - mat[2][0] * mat[1][1] )
			     +  ny * (mat[2][0] * mat[0][1] - mat[0][0] * mat[2][1] )
			     +  nz * (mat[0][0] * mat[1][1] - mat[1][0] * mat[0][1] );
		
		PdVector norm3 = new PdVector( norm_x,norm_y,norm_z);
		norm3.normalize();
	
//		System.out.print("pt"+vert.toShortString());
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

	@Override
	public boolean doNormals() {
	    return doNormals;
	}

}
