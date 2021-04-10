/*
Created 25-Apr-2006 - Richard Morris
*/
package org.singsurf.singsurf.operators;

import jv.geom.PgElementSet;
import jv.geom.PgPointSet;
import jv.geom.PgPolygonSet;
import jv.project.PgGeometryIf;
import jv.vecmath.PdVector;

import org.singsurf.singsurf.jepwrapper.EvaluationException;

/**
 * Abstract base class for all terminal  operations which don't produce geometries.
 * @author Richard Morris
 *
 */
public abstract class AbstractTerminal {


	public Object operate(PgGeometryIf geom) throws UnSuportedGeometryException, EvaluationException
	{		
		if(geom instanceof PgElementSet)
			return operateSurface((PgElementSet) geom);
		if(geom instanceof PgPolygonSet)
			return operateCurve((PgPolygonSet) geom);
		if(geom instanceof PgPointSet)
			return operatePoints((PgPointSet) geom);
		throw new UnSuportedGeometryException("Bad geometry type: "+geom.getClass().getName());
	}
	
	abstract public Object operateSurface(PgElementSet geom) throws EvaluationException;
	abstract public Object operatePoints(PgPointSet geom) throws EvaluationException;
	abstract public Object operateCurve(PgPolygonSet geom) throws EvaluationException;

}
