package org.singsurf.singsurf.operators.vectorfields;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.Test;
import org.singsurf.singsurf.calculators.Evaluator;
import org.singsurf.singsurf.operators.UnSuportedGeometryException;

import org.singsurf.singsurf.jepwrapper.EvaluationException;

import jv.geom.PgPointSet;
import jv.geom.PgVectorField;
import jv.project.PgGeometryIf;
import jv.vecmath.PdVector;

/**
 * Solves a cubic equation for a vector field in parameter space.
 * Input like {@code [a,b,c,d]} results of {@code C(i^3), C(i^2j) C(ij^2) C(j^3)}.
 *
 */
public class CubicVectorField extends SimpleCalcField implements MultipleVectorField {

	public CubicVectorField(Evaluator calc) {
		super(calc);
	}

	static double acosh(double val) {
        double res = Math.log(val+Math.sqrt(val*val-1));
        return res;
	}
	
	static double asinh(double val) {
        double res = Math.log(val+Math.sqrt(val*val+1));
        return res;
	}
	
	@Override
	public PdVector[] calcVectors(PdVector vert) throws EvaluationException {

		double topRes[] = calc.evalTop(vert.getEntries());
		for(double val:topRes) {
			if(!Double.isFinite(val)) {
				return new PdVector[] {new PdVector(3)};
			}
		}
		boolean xovery = Math.abs(topRes[0]) > Math.abs(topRes[3]);
		
		double a = xovery ? topRes[0] : topRes[3];
		double b = xovery ? topRes[1] : topRes[2];
		double c = xovery ? topRes[2] : topRes[1];
		double d = xovery ? topRes[3] : topRes[0];
		double E = topRes[4];
		double F = topRes[5];
		double G = topRes[6];
		QuadraticForm firstFundamentalForm = new QuadraticForm(E,F,G);

		double[] sols = solveCubic(a, b, c, d);
		PdVector[] vecs = new PdVector[sols.length];
		for(int i=0;i<sols.length;++i) {
			double u = xovery ? sols[i] : 1;
			double v = xovery ? 1 : sols[i];
			PdVector p = new PdVector(u,v);
			PdVector q = firstFundamentalForm.orthogonal(p);

			
			double len = length / q.length();
			if(!Double.isFinite(len)) {
				vecs[i] = new PdVector(3);
			}
			else {
				vecs[i] = new PdVector(q.getFirstEntry()* len,q.getLastEntry()*len,0.0);
			}
//			double val = evalForm(a,b,c,d,vecs[i]);
//			double val2 = evalForm(d,c,b,a,vecs[i]);
//			System.out.printf("C(%6.3f) = %6.3f (%3.6f %3.6f) %6.3f %6.3f%n",
//					sols[i], evalCubic(a,b,c,d,sols[i]),
//					vecs[i].getFirstEntry(),vecs[i].getEntry(1),val,val2);
		}
		return vecs;		
	}

	public Object evalCubic(double a, double b, double c, double d, double x) {
		return a*x*x*x+b*x*x+c*x+d;
	}

	public double evalForm(double a, double b, double c, double d, PdVector vec) {
		double x = vec.getEntry(0);
		double y = vec.getEntry(1);
		
		return a*x*x*x+b*x*x*y+c*x*y*y+d*y*y*y;
	}

