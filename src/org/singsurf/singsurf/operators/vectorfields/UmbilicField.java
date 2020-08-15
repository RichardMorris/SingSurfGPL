package org.singsurf.singsurf.operators.vectorfields;

import org.singsurf.singsurf.calculators.Evaluator;

import org.singsurf.singsurf.jepwrapper.EvaluationException;

import jv.vecmath.PdVector;

public class UmbilicField extends CubicVectorField {
	
	public UmbilicField(Evaluator calc) {
		super(calc);

	}

	@Override
	public PdVector[] calcVectors(PdVector vert) throws EvaluationException {

		double topRes[] = calc.evalTop(vert.getEntries());
		for(double val:topRes) {
			if(!Double.isFinite(val)) {
				return new PdVector[] {new PdVector(3)};
			}
		}
		double a = topRes[0];
		double b = topRes[1];
		double c = topRes[2];
		double d = topRes[3];
		double E = topRes[4];
		double F = topRes[5];
		double G = topRes[6];
		QuadraticForm firstFundamentalForm = new QuadraticForm(E,F,G);
		CubicForm v3 = new CubicForm(a,b,c,d);
		
		boolean xovery = Math.abs(topRes[0]) > Math.abs(topRes[3]);
		double[] sols = xovery ? solveCubic(a, 3*b, 3*c, d) : solveCubic(d, 3*c, 3*b, a);
		PdVector[] vecs = new PdVector[sols.length];
		for(int i=0;i<sols.length;++i) {
			double u = xovery ? sols[i] : 1;
			double v = xovery ? 1 : sols[i];
			PdVector p = new PdVector(u,v);
			PdVector q = firstFundamentalForm.orthogonal(p);
//			double test = firstFundamentalForm.apply(p, q);
			LinearForm lin = v3.apply(p).apply(q);
			PdVector r = lin.conj();
			double len = length / r.length();
			
			System.out.printf("p (%4.1f %4.1f) q (%4.1f %4.1f) r (%4.1f %4.1f) v3<pqr> %4.1f v3<ppp> %4.1f%n",
					p.getFirstEntry(),p.getLastEntry(),
					q.getFirstEntry(),q.getLastEntry(),
					r.getFirstEntry(),r.getLastEntry(),
					v3.apply(p, q, r), v3.apply(p, p, p)
										);
			
			if(!Double.isFinite(len)) {
				vecs[i] = new PdVector(3);
			}
			else {
				vecs[i] = new PdVector(r.getFirstEntry()* len,r.getLastEntry()*len,0.0);
			}
			
			
//			double val = evalForm(a,b,c,d,vecs[i]);
//			double val2 = evalForm(d,c,b,a,vecs[i]);
//			System.out.printf("C(%6.3f) = %6.3f (%3.6f %3.6f) %6.3f %6.3f%n",
//					sols[i], evalCubic(a,b,c,d,sols[i]),
//					vecs[i].getFirstEntry(),vecs[i].getEntry(1),val,val2);
		}
		return vecs;		
	}

	
	
}
