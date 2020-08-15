/*
Created 25-Apr-2006 - Richard Morris
*/
package org.singsurf.singsurf.operators;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.singsurf.singsurf.jepwrapper.EvaluationException;

import jv.geom.PgElementSet;
import jv.geom.PgPointSet;
import jv.geom.PgPolygonSet;
import jv.object.PsObject;
import jv.vecmath.PdVector;
import jv.vecmath.PiVector;

/**
 * Clipping algorithm which simply prunes all vertices outside the surface.
 * No attempt to find intersection with surface. 
 */
public class Split4D extends AbstractModifier {

	List<PiVector> newFaces = new ArrayList<PiVector>();
	Map<IntPair, Integer> posSols = new HashMap<IntPair, Integer>();
	Map<IntPair, Integer> negSols = new HashMap<IntPair, Integer>();
	double tolerance = 0.01;

	enum Wtype { POS(true,true), SMALLPOS(false,true), SMALLNEG(false,false), NEG(true,false);
		boolean keep;
		boolean positive;
		private Wtype(boolean keep, boolean positive) {
			this.keep = keep;
			this.positive = positive;
		}
		public boolean isKeep() {
			return keep;
		}
		public boolean isPositive() {
			return positive;
		}		
	}
	
	protected Wtype goodVerts[];

	public static class IntPair {
		int a,b;

		public IntPair(int a, int b) {
			super();
			this.a = a;
			this.b = b;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + a;
			result = prime * result + b;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			IntPair other = (IntPair) obj;
			if (a != other.a)
				return false;
			if (b != other.b)
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "[" + a + "," + b + "]";
		}
		
	}

	public Split4D() {
		super();
	}

	public Wtype isGood(PdVector v) {
		double w = v.getEntry(3);
		if(w > tolerance) return Wtype.POS;
		if(w < -tolerance) return Wtype.NEG;
		if(w>=0) return Wtype.SMALLPOS;
		return Wtype.SMALLNEG;
	}
	
	
	
	public void findGoodVerts(PgPointSet geom) throws EvaluationException
	{
		int nVert=geom.getNumVertices();
		if(nVert==0)
			return;
		goodVerts = new Wtype[nVert];
		for(int i=0;i<nVert;++i)
		{
			goodVerts[i] = isGood(geom.getVertex(i));
			if(!goodVerts[i].isKeep()) {
//				System.out.printf("Del vert %d%n",i);
				geom.setTagVertex(i,PsObject.IS_DELETED);
			}
		}
	}

	@Override
	public PgPointSet operatePoints(PgPointSet geom) throws EvaluationException {
		findGoodVerts(geom);
		geom.removeMarkedVertices();
		return geom;
	}

