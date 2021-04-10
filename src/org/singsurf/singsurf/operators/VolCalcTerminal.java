package org.singsurf.singsurf.operators;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.singsurf.singsurf.jepwrapper.EvaluationException;

import jv.geom.PgElementSet;
import jv.geom.PgPointSet;
import jv.geom.PgPolygonSet;
import jv.vecmath.PdVector;
import jv.vecmath.PiVector;

public class VolCalcTerminal extends AbstractTerminal {
	
	enum Dir { X, Y , Z };
	Dir dir = Dir.X;

	@Override
	public Object operateSurface(PgElementSet geom) throws EvaluationException {
		
		double sumOverAllFaces=0;
		
		geom.makeElementNormals();
		for(int i=0;i<geom.getNumElements();++i) {
			
			sumOverAllFaces += operateTriangle(geom,i);
			
		}

		return sumOverAllFaces;
	}

	private double operateTriangle(PgElementSet geom,int i) {
		PiVector element = geom.getElement(i);
		PdVector normal = geom.getElementNormal(i);
		if(element.getSize()!=3) {
			return 0;
		}
		PdVector A = geom.getVertex(element.getEntry(0));
		PdVector B = geom.getVertex(element.getEntry(1));
		PdVector C = geom.getVertex(element.getEntry(2));
		System.out.printf("A (%6.2f,%6.2f,%6.2f) B (%6.2f,%6.2f,%6.2f) C (%6.2f,%6.2f,%6.2f)\n",
				A.getFirstEntry(),A.getEntry(1),A.getLastEntry(),
				B.getFirstEntry(),B.getEntry(1),B.getLastEntry(),
				C.getFirstEntry(),C.getEntry(1),C.getLastEntry()
				);
		PdVector AB = PdVector.subNew(B, A);
		PdVector AC = PdVector.subNew(C, A);
		PdVector norm = PdVector.crossNew(AB, AC);
		double len = norm.length();
		double area = Math.abs(len/2);
		
		
		norm.normalize();
		if(A.dot(norm)<0)
			norm.invert();
		// 
		double fa = A.getFirstEntry() * norm.getFirstEntry();
		double fb = B.getFirstEntry() * norm.getFirstEntry();
		double fc = C.getFirstEntry() * norm.getFirstEntry();
		
		double res = area * (fa + fb + fc) / 3;
		return res;
	}

	@Override
	public Object operatePoints(PgPointSet geom) throws EvaluationException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object operateCurve(PgPolygonSet geom) throws EvaluationException {
		// TODO Auto-generated method stub
		return null;
	}

}
