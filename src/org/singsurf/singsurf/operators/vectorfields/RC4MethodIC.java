package org.singsurf.singsurf.operators.vectorfields;

import org.singsurf.singsurf.jepwrapper.EvaluationException;

import jv.geom.PgPolygonSet;
import jv.vecmath.PdVector;
import jv.vecmath.PiVector;

public class RC4MethodIC extends AbstractIntergralCurve {

	final AbstractVectorField vecField;
	final int numPoints;
	
	public RC4MethodIC(AbstractVectorField vecField, int numPoints) {
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
			PdVector q1 = vecField.calcVector3D(cur);
			PdVector p2 = PdVector.blendNew(1, cur, 0.5, q1);
			PdVector q2 = vecField.calcVector3D(p2);
			PdVector p3 = PdVector.blendNew(1, cur, 0.5, q2);
			PdVector q3 = vecField.calcVector3D(p3);
			PdVector p4 = PdVector.blendNew(1, cur, 1, q2);
			PdVector q4 = vecField.calcVector3D(p4);
			
			PdVector q = PdVector.blendNew(1, q1, 2, q2, 2, q3, 1, q4);
			
			cur = PdVector.blendNew(1, cur, 1.0/6, q);
			index = curves.addVertex(cur);
			curve.addEntry(index);
		}	
		return curve;
	}

}