	/**
	 * @param a
	 * @param b
	 * @param c
	 * @param d
	 */
	public static double[] solveCubic(double a, double b, double c, double d) {
		// Have a (x/y)^3 + b(x/y)^2+c(x/y)+d=0;
		// Depressed cubic t^3 + p t + q
		// t = x + b/3a
		double p =(3*a*c-b*b)/(3*a*a);
		double q =(2*b*b*b-9*a*b*c+27*a*a*d)/(27*a*a*a);
		// {\displaystyle -\left(4\,p^{3}+27\,q^{2}\right).}
		// subs t = w - 3/w
		// gives w^3 + q - p^3/(27w^3)
		// or    W^2 + q W - p^3/27=0
		//       W^2 + wb W + wc = 0
//		double wb = q, wc = -p*p*p/27;
		double descrim =  q*q/4 + p*p*p/27;
		
		double[] sols=null;
		if(descrim==0) {
			if(p==0) {
				double t = 0;
				double x = t - b/(3*a);
				sols = new double[] {x};
			} else {
				double t1 = 3*q/p;
				double t2 = -3*q/(2*p);
				double x1 = t1 - b/(3*a);
				double x2 = t2 - b/(3*a);
				sols = new double[] {x1,x2};
			}
		} else if(descrim<0) {
//			double W1 = -q/2 + Math.sqrt(descrim);
//			double W2 = -q/2 - Math.sqrt(descrim);
//			double w = 
			final double root = 1.0/3*Math.acos(3*q/(2*p)*Math.sqrt(-3/p));
			double t1=2*Math.sqrt(-p/3)*Math.cos( root -2*Math.PI*1/3);
			double t2=2*Math.sqrt(-p/3)*Math.cos( root -2*Math.PI*2/3);
			double t3=2*Math.sqrt(-p/3)*Math.cos( root -2*Math.PI*3/3);
			double x1 = t1 - b/(3*a);
			double x2 = t2 - b/(3*a);
			double x3 = t3 - b/(3*a);
			sols = new double[] {x1,x2,x3};			
		} else {
			if(p==0) {
				double t0=Math.cbrt(-q);
				double x0 = t0 - b/(3*a);
				sols = new double[] {x0};				
			}
			else if(p<0) {
				double t0=-2*Math.signum(q)*Math.sqrt(-p/3)*Math.cosh( 1.0/3*acosh(-3*Math.abs(q)/(2*p) * Math.sqrt(-3/p)));
				double x0 = t0 - b/(3*a);
				sols = new double[] {x0};
//)\quad {\text{if }}\quad 4p^{3}+27q^{2}>0{\text{ and }}p<0\,,\\
			} else {
				double t0 =-2 * Math.sqrt(p/3) * Math.sinh( 1.0/3 *asinh( 3*q/(2*p) * Math.sqrt(3/p) ));
				double x0 = t0 - b/(3*a);
				sols = new double[] {x0};				
			}
		}
		return sols;
	}

	@Override
	public PgVectorField[] operateAll(PgGeometryIf out) throws EvaluationException, UnSuportedGeometryException {

		PgPointSet geom = (PgPointSet) out;
	
		PgVectorField field1 = new PgVectorField(3,PgVectorField.VERTEX_BASED);
		PgVectorField field2 = new PgVectorField(3,PgVectorField.VERTEX_BASED);
		PgVectorField field3 = new PgVectorField(3,PgVectorField.VERTEX_BASED);
		PgVectorField field4 = new PgVectorField(3,PgVectorField.VERTEX_BASED);
		PgVectorField field5 = new PgVectorField(3,PgVectorField.VERTEX_BASED);
		PgVectorField field6 = new PgVectorField(3,PgVectorField.VERTEX_BASED);
		field1.setGeometry(geom);
		field2.setGeometry(geom);
		field3.setGeometry(geom);
		field4.setGeometry(geom);
		field5.setGeometry(geom);
		field6.setGeometry(geom);
		
		for(int i=0;i<geom.getNumVertices();++i)
		{
			PdVector[] vecs = calcVectors(geom.getVertex(i));
			field1.setVector(i,vecs[0]);
			PdVector vec2 = (PdVector) vecs[0].clone();
			vec2.multScalar(-1.0);
			field2.setVector(i, vec2);
			
			if(vecs.length>1) {
			field3.setVector(i,vecs[1]);
			PdVector vec4 = (PdVector) vecs[1].clone();
			vec4.multScalar(-1.0);
			field4.setVector(i, vec4);
			} else {
				field3.setVector(i, new PdVector(3));
				field4.setVector(i, new PdVector(3));
			}

			if(vecs.length>2) {
			field5.setVector(i,vecs[2]);
			PdVector vec6 = (PdVector) vecs[2].clone();
			vec6.multScalar(-1.0);
			field6.setVector(i, vec6);
			} else {
				field5.setVector(i, new PdVector(3));
				field6.setVector(i, new PdVector(3));
			}

		}
		
		geom.addVectorField(field1);
		geom.addVectorField(field2);
		geom.addVectorField(field3);
		geom.addVectorField(field4);
		geom.addVectorField(field5);
		geom.addVectorField(field6);
		geom.showSingleVectorField(false);

		return new PgVectorField[] { field1, field2, field3, field4, field5, field6 };

	}
	
