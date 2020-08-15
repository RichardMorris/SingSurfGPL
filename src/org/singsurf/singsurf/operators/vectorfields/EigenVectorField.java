package org.singsurf.singsurf.operators.vectorfields;

import org.singsurf.singsurf.calculators.Evaluator;
import org.singsurf.singsurf.operators.UnSuportedGeometryException;

import org.singsurf.singsurf.jepwrapper.EvaluationException;

import jv.geom.PgPointSet;
import jv.geom.PgVectorField;
import jv.project.PgGeometryIf;
import jv.vecmath.PdVector;

/**
 * Solves the eigenvector problem in parameter space.
 * @author rich
 *
 */
public class EigenVectorField extends SimpleCalcField implements MultipleVectorField {

	public EigenVectorField(Evaluator calc) {
		super(calc);
	}

	@Override
	public PdVector[] calcVectors(PdVector vert) throws EvaluationException {

		double topRes[] = calc.evalTop(vert.getEntries());
		for(double val:topRes) {
			if(!Double.isFinite(val)) {
				return new PdVector[] {new PdVector(3), new PdVector(3)};
			}
		}
		double a = topRes[0];
		double b = topRes[1];
		double c = topRes[2];
		double d = topRes[3];

		double det = a* d - b *c;
		double tr = a + d;
		double descrim = tr * tr - 4 * det;
		if(descrim<0) {
			return new PdVector[] {new PdVector(3), new PdVector(3)};
		}
		double lam1 = (tr + Math.sqrt(descrim) )/ 2.0;
		double lam2 = tr - lam1;
//		double lam = major ? lam1 : lam2;

		double A1 = a - lam1;
		double A2 = a - lam2;
		double B = b;
		double C = c;
		double D1 = d - lam1;
		double D2 = d - lam2;
		
		double u,v;
		if( A1*A1 + B*B > C*C + D1*D1) {
			u = -B; v = A1;
		} else {
			u = -D1; v = C;
		}
		double len = length / Math.sqrt(u*u+v*v);
		if(!Double.isFinite(len)) {
			return new PdVector[] {new PdVector(3), new PdVector(3)};
		}
		PdVector out1 = new PdVector(u * len,v *len,0.0);

		double u2,v2;
		if( A2*A2 + B*B > C*C + D2*D2) {
			u2 = -B; v2 = A2;
		} else {
			u2 = -D2; v2 = C;
		}
		double len2 = length / Math.sqrt(u2*u2+v2*v2);
		if(!Double.isFinite(len2)) {
			return new PdVector[] {new PdVector(3), new PdVector(3)};
		}
		PdVector out2 = new PdVector(u2 * len2,v2 *len2,0.0);
		
		
		return new PdVector[] {out1,out2};

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
