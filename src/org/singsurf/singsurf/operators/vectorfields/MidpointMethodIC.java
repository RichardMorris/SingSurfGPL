package org.singsurf.singsurf.operators.vectorfields;

import org.singsurf.singsurf.jepwrapper.EvaluationException;

import jv.geom.PgPolygonSet;
import jv.vecmath.PdVector;
import jv.vecmath.PiVector;

public class MidpointMethodIC extends AbstractIntergralCurve {

	final AbstractVectorField vecField;
	final int numPoints;
	
	public MidpointMethodIC(AbstractVectorField vecField, int numPoints) {
		super();
		this.vecField = vecField;
		this.numPoints = numPoints;
	}



	@Override
	protected PiVector generateCurve(PdVector vertex, PgPolygonSet curves) throws EvaluationException {

		PiVector curve = new PiVector();
		int index = curves.addVertex(vertex);
		curve.addEntry(index);
		PdVector cur = (PdVector) vertex.clone();//    new PdVector(vertex);
		
		for(int i=0;i<numPoints;++i) {
			PdVector vec = vecField.calcVector(cur);
			PdVector mid = PdVector.blendNew(1, cur, 0.5, vec);
			PdVector midvec = vecField.calcVector(mid);
			cur.add(midvec);
			index = curves.addVertex(cur);
			curve.addEntry(index);
		}	
		return curve;
	}

}
