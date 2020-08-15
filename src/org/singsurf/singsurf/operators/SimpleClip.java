/*
Created 25-Apr-2006 - Richard Morris
*/
package org.singsurf.singsurf.operators;

import org.singsurf.singsurf.jepwrapper.EvaluationException;

import jv.geom.PgElementSet;
import jv.geom.PgPointSet;
import jv.geom.PgPolygonSet;
import jv.object.PsObject;
import jv.vecmath.PdVector;

/**
 * Clipping algorithm which simply prunes all vertices outside the surface.
 * No attempt to find intersection with surface. 
 */
public abstract class SimpleClip extends AbstractModifier {

	protected boolean goodVerts[];
	public SimpleClip() {
		super();
	}

	public boolean findGoodVerts(PgPointSet geom) throws EvaluationException
	{
		boolean allGood=true;
		int nVert=geom.getNumVertices();
		if(nVert==0)
			return true;
		goodVerts = new boolean[nVert];
		for(int i=0;i<nVert;++i)
		{
			goodVerts[i] = testClip(geom.getVertex(i));
			if(!goodVerts[i]) {
				allGood = false;
				geom.setTagVertex(i,PsObject.IS_DELETED);
			}
			
		}
		return allGood;
	}

	@Override
	public PgPointSet operatePoints(PgPointSet geom) throws EvaluationException {
		boolean allGood = findGoodVerts(geom);
		if(allGood) return geom;
		geom.removeMarkedVertices();
		return geom;
	}

	@Override
	public PgElementSet operateSurface(PgElementSet geom) throws EvaluationException {
		
		boolean allGood = findGoodVerts(geom);
		if(allGood) return geom;
		geom.removeMarkedVertices();
		return geom;
	}

	@Override
	public PgPolygonSet operateCurve(PgPolygonSet geom) throws EvaluationException {
		boolean allGood = findGoodVerts(geom);
		if(allGood) return geom;
		geom.removeMarkedVertices();
		return geom;
	}

	public abstract boolean testClip(PdVector vec) throws EvaluationException;
}
