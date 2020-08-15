/*
Created 25-Apr-2006 - Richard Morris
*/
package org.singsurf.singsurf.operators;

import jv.geom.PgElementSet;
import jv.geom.PgPointSet;
import jv.geom.PgPolygonSet;
import jv.project.PgGeometryIf;

import org.singsurf.singsurf.jepwrapper.EvaluationException;


/**
 * Abstract base class for geometry operations which operate in place on a geometry.
 * Handles redirection to sub types of PgGeometryIf.
 * @author Richard Morris
 *
 */
public abstract class AbstractModifier {

	protected boolean useTextureCoordinates=false; 
	protected boolean paramsFromTexture=false;
	public void setUseTextureCoordinates(boolean state) {
		useTextureCoordinates = state;
	}
	
	public void setParamsFromTexture(boolean state) {
		paramsFromTexture = state;
	}
	
	public PgGeometryIf operate(PgGeometryIf geom) throws UnSuportedGeometryException, EvaluationException
	{
		try {
		if(geom instanceof PgElementSet)
			return operateSurface((PgElementSet) geom);
		if(geom instanceof PgPolygonSet)
			return operateCurve((PgPolygonSet) geom);
		if(geom instanceof PgPointSet)
			return operatePoints((PgPointSet) geom);
		} catch(NullPointerException e) {
			StackTraceElement[] st = e.getStackTrace();
			System.out.println("Null pointer ");
			for(int i=0;i<5;++i) {
				System.out.println(st[i]);
			}
		}
		throw new UnSuportedGeometryException("Bad geometry type: "+geom.getClass().getName());
	}
	
	abstract public PgElementSet operateSurface(PgElementSet geom) throws EvaluationException;
	abstract public PgPointSet operatePoints(PgPointSet geom) throws EvaluationException;
	abstract public PgPolygonSet operateCurve(PgPolygonSet geom) throws EvaluationException;

}
