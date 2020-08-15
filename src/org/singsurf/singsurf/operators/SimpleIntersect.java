/*
Created 26-Apr-2006 - Richard Morris
*/
package org.singsurf.singsurf.operators;

import jv.geom.PgPointSet;
import jv.project.PgGeometryIf;
import jv.vecmath.PdVector;

import org.singsurf.singsurf.jepwrapper.EvaluationException;

/**
 * Base class for intersection algorithms which test zero crossing of a real valued function.
 * @author Richard Morris
 *
 */
public abstract class SimpleIntersect extends AbstractIntersect {
    int nItterations = 5; 

    protected double vertexVals[];
	//protected int vertexSigns[];

	@Override
	protected void setup(PgGeometryIf geom) throws EvaluationException {
		super.setup(geom);
		int n=((PgPointSet)geom).getNumVertices();
		vertexVals = new double[n];
		for(int i=0;i<n;++i)
			vertexVals[i] = findValue(i);
	}

	@Override
	protected void tidyUp() {
		vertexVals = null;
		super.tidyUp();
	}

	@Override
	public boolean testIntersection(int a, int b) {
		if(vertexVals[a]==0) return true;
		if(vertexVals[b]==0) return false;
		return((vertexVals[a]>0 && vertexVals[b] <0)
		     ||(vertexVals[a]<0 && vertexVals[b] >0));
	}

	/* (non-Javadoc)
	 * @see org.singsurf.singsurf.operators.AbstractIntersect#testCrossing(jv.vecmath.PdVector, jv.vecmath.PdVector)
	 */
	@Override
	public boolean testIntersection(PdVector A, PdVector B) throws EvaluationException {
		double valA = findValue(A);
		double valB = findValue(B);
		return(valA==0||(valA>0 && valB <0)
			     ||(valA<0 && valB >0));
	}


	@Override
	protected PdVector calculateIntersection(int a, int b) throws EvaluationException {
		if(this.paramsFromTexture) {
			return calculateIntersection(
					inGeom.getVertex(a),
					texrng.scale(((PgPointSet)inGeom).getVertexTexture(a)),
					vertexVals[a],
					inGeom.getVertex(b),
					texrng.scale(((PgPointSet)inGeom).getVertexTexture(b)),
					vertexVals[b]);
		} else {
		return calculateIntersection(
				inGeom.getVertex(a),vertexVals[a],
				inGeom.getVertex(b),vertexVals[b]);
		}
	}
	

	/**
	 * Calculates intersection between two points, with given function values.
	 * Uses a linear interpolation between the points.
	 * @param A first point
	 * @param aVal function value for first point
	 * @param B second point
	 * @param bVal function value for second point
	 * @return the intersection point or null if it cannot be found
	 * @throws EvaluationException 
	 */
	@Override
	public PdVector calculateIntersection(PdVector A, PdVector B) throws EvaluationException {

		double aVal = findValue(A);
		double bVal = findValue(B);
		
		
		return calculateIntersection(A, aVal, B, bVal);
	}

	/**
	 * @param A
	 * @param aVal
	 * @param B
	 * @param bVal
	 * @return
	 * @throws EvaluationException
	 */
	protected PdVector calculateIntersection(PdVector A, double aVal, PdVector B, double bVal)
			throws EvaluationException {
		if(aVal==0.0) return (PdVector) A.clone();
		if(bVal==0.0) return null;
		if(aVal*bVal>0.0) return null;
		PdVector C= new PdVector(dim);
        PdVector H,L;
        double cVal;
        if(aVal > 0.0) {
            H = A; L = B;
        } else {
            H = B; L = A;
            cVal = aVal; aVal = bVal; bVal = cVal;
        }
        for(int i=nItterations;i>=0;--i) {
            double lambda = - bVal / ( aVal - bVal);
            C.blend(lambda,H,1-lambda,L);
            if(i==0) break;
            
            cVal = findValue(C);
            if(cVal > 0.0) {
                H = C; aVal = cVal;
            } else {
                L = C; bVal = cVal;
            }
            C= new PdVector(dim);
        }
		return C;
	}

	
	@Override
	protected PdVector calculateIntersectionWithTexture(PdVector A, PdVector B, PdVector Atex,
			PdVector Btex) throws EvaluationException {
		double aVal = findValue(Atex);
		double bVal = findValue(Btex);
		
		return calculateIntersection(A, Atex, aVal, B, Btex, bVal);
	}

	/**
	 * @param A
	 * @param Atex
	 * @param aVal
	 * @param B
	 * @param Btex
	 * @param bVal
	 * @return
	 * @throws EvaluationException
	 */
	protected PdVector calculateIntersection(PdVector A, PdVector Atex, double aVal, PdVector B, PdVector Btex,
			double bVal) throws EvaluationException {
		if(aVal==0.0) return (PdVector) A.clone();
		if(bVal==0.0) return null;
		if(aVal*bVal>0.0) return null;
		PdVector Mtex= new PdVector(3);

		double Hpos,Lpos,Mpos=0.5;
		PdVector Htex,Ltex;
		double hVal,lVal;
        double mVal;
        if(aVal > 0.0) {
            Hpos = 0; Lpos = 1;
            Htex = Atex; Ltex = Btex;
            hVal = aVal; lVal = bVal;
        } else {
            Hpos = 1; Lpos = 0;
            Htex = Btex; Ltex = Atex;
            hVal = bVal; lVal = aVal;
        }
        for(int i=nItterations;i>=0;--i) {
            double lambda = -lVal / ( hVal - lVal);
            Mpos = lambda * Hpos + (1-lambda) * Lpos;
            if(i==0) break;

            Mtex.blend(lambda,Htex,1-lambda,Ltex);
            mVal = findValue(Mtex);
            if(mVal > 0.0) {
                Hpos = Mpos; hVal = mVal;
                Htex = Mtex;
            } else {
                Lpos = Mpos; lVal = mVal;
                Ltex = Mtex;
            }
            Mtex = new PdVector(3);
        }
		PdVector C= new PdVector(dim);
		C.blend(1-Mpos, A, Mpos, B);
		return C;
	}

	
	/**
	 * Calculates the function value at a give point.
	 * @param vec
	 * @return the value
	 * @throws EvaluationException 
	 */

	public abstract double findValue(PdVector vec) throws EvaluationException;
	
	public double findValue(int a) throws EvaluationException {
//		if(this.paramsFromTexture) 
//			return findValue(texrng.scale(((PgPointSet)inGeom).getVertexTexture(a)));
	    return findValue(getVertexOrTexture((PgPointSet)inGeom,a));
	}
	
	   public int getnItterations() {
	        return nItterations;
	    }

	    public void setnItterations(int nItterations) {
	        this.nItterations = nItterations;
	    }


}
