package org.singsurf.singsurf.operators.vectorfields;

import org.singsurf.singsurf.operators.AbstractOperator;

import org.singsurf.singsurf.jepwrapper.EvaluationException;

import jv.geom.PgElementSet;
import jv.geom.PgPointSet;
import jv.geom.PgPolygonSet;
import jv.project.PgGeometryIf;
import jv.vecmath.PdVector;
import jv.vecmath.PiVector;

public abstract class AbstractIntergralCurve extends AbstractOperator {

	@Override
	public PgGeometryIf operateSurface(PgElementSet geom) throws EvaluationException {
		return operatePoints(geom);
	}

	@Override
	public PgGeometryIf operatePoints(PgPointSet geom) throws EvaluationException {
		
		PgPolygonSet curves = new PgPolygonSet(geom.getDimOfVertices());
		
		for(int i=0;i<geom.getNumVertices();++i)
		{
			PiVector poly = generateCurve(geom.getVertex(i),curves);
			curves.addPolygon(poly);
		}
		
		return curves;
	}

	protected abstract PiVector generateCurve(PdVector vertex, PgPolygonSet curves) throws EvaluationException;

	@Override
	public PgGeometryIf operateCurve(PgPolygonSet geom) throws EvaluationException {
		return operatePoints(geom);
	}

}