	/**
	 * (qx qy) (E G) (px) = 0
	 *         (G F) (py)
	 *         
	 * (qx qy) (E px + G py) = (qx qy) (A)
	 *         (G px + F py)           (B)
	 * @param preVec
	 * @param E
	 * @param G
	 * @param F
	 * @return
	 */
	public PdVector orthogonalVector(PdVector preVec, double E, double G, double F) {
		double A = E * preVec.getEntry(0) + F * preVec.getEntry(1);
		double B = E * preVec.getEntry(0) + G * preVec.getEntry(1);
		
		return new PdVector(-B,A,0);
	}
	
	public void normTgt(PdVector preVec,double E, double G, double F) {
		double x = preVec.getEntry(0);
		double y = preVec.getEntry(1);
		double len = Math.sqrt(E*x*x+2*F*x*y+G*y*y);
		preVec.multScalar(1/len);
	}

	public static class Tester {
		
	double[] coeff(double a,double b,double c) {
		return new double[] {-(a+b+c),(a*c+a*b+b*c),-a*b*c};
	}
	
	/**
	 *  coeffs of (x-a)(x^2+bx+c)
	 */
	double[] quadcoeff(double a,double b,double c) {
		return new double[] {b-a,c-a*b,-a*c};
	}
	
	double eval(double[] co,double x) {
		double res = x*x*x + co[0]*x*x + co[1]*x+co[2];
		return res;
	}
	
	double eval(double a,double b, double c, double d,double x) {
		double res = a*x*x*x + b*x*x + c*x+d;
		return res;
	}
	
	
	@Test
	public void testCublic() {
		double[] res = solveCubic(1,0,0,0);
		assertArrayEquals(new double[] {0},res);
		
		res = solveCubic(1,0,0,-1);
		assertArrayEquals(new double[] {1},res);
		
		double[] co = coeff(1,2,3);
		res = solveCubic(1,co[0],co[1],co[2]);
		assertArrayEquals(new double[] {1,2,3},res);

		co = coeff(-1,2,3);
		res = solveCubic(1,co[0],co[1],co[2]);
		assertArrayEquals(new double[] {-1,2,3},res);

		co = quadcoeff(1,2,2);
		res = solveCubic(1,co[0],co[1],co[2]);
		assertArrayEquals(new double[] {1},res);

		co = quadcoeff(1,2,1);
		res = solveCubic(1,co[0],co[1],co[2]);
		assertArrayEquals(new double[] {-1,1},res);

		res = solveCubic(1,0,2,0);
		assertArrayEquals(new double[] {0},res);

		
		for(int i=0;i<100;++i) {
			double a = 20 *Math.random() - 10;
			double b = 20 *Math.random() - 10;
			double c = 20 *Math.random() - 10;
			double d = 20 *Math.random() - 10;
			res = solveCubic(a,b,c,d);
			for(double x:res) {
				double val = eval(a,b,c,d,x);
				assertEquals(0.0,val,1e-8);
			}
		}
	}

	public void assertArrayEquals(double[] expected,double[] test) {
		assertEquals(expected.length,test.length);
		Arrays.sort(test);
		for(int i=0;i<expected.length;++i) 
			assertEquals(expected[i],test[i],1e-9);
	}
	}
}
