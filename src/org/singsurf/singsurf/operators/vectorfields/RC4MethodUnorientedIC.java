package org.singsurf.singsurf.operators.vectorfields;

import org.singsurf.singsurf.jepwrapper.EvaluationException;

import jv.geom.PgPolygonSet;
import jv.vecmath.PdVector;
import jv.vecmath.PiVector;

public class RC4MethodUnorientedIC extends AbstractIntergralCurve {

	final AbstractVectorField vecField;
	final int numPoints;
	final double length;
	final AbstractVectorField startField;
	
	public RC4MethodUnorientedIC(AbstractVectorField vecField, 
			int numPoints, double length,AbstractVectorField start) {
		super();
		this.vecField = vecField;
		this.numPoints = numPoints;
		this.length = length;
		this.startField = start;
	}

	void fixDirection(PdVector vec, PdVector orig) {
		double len = vec.length();
		double mul = length/len;
		if(vec.dot(orig) < 0)
			mul = -mul;
		vec.multScalar(mul);
		
	}
	
	@Override
	protected PiVector generateCurve(PdVector vertex, PgPolygonSet curves) throws EvaluationException {

		PiVector curve = new PiVector();
		int index = curves.addVertex(vertex);
		curve.addEntry(index);
		PdVector cur = (PdVector) vertex.clone();//    new PdVector(vertex);
		
		PdVector lastVec = startField.calcVector(vertex);
//		lastVec.multScalar(-1);
		for(int i=0;i<numPoints;++i) {
			PdVector q1 = vecField.calcVector(cur);
			fixDirection(q1,lastVec);
			PdVector p2 = PdVector.blendNew(1, cur, 0.5, q1);
			PdVector q2 = vecField.calcVector(p2);
			fixDirection(q2,lastVec);
			PdVector p3 = PdVector.blendNew(1, cur, 0.5, q2);
			PdVector q3 = vecField.calcVector(p3);
			fixDirection(q3,lastVec);
			PdVector p4 = PdVector.blendNew(1, cur, 1, q2);
			PdVector q4 = vecField.calcVector(p4);
			fixDirection(q4,lastVec);
			PdVector q = PdVector.blendNew(1, q1, 2, q2, 2, q3, 1, q4);

			cur = PdVector.blendNew(1, cur, 1.0/6, q);
			index = curves.addVertex(cur);
			curve.addEntry(index);
			lastVec = q;
		}	
		return curve;
	}

}
