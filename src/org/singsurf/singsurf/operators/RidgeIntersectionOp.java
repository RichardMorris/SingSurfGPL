/*
Created 27-Apr-2006 - Richard Morris
*/
package org.singsurf.singsurf.operators;

import org.singsurf.singsurf.calculators.Evaluator;
import org.singsurf.singsurf.operators.vectorfields.MultipleVectorField;

import org.singsurf.singsurf.jepwrapper.EvaluationException;

import jv.vecmath.PdVector;

public class RidgeIntersectionOp extends AbstractIntersect {
	Evaluator calc;
	MultipleVectorField vecField;
	private int nItterations;
	
	public RidgeIntersectionOp(Evaluator calc, MultipleVectorField vf, int nitts) {
		this.calc = calc;
		this.vecField = vf;
		this.nItterations = nitts;
	}
	
	static class PointResult {
		double val;
		PdVector P;
		PdVector Q;
		PdVector pt;
		
		public PointResult(PdVector pt,double val, PdVector p, PdVector q) {
			super();
			this.pt = pt;
			this.val = val;
			P = p;
			Q = q;
		}
		
		void fixOrientation(PointResult base) {
			if(P.dot(base.P) < 0) {
				P.multScalar(-1);
			}
			
			if(Q.dot(base.Q) < 0) {
				Q.multScalar(-1);
			}	
		}
		
		public String toString() {
			return String.format("pt (%6.3f,%6.3f,%6.3f) val %9.6f P (%6.3f,%6.3f,%6.3f) Q (%6.3f,%9.3f,%6.3f)", 
					pt.getEntry(0),pt.getEntry(1),pt.getEntry(2),val,
					P.getEntry(0),P.getEntry(1),P.getEntry(2),
					Q.getEntry(0),Q.getEntry(1),Q.getEntry(2)
					);
		}
	}

	public PointResult calcValue(PdVector A)  throws EvaluationException {
		PdVector[] dirs1 = vecField.calcVectors(A);
		
		
		final PdVector P1 = dirs1[0];
		final PdVector Q1 = dirs1[1];

		double[] in1 = new double[] {A.getEntry(0),A.getEntry(1),A.getEntry(2), 
				P1.getEntry(0),P1.getEntry(1),P1.getEntry(2),
				Q1.getEntry(0),Q1.getEntry(1),Q1.getEntry(2) };

		try {
            double[] val1 = calc.evalTop(in1);

            return new PointResult(A,val1[0],P1,Q1);
        } catch (NullPointerException e) {
            e.printStackTrace();
            throw e;
        }
	}

	public PointResult calcVectors(PdVector A)  throws EvaluationException {
		PdVector[] dirs1 = vecField.calcVectors(A);
		
		
		final PdVector P1 = dirs1[0];
		final PdVector Q1 = dirs1[1];
		return new PointResult(A,Double.NaN,P1,Q1);
	}

	public void calcValue(PointResult pr)  throws EvaluationException {
		
		final PdVector P1 = pr.P;
		final PdVector Q1 = pr.Q;

		double[] in1 = new double[] {pr.pt.getEntry(0),pr.pt.getEntry(1),pr.pt.getEntry(2), 
				P1.getEntry(0),P1.getEntry(1),P1.getEntry(2),
				Q1.getEntry(0),Q1.getEntry(1),Q1.getEntry(2) };

		try {
            double[] val1 = calc.evalTop(in1);

            pr.val = val1[0];
        } catch (NullPointerException e) {
            e.printStackTrace();
            throw e;
        }
	}



	@Override
	public PdVector calculateIntersection(PdVector A, PdVector B) throws EvaluationException {
		PointResult resA = calcValue(A);
		PointResult resB = calcVectors(B);
		resB.fixOrientation(resA);
		calcValue(resB);		
		
		PointResult high=null;
		PointResult low=null;
		
		if(!Double.isFinite(resA.val) ) {
			System.out.println("Bad value "+resA);
			return null;
		} else if(!Double.isFinite(resA.val) ) {
			System.out.println("Bad value "+resA);
			return null;
		} else if(resA.val ==0) {
			return A;
		} else if(resB.val ==0) {
			return B;
		} else if(resA.val > 0 && resB.val < 0) {
			high = resA;
			low = resB;
		} else if(resA.val < 0 && resB.val > 0) {
			high = resB;
			low = resA;
		} else {
			return null;
		}
		
		
        for(int i=nItterations;i>=0;--i) {
        	double lambda = -low.val / ( high.val - low.val);
        	PdVector mid = PdVector.blendNew(1-lambda, low.pt, lambda, high.pt);
    		if(i==0) {
    			return mid;
    		}
    		PointResult pr = calcVectors(mid);
    		pr.fixOrientation(high);
    		calcValue(pr);
    		if(!Double.isFinite(pr.val)) {
    			System.out.println("Bad value "+resA);  
    			return null;
    		}
    		if(pr.val==0.0) {
    			return pr.pt;
    		}
    		if(pr.val>0) {
    			if(pr.val > high.val) {
        			System.out.println("Diverging\n\thigh "+high+"\n\tlow  "+low+"\n\tmid  "+pr+"\n");  
        			return null;
    			}
    			high = pr;
    		} else {
    			if(pr.val < low.val) {
        			System.out.println("Diverging\n\thigh "+high+"\n\tlow  "+low+"\n\tmid  "+pr+"\n");  
        			return null;
    			}
    			low = pr;
    		}
        }
        System.out.println("Loop finished");
        return null;
	}


	@Override
	protected PdVector calculateIntersectionWithTexture(PdVector vertex, PdVector vertex2, PdVector vertexTexture,
			PdVector vertexTexture2) throws EvaluationException {
		
		return calculateIntersection(vertexTexture,vertexTexture2);
	}

	@Override
	public boolean testIntersection(PdVector A, PdVector B) throws EvaluationException {
		PointResult resA = calcValue(A);
		PointResult resB = calcVectors(B);
		resB.fixOrientation(resA);
		calcValue(resB);		
		return resA.val * resB.val < 0.0;
	}

	
}
