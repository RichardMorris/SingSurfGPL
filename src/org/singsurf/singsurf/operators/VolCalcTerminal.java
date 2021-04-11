package org.singsurf.singsurf.operators;

import org.singsurf.singsurf.jepwrapper.EvaluationException;

import jv.geom.PgElementSet;
import jv.geom.PgPointSet;
import jv.geom.PgPolygonSet;
import jv.vecmath.PdVector;
import jv.vecmath.PiVector;

public class VolCalcTerminal extends AbstractTerminal {
	
	enum Dir { X, Y , Z };
	Dir dir = Dir.X;
	
	public static class VolInfo {
		public double area;
		public double volume;
		public PdVector centroid;
		double x;
		double y;
		double z;
		
		public VolInfo() {
			area = 0;
			volume = 0;
			centroid = new PdVector(3);
		}
	}

	@Override
	public Object operateSurface(PgElementSet geom) throws EvaluationException {
		
		VolInfo volinfo = new VolInfo();
		
		geom.makeElementNormals();

		for(int i=0;i<geom.getNumElements();++i) {
			
			operateTriangle(geom,i,volinfo);
			
		}
		volinfo.centroid.set(volinfo.x/ volinfo.volume, 
				volinfo.y/ volinfo.volume, 
				volinfo.z/ volinfo.volume);
		return volinfo;
	}

	private void operateTriangle(PgElementSet geom,int i,VolInfo volinfo) {
		PiVector element = geom.getElement(i);
		PdVector normal = geom.getElementNormal(i);
		if(element.getSize()!=3) {
			return;
		}
		PdVector A = geom.getVertex(element.getEntry(0));
		PdVector B = geom.getVertex(element.getEntry(1));
		PdVector C = geom.getVertex(element.getEntry(2));
		final double ax = A.getFirstEntry();
		final double bx = B.getFirstEntry();
		final double cx = C.getFirstEntry();
		final double ay = A.getEntry(1);
		final double by = B.getEntry(1);
		final double cy = C.getEntry(1);
		final double az = A.getLastEntry();
		final double bz = B.getLastEntry();
		final double cz = C.getLastEntry();
//		System.out.printf("A (%6.2f,%6.2f,%6.2f) B (%6.2f,%6.2f,%6.2f) C (%6.2f,%6.2f,%6.2f)\n",
//				ax,ay,az,
//				bx,by,bz,
//				cx,cy,cz
//				);
//		PdVector AB = PdVector.subNew(B, A);
//		PdVector AC = PdVector.subNew(C, A);
//		PdVector norm = PdVector.crossNew(AB, AC);
//		double len = norm.length();
//		double area = Math.abs(len/2);
		
		final double area = geom.getAreaOfElement(i);
		volinfo.area += area;
				// 
		final double nx = normal.getFirstEntry();
		final double ny = normal.getEntry(1);
		final double nz = normal.getLastEntry();
		double fa = ax * nx;
		double fb = bx * nx;
		double fc = cx * nx;
		
		volinfo.volume += area * (fa + fb + fc) / 3;
		double Cx = 1.0/12 * area * nx * (ax*ax + bx*bx + cx*cx + ax*bx + bx*cx + cx*ax);
		double Cy = 1.0/12 * area * ny * (ay*ay + by*by + cy*cy + ay*by + by*cy + cy*ay);
		double Cz = 1.0/12 * area * nz * (az*az + bz*bz + cz*cz + az*bz + bz*cz + cz*az);
		
		double fA = 0.5 * ax * ax * nx;
		double fB = 0.5 * bx * bx * nx;
		double fC = 0.5 * cx * cx * nx;
		double total = 1./3 * area * (fA + fB + fC);
//		System.out.printf("N (%5.2f,%5.2f,%5.2f), C (%6.3f,%6.3f,%6.3f) area %6.3f alt %6.3f%n",
//				nx,ny,nz,Cx,Cy,Cz,area,total);
		volinfo.x += Cx;
		volinfo.y += Cy;
		volinfo.z += Cz;
		
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
