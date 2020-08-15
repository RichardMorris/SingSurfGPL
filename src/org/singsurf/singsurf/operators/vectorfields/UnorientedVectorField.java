package org.singsurf.singsurf.operators.vectorfields;

import org.singsurf.singsurf.calculators.Evaluator;
import org.singsurf.singsurf.operators.UnSuportedGeometryException;

import org.singsurf.singsurf.jepwrapper.EvaluationException;

import jv.geom.PgPointSet;
import jv.geom.PgVectorField;
import jv.project.PgGeometryIf;
import jv.vecmath.PdVector;

public class UnorientedVectorField extends SimpleCalcField {

	public UnorientedVectorField(Evaluator calc) {
		super(calc);
	}

	@Override
	public PdVector calcVector(PdVector vert) throws EvaluationException {
		PdVector vec = super.calcVector(vert);
		boolean flag = vec.normalize();
		if(!flag)
			return new PdVector(vec.getSize());
		vec.multScalar(length);
		return vec;
	}

	@Override
	public PgGeometryIf operatePoints(PgPointSet geom) throws EvaluationException {

		PgVectorField field = new PgVectorField(3,PgVectorField.VERTEX_BASED);
		PgVectorField field2 = new PgVectorField(3,PgVectorField.VERTEX_BASED);
		field.setGeometry(geom);
		field2.setGeometry(geom);
		
		for(int i=0;i<geom.getNumVertices();++i)
		{
			PdVector vec = calcVector(geom.getVertex(i));
			PdVector vec2 = (PdVector) vec.clone();
			vec2.multScalar(-1.0);
			field.setVector(i,vec);
			field2.setVector(i, vec2);
		}
		
		geom.addVectorField(field);
		geom.addVectorField(field2);
		geom.showSingleVectorField(false);

		return field;
	}

	@Override
	public PgVectorField[] operateAll(PgGeometryIf out) throws EvaluationException, UnSuportedGeometryException {

		PgPointSet geom = (PgPointSet) out;
	
		PgVectorField field = new PgVectorField(3,PgVectorField.VERTEX_BASED);
		PgVectorField field2 = new PgVectorField(3,PgVectorField.VERTEX_BASED);
		field.setGeometry(geom);
		field2.setGeometry(geom);
		
		for(int i=0;i<geom.getNumVertices();++i)
		{
			PdVector vec = calcVector(geom.getVertex(i));
			PdVector vec2 = (PdVector) vec.clone();
			vec2.multScalar(-1.0);
			field.setVector(i,vec);
			field2.setVector(i, vec2);
		}
		
		geom.addVectorField(field);
		geom.addVectorField(field2);
		geom.showSingleVectorField(false);

		return new PgVectorField[] { field, field2 };

	}

	
	
}
