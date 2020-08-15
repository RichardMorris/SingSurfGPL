/*
Created 25-Apr-2006 - Richard Morris
 */
package org.singsurf.singsurf.operators;

import java.awt.Color;
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
 * More advanced clipping algorithm
 * Finds the exact intersection of an edge with the clipping function,
 * creates new polygons as needed.
 */
public abstract class IntersectionClip extends SimpleClip {

	private int nItterations;
	private int dim = 3;
	// PgGeometryIf geom;
	List<PiVector> newFaces = new ArrayList<PiVector>();
	List<Color> newColours = new ArrayList<Color>();
	Map<IntPair, Integer> newVerts = new HashMap<IntPair, Integer>();
	
	

	public IntersectionClip(int nItterations) {
		super();
		this.nItterations = nItterations;
	}

	@SuppressWarnings("unused")
	private IntersectionClip() {
	}

	@Override
	public PgPointSet operatePoints(PgPointSet geom) throws EvaluationException {
		boolean allGood = findGoodVerts(geom);
		if (allGood)
			return geom;
		geom.removeMarkedVertices();
		return geom;
	}

	@Override
	public PgElementSet operateSurface(PgElementSet geom) throws EvaluationException {
		// this.geom = geom;
		newFaces.clear();
		newColours.clear();
		newVerts.clear();

		boolean hasElementColors = geom.hasElementColors();
//		System.out.println("Vert cols " + geom.hasVertexColors() + " ele cols " + hasElementColors);

		boolean allGood = findGoodVerts(geom);
		if (allGood)
			return geom;

		for (int i = 0; i < geom.getNumElements(); ++i) {
			int nGood = 0;
			PiVector face = geom.getElement(i);
			Color elementColor = geom.getElementColor(i);

			int size = face.getSize();
			boolean goodFaceVerts[] = new boolean[size];
			for (int j = 0; j < size; ++j) {
				int index = face.getEntry(j);
				if (goodVerts[index]) {
					++nGood;
					goodFaceVerts[j] = true;
				} else {
					goodFaceVerts[j] = false;
				}
			}
			if (nGood == size || nGood == 0)
				continue;

			geom.setTagElement(i, PsObject.IS_DELETED);
			int firstFlip = -1;
			for (int j = 0; j < size; ++j) {
				if (!goodFaceVerts[j] && goodFaceVerts[(j + 1) % size]) {
					firstFlip = j;
					break;
				}
			}
			int pos = firstFlip;
			int next = (pos + 1) % size;
			do {
				PiVector nface = new PiVector();
				int index = this.calcIntersectionIndex(geom, face.getEntry(pos), face.getEntry(next));
				nface.addEntry(index);
				do {
					pos = next;
					next = (pos + 1) % size;
					nface.addEntry(face.getEntry(pos));
				} while (goodFaceVerts[next]);
				index = this.calcIntersectionIndex(geom, face.getEntry(pos), face.getEntry(next));
				nface.addEntry(index);
				newFaces.add(nface);
				if(hasElementColors)
					newColours.add(new Color(elementColor.getRGB()));

				while (!goodFaceVerts[next]) {
					pos = next;
					next = (pos + 1) % size;
				}
			} while (pos != firstFlip);
		}
		int numElements = geom.getNumElements();
		int newEles = newFaces.size();

		geom.setNumElements(numElements+newEles);
		int curEleNum = numElements;
		for (PiVector nface : newFaces) {
			geom.setElement(curEleNum, nface);

			if(hasElementColors) {
				Color c = newColours.remove(0);
				geom.setElementColor(curEleNum, c);
			}
			++curEleNum;
		}
		for (int i = 0; i < goodVerts.length; ++i) {
			if (!goodVerts[i])
				geom.setTagVertex(i, PsObject.IS_DELETED);
		}
		geom.removeMarkedVertices();
		// geom.removeMarkedElements();

//		System.out.println("Vert cols " + geom.hasVertexColors() + " " + hasElementColors);

		return geom;
	}

