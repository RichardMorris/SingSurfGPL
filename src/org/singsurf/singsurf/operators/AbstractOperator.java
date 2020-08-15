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
 * Abstract base class for all geometry operations. Handles redirection to sub types of PgGeometryIf.
 * @author Richard Morris
 *
 */
public abstract class AbstractOperator {

	protected boolean useTextureCoordinates=false; 
	protected boolean paramsFromTexture=false;
	public void setUseTextureCoordinates(boolean state) {
		useTextureCoordinates = state;
	}
	
	public void setParamsFromTexture(boolean state) {
		paramsFromTexture = state;
	}

	TextureRange texrng;
	
	public void findTextureRange(PgGeometryIf geom) throws EvaluationException {
		texrng = paramsFromTexture
			? TextureRange.findFrom(geom)
			: TextureRange.UnitRange;
	}

	/**
	 * Gets either the vertex or the coord from texture 
	 * @param geom
	 * @param i
	 * @return
	 */
	public PdVector getVertexOrTexture(PgPointSet geom, int i) {
		PdVector vec = useTextureCoordinates || paramsFromTexture
				? texrng.scale(geom.getVertexTexture(i))
				: geom.getVertex(i);
		return vec;
	}

	public PgGeometryIf operate(PgGeometryIf geom) throws UnSuportedGeometryException, EvaluationException
	{
		findTextureRange(geom);
		
		if(geom instanceof PgElementSet)
			return operateSurface((PgElementSet) geom);
		if(geom instanceof PgPolygonSet)
			return operateCurve((PgPolygonSet) geom);
		if(geom instanceof PgPointSet)
			return operatePoints((PgPointSet) geom);
		throw new UnSuportedGeometryException("Bad geometry type: "+geom.getClass().getName());
	}
	
	abstract public PgGeometryIf operateSurface(PgElementSet geom) throws EvaluationException;
	abstract public PgGeometryIf operatePoints(PgPointSet geom) throws EvaluationException;
	abstract public PgGeometryIf operateCurve(PgPolygonSet geom) throws EvaluationException;

}
