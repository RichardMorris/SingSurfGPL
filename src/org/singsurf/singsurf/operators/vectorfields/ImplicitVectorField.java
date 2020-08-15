package org.singsurf.singsurf.operators.vectorfields;

import org.singsurf.singsurf.calculators.Evaluator;
import org.singsurf.singsurf.operators.UnSuportedGeometryException;

import org.singsurf.singsurf.jepwrapper.EvaluationException;

import jv.geom.PgPointSet;
import jv.geom.PgVectorField;
import jv.project.PgGeometryIf;
import jv.vecmath.PdMatrix;
import jv.vecmath.PdVector;

public class ImplicitVectorField extends SimpleCalcField {
	public ImplicitVectorField(Evaluator calc) {
		super(calc);
	}

	public PdVector[] calcVectors(PdVector vert) throws EvaluationException {

		double topRes[] = calc.evalTop(vert.getEntries());
		for(double val:topRes) {
			if(!Double.isFinite(val)) {
				return new PdVector[] {new PdVector(3), new PdVector(3)};
			}
		}
		double Hxx = topRes[0];
		double Hxy = topRes[1];
		double Hxz = topRes[2];
		double Hyy = topRes[3];
		double Hyz = topRes[4];
		double Hzz = topRes[5];
		double Hx = topRes[6];
		double Hy = topRes[7];
		double Hz = topRes[8];
		PdVector N = new PdVector(Hx,Hy,Hz);
		double lamsq = N.sqrLength();
		N.normalize();
		
		/*
		System.out.printf("%6.3f %6.3f %6.3f %6.3f %6.3f %6.3f %6.3f %6.3f %6.3f%n",
				Hxx,Hxy,Hxz,Hyy,Hyz,Hzz,Hx,Hy,Hz);

		double numer = 
				+ Hxx * (Hy * Hy + Hz * Hz) 
				+ Hyy * (Hx * Hx + Hz * Hz) 
				+ Hzz * (Hx * Hx + Hy * Hy)
				- 2 * Hxy * Hx * Hy 
				- 2 * Hxz * Hx * Hz 
				- 2 * Hyz * Hy * Hz;
		double denom = 2 * Math.pow(Hx * Hx + Hy * Hy + Hz * Hz, 1.5);
		double H = numer / denom;

		double numerG = 
				+ (Hzz * Hyy - Hyz * Hyz) * Hx * Hx 
				+ (Hxx * Hzz - Hxz * Hxz) * Hy * Hy
				+ (Hxx * Hyy - Hxy * Hxy) * Hz * Hz 
				+ 2 * (Hxy * Hxz - Hxx * Hyz) * Hy * Hz
				+ 2 * (Hxy * Hyz - Hyy * Hxz) * Hx * Hz 
				+ 2 * (Hxz * Hyz - Hzz * Hxy) * Hx * Hy;
		double denomG = lamsq * lamsq;
		double K = numerG / denomG;

		double A = Hxz * Hy - Hxy * Hz;
		double D = Hxy * Hz - Hyz * Hx;
		double F = Hyz * Hx - Hxz * Hy;
		double B = (Hz*Hxx - Hx*Hxz + Hy*Hyz - Hz*Hyy)/2;
		double C = (Hy*Hzz - Hz*Hyz + Hx*Hxy - Hy*Hxx)/2;
		double E = (Hx*Hyy - Hy*Hxy + Hz*Hxz - Hx*Hzz)/2;
		
		double det = A * D * F + 2*B*E*C - A*E*E - D*C*C - B*B*F;
		double trace = A+D+F;
		double cofac = A*D-B*B + A*F-C*C + D*F-E*E;
		
		
		System.out.printf("len %6.3f tr %6.3f co %6.3f det %6.3f K %6.3f H %6.3f%n",
				Math.sqrt(lamsq),trace,cofac,det,K,H);
		*/
		
		PdVector Hvec = new PdVector(Hx,Hy,Hz);
		PdVector Ivec = new PdVector(1,0,0);
		PdVector Jvec = new PdVector(0,1,0);
		PdVector Kvec = new PdVector(0,0,1);
		//PdMatrix hess = new PdMatrix(new double[][] {{A,B,C}, {B,D,E}, {C,E,F}});
		PdMatrix Huu = new PdMatrix(new double[][] {{Hxx,Hxy,Hxz}, {Hxy,Hyy,Hyz}, {Hxz,Hyz,Hzz}});
		
		PdVector U=null;
		// Now lets find the two best tangent vectors
		if(Math.abs(Hx)>=Math.abs(Hy)) {
			if(Math.abs(Hx)>=Math.abs(Hz)) {
				U = PdVector.crossNew(Hvec, Kvec);
			} else {
				U = PdVector.crossNew(Hvec, Jvec);
			}
		} else {
			if(Math.abs(Hy)>=Math.abs(Hz)) {
				U = PdVector.crossNew(Hvec, Ivec);
			} else {
				U = PdVector.crossNew(Hvec, Jvec);
			}
		}
		PdVector V = PdVector.crossNew(Hvec, U);
		U.normalize();
		V.normalize();
//		double Suu = hess.multQuadratic(U, U);
//		double Suv = hess.multQuadratic(U, V);
//		double Svv = hess.multQuadratic(V, V);
		
//		System.out.printf("Suu %6.3f %6.3f %6.3f%n",Suu,Suv,Svv);
		double Suu = Huu.multQuadratic(U, U);
		double Suv = Huu.multQuadratic(U, V);
		double Svv = Huu.multQuadratic(V, V);
//		System.out.printf("Suu %6.3f %6.3f %6.3f%n",Suu,Suv,Svv);

		/*
		PdVector Sx = new PdVector(1,0,Hx);
		PdVector Sy = new PdVector(0,1,Hy);
		PdVector Sxx = new PdVector(0,0,Hxx);
		PdVector Sxy = new PdVector(0,0,Hxy);
		PdVector Syy = new PdVector(0,0,Hyy);
		double EE = Sx.dot(Sx);
		double FF = Sx.dot(Sy);
		double GG = Sy.dot(Sy);
		double l = Sxx.dot(N);
		double m = Sxy.dot(N);
		double n = Syy.dot(N);
		System.out.printf("K %6.3f H %6.3f%n",
				(l*n-m*m)/(EE*GG-FF*FF),
				(EE*n+GG*l-2*FF*m)/(2*(EE*GG-FF*FF)));
		*/
		double Nlen = Math.sqrt(lamsq);
		double EE = 1;
		double FF = 0;
		double GG = 1;
		double l = Suu / Nlen;
		double m = Suv / Nlen;
		double n = Svv / Nlen;
		final double K = l*n-m*m;
		final double H = (n+l)/2;
//		System.out.printf("K %6.3f H %6.3f %n", K,H);
		
		double descrim = H*H - K;
		double kp = descrim>= 0 ?
				H + Math.sqrt( descrim ) : H;
		if(descrim < 0) {
			System.out.println("Negative descrim H "+H+" K "+K+" decrim "+descrim);
		}
//		double kq = H - Math.sqrt( H*H - K );
		
		double a = l - kp*EE;
		double b = m - kp*FF;
		double c = n - kp*GG;
		PdVector P,Q;
		if(a*a>c*c) {
			P = PdVector.blendNew(b, U, -a, V);
			Q = PdVector.blendNew(a, U,  b, V);
		} else {
			P = PdVector.blendNew(c, U, -b, V);			
			Q = PdVector.blendNew(b, U,  c, V);			
		}

		boolean pgood =  P.normalize(); 
		boolean qgood = Q.normalize();
		if(!pgood) P.set(0, 0,0);
		if(!qgood) Q.set(0, 0,0);
		
//		len * P / sqrt(P.P);
//		P = [u,v,0];
//		u = if(cond, b, c);
//		v= -if(cond, a, b);
//		cond = a^2 > c^2;

		P.multScalar(length);
		Q.multScalar(length);
		System.out.printf("pt (%6.3f %6.3f %6.3f) P (%6.3f %6.3f %6.3f) %b Q (%6.3f %6.3f %6.3f) %b %n", 
				vert.getEntry(0),vert.getEntry(1),vert.getEntry(2),
				P.getEntry(0),P.getEntry(1),P.getEntry(2),pgood,
				Q.getEntry(0),Q.getEntry(1),Q.getEntry(2),qgood);
		return new PdVector[] {P, Q};

	}

