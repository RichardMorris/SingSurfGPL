package org.singsurf.singsurf.operators;

import org.singsurf.singsurf.calculators.Evaluator;

import org.singsurf.singsurf.jepwrapper.EvaluationException;

import jv.geom.PgElementSet;
import jv.geom.PgPointSet;
import jv.geom.PgPolygonSet;
import jv.geom.PgVectorField;
import jv.vecmath.PdVector;

/** A map which sends tangent vectors to tangent vectors.
 * 
 * df<v>   = ( df/dx , df/dy )
 * 
 * @author rich
 *
 */
public class TangentSpaceCalcMap extends SimpleCalcMap {
	int dim_domain=2;
	boolean normalize;
	double length;
	
	public TangentSpaceCalcMap(Evaluator calc,boolean normalize,int ninputs,double length) {
		super(calc, false);
//		this.dim_domain = calc.getNumNormalInputVariables();
		this.dim_domain = ninputs;
		this.normalize = normalize;
		this.length = length;
	}

	@Override
	public boolean doNormals() {
		return false;
	}

	public void operateVectorField(PgVectorField field) throws EvaluationException {
		PdVector[] verts = field.getVertices();
		for(int i=0;i<field.getNumVectors();++i) {
			PdVector vect = field.getVector(i);
			PdVector image = mapVect(verts[i],vect);
			field.setVector(i, image);
		}
	}
	
	private PdVector mapVect(PdVector pt, PdVector vect) throws EvaluationException {

//		System.out.printf("(%7.4f,%7.4f,%7.4f) vect (%7.4f,%7.4f,%7.4f)%n",
//				pt.m_data[0],pt.m_data[1],pt.m_data[2],
//				vect.m_data[0],vect.m_data[1],vect.m_data[2]);

		// Need to calculate the top before we can evaluate derivatives
		calc.evalTop(pt.getEntries());
		
		double x =0,y=0,z=0;
		for(int i=0;i<this.dim_domain;++i) {
			double[] dfx = calc.evalDerivative(i);

			x += vect.m_data[i] * dfx[0];
			y += vect.m_data[i] * dfx[1];
			z += vect.m_data[i] * dfx[2];
		}

//		mat[0] = calc.evalDerivative(0);
//		mat[1] = calc.evalDerivative(1);
//
//		double x = vect.m_data[0] * mat[0][0] + vect.m_data[1] * mat[1][0];
//		double y = vect.m_data[0] * mat[0][1] + vect.m_data[1] * mat[1][1];
//		double z = vect.m_data[0] * mat[0][2] + vect.m_data[1] * mat[1][2];
		
		PdVector vec = new PdVector( x,y,z);

//		System.out.printf("-> (%7.4f,%7.4f,%7.4f) vect [%7.4f,%7.4f,%7.4f]%n",
//				topRes[0],topRes[1],topRes[2], x,y,z);

		if(this.normalize) {
			boolean flag = vec.normalize();
			if(!flag)
				return new PdVector(vec.getSize());
			vec.multScalar(length);
		} else if( ! Double.isFinite(vec.sqrLength())) {
			return new PdVector(vec.getSize());
		}
		
		return vec;
	}

	@Override
	public PgElementSet operateSurface(PgElementSet geom) throws EvaluationException {
//		System.out.println("inputs "+calc.getNumInputVariables()+" , "+
//		calc.getNumNormalInputVariables());

		for(int i=0; i<geom.getNumVectorFields(); ++i) {
			PgVectorField field = geom.getVectorField(i);
			this.operateVectorField(field);
		}
		PgElementSet res = super.operateSurface(geom);
		return res;
	}



	@Override
	public PgPolygonSet operateCurve(PgPolygonSet geom) throws EvaluationException {
		for(int i=0; i<geom.getNumVectorFields(); ++i) {
			PgVectorField field = geom.getVectorField(i);
			this.operateVectorField(field);
		}
		PgPolygonSet res =  super.operateCurve(geom);
		return res;
	}



	@Override
	public PgPointSet operatePoints(PgPointSet geom) throws EvaluationException {
		for(int i=0; i<geom.getNumVectorFields(); ++i) {
			PgVectorField field = geom.getVectorField(i);
			this.operateVectorField(field);
		}
		PgPointSet res = super.operatePoints(geom);
		return res;
	}

	public boolean isNormalize() {
		return normalize;
	}

	public void setNormalize(boolean normalize) {
		this.normalize = normalize;
	}

	public double getLength() {
		return length;
	}

	public void setLength(double length) {
		this.length = length;
	}


	
}