	@Override
	public PgPolygonSet operateCurve(PgPolygonSet geom) throws EvaluationException {
		// this.geom = geom;
		newFaces.clear();
		newVerts.clear();

		boolean allGood = findGoodVerts(geom);
		if (allGood)
			return geom;

		for (int i = 0; i < geom.getNumPolygons(); ++i) {
			PiVector eles = geom.getPolygon(i);
			int nGood = 0;
			int size = eles.getSize();
			for (int j = 0; j < size; ++j) {
				int index = eles.getEntry(j);
				if (goodVerts[index])
					++nGood;

			}
			if (nGood == size || nGood == 0)
				continue;
			geom.setTagPolygon(i, PsObject.IS_DELETED);

			PiVector newEles = new PiVector();
			for (int j = 0; j < size - 1; ++j) {
				int a = eles.getEntry(j);
				int b = eles.getEntry(j + 1);
				if (goodVerts[a]) {
					newEles.addEntry(a);

					if (!goodVerts[b]) {
						int index = calcIntersectionIndex(geom, a, b);
						newEles.addEntry(index);
						newFaces.add(newEles);
						newEles = null;
					}
				} else {
					if (goodVerts[b]) {
						int index = calcIntersectionIndex(geom, a, b);
						newEles = new PiVector();
						newEles.addEntry(index);
					}
				}
			}
			if (newEles != null) {
				int a = eles.getEntry(size - 1);
				if (goodVerts[a])
					newEles.addEntry(a);
				newFaces.add(newEles);
			}
		}

		for (PiVector nface : newFaces)
			geom.addPolygon(nface);
		for (int i = 0; i < goodVerts.length; ++i) {
			if (!goodVerts[i])
				geom.setTagVertex(i, PsObject.IS_DELETED);
		}
		geom.removeMarkedVertices();
		geom.removeMarkedPolygons();
		return geom;
	}

	static class IntPair {
		int a,b;

		public IntPair(int a, int b) {
			if(a > b) {
				this.a = b;
				this.b = a;				
			} else {
				this.a = a;
				this.b = b;
			}
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
	}
	
	protected int calcIntersectionIndex(PgPointSet geom, int a, int b) throws EvaluationException {
		IntPair pair = new IntPair(a,b);
		Integer index = newVerts.get(pair);
		if (index != null)
			return index;
		
		PdVector A = geom.getVertex(a);
		PdVector B = geom.getVertex(b);
		double valA = findValue(A);
		double valB = findValue(B);
		if (valA == 0.0)
			return a;
		if (valB == 0.0)
			return b;
		PdVector C = calculateIntersection(A, valA, B, valB);
		index = geom.addVertex(C);
		if (index < 0) {
			System.out.println("index is neg");
		}
		newVerts.put(pair, index);
		if(geom.hasVertexColors()) {
			double dist = A.dist(C);
			double basedist = A.dist(B);
			Color colorA = geom.getVertexColor(a);
			Color colorB = geom.getVertexColor(b);
			float lambda = (float) (basedist < 1e-9 ? 0.5 : dist/basedist);
			float mu  = 1 - lambda;
			float[] Acols = new float[3];
			float[] Bcols = new float[3];
			colorA.getColorComponents(Acols);
			colorB.getColorComponents(Bcols);
			float[] Ccols = new float[] { mu * Acols[0] + lambda * Bcols[0],  mu * Acols[1] + lambda * Bcols[1],  mu * Acols[2] + lambda * Bcols[2] };
			Color colorC = new Color( Ccols[0],Ccols[1],Ccols[2]);
			geom.setVertexColor(index, colorC);
		}
		if(geom.hasVertexTextures()) {
			double dist = A.dist(C);
			double basedist = A.dist(B);
			PdVector texA = geom.getVertexTexture(a);
			PdVector texB = geom.getVertexTexture(b);
			float lambda = (float) (basedist < 1e-9 ? 0.5 : dist/basedist);
			float mu  = 1 - lambda;
			PdVector texC = new PdVector(2);
			texC.blend(mu, texA, lambda, texB);
			geom.setVertexTexture(index, texC);
		}
		
		return index;
	}

	/**
	 * Calculates intersection between two points, with given function values. Uses
	 * a linear interpolation between the points.
	 * 
	 * @param A
	 *                 first point
	 * @param aVal
	 *                 function value for first point
	 * @param B
	 *                 second point
	 * @param bVal
	 *                 function value for second point
	 * @return the intersection point or null if it cannot be found
	 * @throws EvaluationException
	 */
	protected PdVector calculateIntersection(PdVector A, double aVal, PdVector B, double bVal)
			throws EvaluationException {
		if (aVal == 0.0)
			return (PdVector) A.clone();
		if (bVal == 0.0)
			return null;
		if (aVal * bVal > 0.0)
			return null;
		PdVector C = new PdVector(dim);
		PdVector H, L;
		double cVal;
		if (aVal > 0.0) {
			H = A;
			L = B;
		} else {
			H = B;
			L = A;
			cVal = aVal;
			aVal = bVal;
			bVal = cVal;
		}
		for (int i = nItterations; i >= 0; --i) {
			double lambda = -bVal / (aVal - bVal);
			C.blend(lambda, H, 1 - lambda, L);
			if (i == 0)
				break;

			cVal = findValue(C);
			if (cVal > 0.0) {
				H = C;
				aVal = cVal;
			} else {
				L = C;
				bVal = cVal;
			}
			C = new PdVector(dim);
		}
		return C;
	}

	
	public abstract double findValue(PdVector c) throws EvaluationException;

	public int getnItterations() {
		return nItterations;
	}

	public void setnItterations(int nItterations) {
		this.nItterations = nItterations;
	}

}