	@Override
	public PgElementSet operateSurface(PgElementSet geom) throws EvaluationException {
		newFaces.clear();
		posSols.clear();
		negSols.clear();
		
		findGoodVerts(geom);
		
		final int numElements = geom.getNumElements();
		for (int i = 0; i < numElements; ++i) {
			PiVector face = geom.getElement(i);
			int a = face.getFirstEntry();
			int b= face.getEntry(1);
			int c = face.getEntry(2);
			
			if(goodVerts[a]==Wtype.POS && goodVerts[b]==Wtype.POS && goodVerts[c]==Wtype.POS) {
				// keep
			} else if(goodVerts[a]==Wtype.NEG && goodVerts[b]==Wtype.NEG && goodVerts[c]==Wtype.NEG) {
				// keep
			} else {
				geom.setTagElement(i, PsObject.IS_DELETED);

				if(goodVerts[a]==Wtype.POS && goodVerts[b]==Wtype.POS ) { 
					splitTwoPos(geom,a,b,c);
				} else if(goodVerts[b]==Wtype.POS && goodVerts[c]==Wtype.POS ) {
					splitTwoPos(geom,b,c,a);
				} else if(goodVerts[a]==Wtype.POS && goodVerts[c]==Wtype.POS ) {
					splitTwoPos(geom,c,a,b);
				} else if(goodVerts[a]==Wtype.POS ) { 
					splitOnePos(geom,a,b,c);
				} else if(goodVerts[b]==Wtype.POS ) {
					splitOnePos(geom,b,c,a);
				} else if(goodVerts[c]==Wtype.POS ) {
					splitOnePos(geom,c,a,b);
				} 

				if(goodVerts[a]==Wtype.NEG && goodVerts[b]==Wtype.NEG ) { 
					splitTwoNeg(geom,a,b,c);
				} else if(goodVerts[b]==Wtype.NEG && goodVerts[c]==Wtype.NEG ) {
					splitTwoNeg(geom,b,c,a);
				} else if(goodVerts[a]==Wtype.NEG && goodVerts[c]==Wtype.NEG ) {
					splitTwoNeg(geom,c,a,b);
				} else if(goodVerts[a]==Wtype.NEG ) { 
					splitOneNeg(geom,a,b,c);
				} else if(goodVerts[b]==Wtype.NEG ) {
					splitOneNeg(geom,b,c,a);
				} else if(goodVerts[c]==Wtype.NEG ) {
					splitOneNeg(geom,c,a,b);
				} 
			}
			
/*		
		
			 if(goodVerts[a].isPositive() && goodVerts[b].isPositive()) {
				geom.setTagElement(i, PsObject.IS_DELETED);
				splitFace(geom,a,b,c);
			} else if(goodVerts[a].isPositive() && goodVerts[c].isPositive()) {
				geom.setTagElement(i, PsObject.IS_DELETED);
				splitFace(geom,c,a,b);
			} else if(goodVerts[b].isPositive() && goodVerts[c].isPositive()) {
				geom.setTagElement(i, PsObject.IS_DELETED);
				splitFace(geom,b,c,a);				
			} else if(!goodVerts[a].isPositive() && !goodVerts[b].isPositive()) {
				geom.setTagElement(i, PsObject.IS_DELETED);
				splitFace(geom,a,b,c);
			} else if(!goodVerts[a].isPositive() && !goodVerts[c].isPositive()) {
				geom.setTagElement(i, PsObject.IS_DELETED);
				splitFace(geom,c,a,b);
			} else if(!goodVerts[b].isPositive() && !goodVerts[c].isPositive()) {
				geom.setTagElement(i, PsObject.IS_DELETED);
				splitFace(geom,b,c,a);
			} else {
				PdVector A = geom.getVertex(a);
				PdVector B = geom.getVertex(b);
				PdVector C = geom.getVertex(c);

				System.out.printf("skipped A (%6.3f,%6.3f,%6.3f,%6.3f) B (%6.3f,%6.3f,%6.3f,%3f) C (%6.3f,%6.3f,%6.3f,%3f)%n",
				A.getEntry(0),A.getEntry(1),A.getEntry(2),A.getEntry(3),
				B.getEntry(0),B.getEntry(1),B.getEntry(2),B.getEntry(3),
				C.getEntry(0),C.getEntry(1),C.getEntry(2),C.getEntry(3)
				);
				
			}
			
		}
		*/
		}
		return geom;
	}
	
	
	
	/**
	 * Case with two positive vertices
	 * @param geom
	 * @param a A positive vertex
	 * @param b A positive vertex
	 * @param c A negative or bad vertex
	 */
	void splitTwoPos(PgElementSet geom,int a,int b,int c) {
		int d = posSolOnEdge(geom,a,c);
		int e = posSolOnEdge(geom,b,c);
		if(e>=0) {
			addEle(geom,a,b,e);
		}
		if(e>=0 && d>=0) {
			addEle(geom,a,e,d);
		}
		if(e<0 && d>=0) {
			addEle(geom,a,b,e);			
		}
	}
	
	/**
	 * Case with one positive vertices
	 * @param geom
	 * @param a A positive vertex
	 * @param b A positive vertex
	 * @param c A negative or bad vertex
	 */
	void splitOnePos(PgElementSet geom,int a,int b,int c) {
		int d = posSolOnEdge(geom,a,b);
		int e = posSolOnEdge(geom,a,c);
		if(e>=0 && d>=0) {
			addEle(geom,a,d,e);
		}		
	}
	
