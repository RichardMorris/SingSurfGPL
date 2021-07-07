/*
Created 25-Apr-2006 - Richard Morris
*/
package org.singsurf.singsurf.operators;

import jv.geom.PgElementSet;
import jv.geom.PgPointSet;
import jv.geom.PgPolygonSet;
import jv.vecmath.PdVector;

import org.singsurf.singsurf.jepwrapper.EvaluationException;

public abstract class SimpleMap extends AbstractModifier {

    public abstract boolean doNormals();
    	
	@Override
	public PgElementSet operateSurface(PgElementSet geom) throws EvaluationException {
	    if(doNormals() && geom.hasVertexNormals() && !paramsFromTexture) {
	    	return operatePointsAndNormals(geom);
	    } else {
	    	return (PgElementSet) operatePoints(geom);
	    }
	}

	private PgElementSet operatePointsAndNormals(PgElementSet geom) throws EvaluationException {
		for(int i=0;i<geom.getNumVertices();++i)
		{
			PdVector[] vecNorm = map(geom.getVertex(i),geom.getVertexNormal(i));
			geom.setVertex(i,vecNorm[0]);
			geom.setVertexNormal(i,vecNorm[1]);
		}
		return geom;
	}

	@Override
	public PgPolygonSet operateCurve(PgPolygonSet geom) throws EvaluationException {
		return (PgPolygonSet) operatePoints(geom);
	}

	@Override
	public PgPointSet operatePoints(PgPointSet geom) throws EvaluationException {
		TextureRange texrng =
				paramsFromTexture
				? TextureRange.findFrom(geom)
				: TextureRange.UnitRange;

		for(int i=0;i<geom.getNumVertices();++i)
		{
			PdVector vec = useTextureCoordinates || paramsFromTexture
					? texrng.scale(geom.getVertexTexture(i))
					: geom.getVertex(i);

			PdVector res = map(vec);
			geom.setVertex(i,res);
		}
		return geom;
	}

	public abstract PdVector map(PdVector vec) throws EvaluationException;

	public abstract PdVector[] map(PdVector vertex, PdVector vertexNormal)  throws EvaluationException;

}
