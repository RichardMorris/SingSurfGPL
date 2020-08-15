package org.singsurf.singsurf.operators.vectorfields;

import org.singsurf.singsurf.operators.AbstractOperator;
import org.singsurf.singsurf.operators.UnSuportedGeometryException;

import org.singsurf.singsurf.jepwrapper.EvaluationException;

import jv.geom.PgElementSet;
import jv.geom.PgPointSet;
import jv.geom.PgPolygonSet;
import jv.geom.PgVectorField;
import jv.project.PgGeometryIf;
import jv.vecmath.PdVector;

public abstract class AbstractVectorField extends AbstractOperator {

	protected PgGeometryIf inGeom,outGeom;
	protected double length=1.0;

	@Override
	public PgGeometryIf operateSurface(PgElementSet geom) throws EvaluationException {
		return operatePoints(geom);
	}

	@Override
	public PgGeometryIf operatePoints(PgPointSet geom) throws EvaluationException {

		PgVectorField field = new PgVectorField(3,PgVectorField.VERTEX_BASED);
		field.setGeometry(geom);
		
		for(int i=0;i<geom.getNumVertices();++i)
		{
			PdVector vec = calcVector(geom.getVertex(i));
			field.setVector(i,vec);
		}
		geom.addVectorField(field);

		return field;
	}

	@Override
	public PgGeometryIf operateCurve(PgPolygonSet geom) throws EvaluationException {
		return operatePoints(geom);
	}
	
	public abstract PdVector calcVector(PdVector vec) throws EvaluationException;

	public PgVectorField[] operateAll(PgGeometryIf geom) throws EvaluationException, UnSuportedGeometryException {
		PgVectorField field = (PgVectorField) this.operate(geom);
		return new PgVectorField[] { field} ;
	}

	public double getLength() {
		return length;
	}

	public void setLength(double length) {
		this.length = length;
	}

}