	/**
	 * Case with two negative vertices
	 * @param geom
	 * @param a negative vertex
	 * @param b negative vertex
	 * @param c positive or bad vertex
	 */
	void splitTwoNeg(PgElementSet geom,int a,int b,int c) {
		int d = negSolOnEdge(geom,a,c);
		int e = negSolOnEdge(geom,b,c);
		if(e>=0) {
			addEle(geom,a,b,e);
		}
		if(e>=0 && d>=0) {
			addEle(geom,a,e,d);
		}
		if(e<0 && d>=0) {
			addEle(geom,a,b,e);			
		}
		
	}
	
	/**
	 * Case with one negative vertices
	 * @param geom
	 * @param a negative vertex
	 * @param b positive or bad
	 * @param c positive or bad vertex
	 */
	void splitOneNeg(PgElementSet geom,int a,int b,int c) {
		int d = negSolOnEdge(geom,a,b);
		int e = negSolOnEdge(geom,a,c);
		if(e>=0 && d>=0) {
			addEle(geom,a,d,e);
		}		
	}
	
	
	private int posSolOnEdge(PgElementSet geom,int a,int b) {
		if(a>b) {
			return posSolOnEdge(geom,b,a);
		}
		IntPair pair = new IntPair(a,b);
		Integer index = posSols.get(pair);
		if(index!=null) return index;
		
		PdVector A = geom.getVertex(a);
		PdVector C = geom.getVertex(b);

		final double Aw = A.getEntry(3);
		final double Cw = C.getEntry(3);
		double lambda = (tolerance - Aw) /( Cw - Aw);
		
		if(lambda < 0 || lambda > 1) {
			posSols.put(pair, -1);
			return -1;
		}
		PdVector Dh = PdVector.blendNew(1-lambda, A, lambda, C);
		int ind = geom.addVertex(Dh);
		posSols.put(pair, ind);
		return ind;
	}
	

	private int negSolOnEdge(PgElementSet geom,int a,int b) {
		if(a>b) {
			return negSolOnEdge(geom,b,a);
		}
		IntPair pair = new IntPair(a,b);
		Integer index = negSols.get(pair);
		if(index!=null) return index;
		
		PdVector A = geom.getVertex(a);
		PdVector C = geom.getVertex(b);

		final double Aw = A.getEntry(3);
		final double Cw = C.getEntry(3);
		double lambda = (-tolerance - Aw) /( Cw - Aw);
		
		if(lambda < 0 || lambda > 1) {
			negSols.put(pair, -1);
			return -1;
		}
		PdVector Dh = PdVector.blendNew(1-lambda, A, lambda, C);
		int ind = geom.addVertex(Dh);
		negSols.put(pair, ind);
		return ind;
	}

	/**
	 * @param geom
	 * @param a
	 * @param ds
	 * @param es
	 */
	public void addEle(PgElementSet geom, int a, int b, int c) {
		PdVector A = geom.getVertex(a);
		PdVector B = geom.getVertex(b);
		PdVector C = geom.getVertex(c);
		if(A.getEntry(3) * B.getEntry(3) <=0.0 || A.getEntry(3) * C.getEntry(3) <=0.0 || B.getEntry(3) * C.getEntry(3) <=0.0 ) {
			System.out.printf("splitFace1 A (%6.3f,%6.3f,%6.3f,%6.3f) B (%6.3f,%6.3f,%6.3f,%6.3f) C (%6.3f,%6.3f,%6.3f,%6.3f)%n",
					A.getEntry(0),A.getEntry(1),A.getEntry(2),A.getEntry(3),
					B.getEntry(0),B.getEntry(1),B.getEntry(2),B.getEntry(3),
					C.getEntry(0),C.getEntry(1),C.getEntry(2),C.getEntry(3)	);			
		}
		
		geom.addElement(new PiVector(a,b,c));
	}

	@Override
	public PgPolygonSet operateCurve(PgPolygonSet geom) throws EvaluationException {
		findGoodVerts(geom);
		geom.removeMarkedVertices();
		return geom;
	}

}
