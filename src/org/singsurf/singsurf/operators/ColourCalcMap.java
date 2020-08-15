/*
Created 25-Apr-2006 - Richard Morris
*/
package org.singsurf.singsurf.operators;

import java.awt.Color;
import java.util.Arrays;

import org.singsurf.singsurf.calculators.Evaluator;

import org.singsurf.singsurf.jepwrapper.EvaluationException;

import jv.geom.PgElementSet;
import jv.geom.PgPointSet;
import jv.geom.PgPolygonSet;
import jv.vecmath.PdVector;

/**
 * @author Richard Morris
 *
 */
public class ColourCalcMap extends AbstractModifier {
	Evaluator calc;

	public ColourCalcMap(Evaluator calc) {
		this.calc = calc;
	}
	
	private float clipCol(double val) { 
		if(val<0) val = 0; 
		else if(val>1.0) val = 1.0; 
		return (float) val; 
	}
	@Override
	public PgPointSet operatePoints(PgPointSet geom) throws EvaluationException {
		TextureRange texrng =
				paramsFromTexture
				? TextureRange.findFrom(geom)
				: TextureRange.UnitRange;
		
		System.out.println(texrng);
		
		geom.assureVertexColors();
		//Color cols[] = geom.getVertexColors();
		for(int i=0;i<geom.getNumVertices();++i)
		{
			PdVector vec = useTextureCoordinates || paramsFromTexture
				? texrng.scale(geom.getVertexTexture(i))
				: geom.getVertex(i);
			//Color col = geom.getVertexColor(i);
			double topRes[] = calc.evalTop(vec.getEntries());
			Color cols = new Color(clipCol(topRes[0]),clipCol(topRes[1]),clipCol(topRes[2]));

			geom.setVertexColor(i,cols);
			if(i%1000 == 0) {
				System.out.println(""+i+" tex ("+vec.getFirstEntry()+","+vec.getLastEntry()+")"
						+ " res "+Arrays.toString(topRes));
			}
		}
		return geom;
	}
	@Override
	public PgPolygonSet operateCurve(PgPolygonSet geom) throws EvaluationException {
		return (PgPolygonSet) operatePoints(geom);
	}
	@Override
	public PgElementSet operateSurface(PgElementSet geom) throws EvaluationException {
		return (PgElementSet) operatePoints(geom);
	}


}
