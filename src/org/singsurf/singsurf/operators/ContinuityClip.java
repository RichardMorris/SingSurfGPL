/*
Created 25-Apr-2006 - Richard Morris
*/
package org.singsurf.singsurf.operators;

import java.util.ArrayList;
import java.util.List;

import jv.geom.PgElementSet;
import jv.geom.PgPointSet;
import jv.geom.PgPolygonSet;
import jv.object.PsObject;
import jv.vecmath.PdVector;
import jv.vecmath.PiVector;

/**
 * A clipping algorithm which rejects cells if points are too far from each other.
 * 
 * @author Richard Morris
 */
public class ContinuityClip extends AbstractModifier {
	double maxDistSq;
	protected boolean goodVerts[];
	public ContinuityClip(double maxDist) {
		super();
		this.maxDistSq = maxDist*maxDist;
	}


	@Override
	public PgPointSet operatePoints(PgPointSet geom) {
		return geom;
	}

	boolean testElement(PgElementSet geom,int index) {
		PiVector ind = geom.getElement(index);
		for(int j=0;j<ind.getSize()-1;++j) {
			PdVector jVec = geom.getVertex(ind.getEntry(j));
			for(int k=j+1;k<ind.getSize();++k) {
				PdVector kVec = geom.getVertex(ind.getEntry(k));
				if(jVec.sqrDist(kVec)>maxDistSq) {
					return false;
				}
			}
		}
		return true;
	}

	@Override
	public PgElementSet operateSurface(PgElementSet geom) {
		
		for(int i=0;i<geom.getNumElements();++i) {
			boolean flag=testElement(geom,i);
			if(!flag) geom.setTagElement(i,PsObject.IS_DELETED);
		}
		geom.removeMarkedElements();
		return geom;
	}

	@Override
	public PgPolygonSet operateCurve(PgPolygonSet geom) {
		for(int i=0;i<geom.getNumPolygons();++i) {
			testCurve(geom,i);
		}
		geom.removeMarkedPolygons();
		return geom;
	}


	private void testCurve(PgPolygonSet geom, int polygonNum) {
		PiVector indicies = geom.getPolygon(polygonNum);
		int length = indicies.getSize();
		if(length==0) return;

		PdVector prev = geom.getVertex(indicies.getEntry(0));
		PdVector next;
		List<Integer> breakPoints = new ArrayList<Integer>();
		for(int i=1;i<indicies.getSize();++i) {
			next = geom.getVertex(indicies.getEntry(i));
			double sqrDist = prev.sqrDist(next);

            if(sqrDist>maxDistSq)
				breakPoints.add(i);
			prev = next;
		}
		if(breakPoints.isEmpty()) 
		    return;
//	    geom.setTagPolygon(polygonNum,PsObject.IS_DELETED);
//        if(!breakPoints.isEmpty()) return;
//        System.out.println("wierd");

        Integer[] bps=new Integer[breakPoints.size()];
		bps =breakPoints.toArray(bps);
		//Integer[] lengths=new Integer[breakPoints.size()+1];
		
/*		lengths[0] = bps[0];
		for(int i=1;i<bps.length;++i)
			lengths[i]=bps[i]-bps[i-1];
		lengths[bps.length]=length-bps[bps.length-1];
*/		
		// Now build the new polygons
		
		PiVector[] splits = new PiVector[breakPoints.size()+1];

		splits[0] = new PiVector(bps[0]);
		for(int j=0;j<bps[0];++j) 
			splits[0].setEntry(j, indicies.getEntry(j));
			
		for(int i=1;i<breakPoints.size();++i) {
			splits[i]=new PiVector(bps[i]-bps[i-1]);
			for(int j=bps[i-1];j<bps[i];++j)
				splits[i].setEntry(j-bps[i-1],indicies.getEntry(j));
		}

		splits[breakPoints.size()] = new PiVector(length-bps[bps.length-1]);
		for(int j=bps[bps.length-1];j<length;++j)
			splits[breakPoints.size()].setEntry(j-bps[bps.length-1],j);
			
		for(int i=0; i<splits.length;++i) {
			if(splits[i].getSize()>1)
				geom.addPolygon(splits[i]);
		}
		geom.setTagPolygon(polygonNum,PsObject.IS_DELETED);
	}


	public double getMaxDist() {
		return Math.sqrt(maxDistSq);
	}


	public void setMaxDist(double maxDist) {
		this.maxDistSq = maxDist*maxDist;
	}

	
}