	@Override
	public PgVectorField[] operateAll(PgGeometryIf out) throws EvaluationException, UnSuportedGeometryException {

		PgPointSet geom = (PgPointSet) out;
	
		PgVectorField field1 = new PgVectorField(3,PgVectorField.VERTEX_BASED);
		PgVectorField field2 = new PgVectorField(3,PgVectorField.VERTEX_BASED);
		PgVectorField field3 = new PgVectorField(3,PgVectorField.VERTEX_BASED);
		PgVectorField field4 = new PgVectorField(3,PgVectorField.VERTEX_BASED);
		field1.setGeometry(geom);
		field2.setGeometry(geom);
		field3.setGeometry(geom);
		field4.setGeometry(geom);
		
		for(int i=0;i<geom.getNumVertices();++i)
		{
			PdVector[] vecs = calcVectors(geom.getVertex(i));
			field1.setVector(i,vecs[0]);
			PdVector vec2 = (PdVector) vecs[0].clone();
			vec2.multScalar(-1.0);
			field2.setVector(i, vec2);
			
			field3.setVector(i,vecs[1]);
			PdVector vec4 = (PdVector) vecs[1].clone();
			vec4.multScalar(-1.0);
			field4.setVector(i, vec4);
		}
		
		geom.addVectorField(field1);
		geom.addVectorField(field2);
		geom.addVectorField(field3);
		geom.addVectorField(field4);
		geom.showSingleVectorField(false);

		return new PgVectorField[] { field1, field2, field3, field4 };

	}

	
	
}
