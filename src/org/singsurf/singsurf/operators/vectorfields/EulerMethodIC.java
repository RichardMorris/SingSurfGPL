package org.singsurf.singsurf.operators.vectorfields;

import org.singsurf.singsurf.jepwrapper.EvaluationException;

import jv.geom.PgPolygonSet;
import jv.vecmath.PdVector;
import jv.vecmath.PiVector;

public class EulerMethodIC extends AbstractIntergralCurve {

	final AbstractVectorField vecField;
	final int numPoints;
	
	public EulerMethodIC(AbstractVectorField vecField, int numPoints) {
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
			cur.add(vec);
			index = curves.addVertex(cur);
			curve.addEntry(index);
		}	
		return curve;
	}

}
