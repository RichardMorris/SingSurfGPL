/*
Created 25-Apr-2006 - Richard Morris
*/
package org.singsurf.singsurf.operators;

import java.util.Arrays;
import java.util.Hashtable;

import jv.geom.PgElementSet;
import jv.geom.PgPointSet;
import jv.geom.PgPolygonSet;
import jv.project.PgGeometryIf;
import jv.vecmath.PdVector;
import jv.vecmath.PiVector;

import org.singsurf.singsurf.jepwrapper.EvaluationException;

/**
 * Abstract base class for intersection methods.
 * @author Richard Morris
 */
public abstract class AbstractIntersect extends AbstractOperator {
	protected static final boolean PRINTDEBUG=false;
	protected int    newIndices[];
	protected boolean    goodVerts[];
	protected int dim;
	protected PgGeometryIf inGeom,outGeom;
	protected Hashtable<Pair, Integer> pairSolutions=new Hashtable<Pair, Integer>();
	
	/**
	 * @throws EvaluationException  
	 */
	protected void setup(PgGeometryIf geom) throws EvaluationException
	{
		this.inGeom = geom;
		dim = geom.getDimOfVertices();
		findTextureRange(geom);
	}
	
	protected void tidyUp()
	{
		pairSolutions.clear();
		inGeom=null; 
	}
	
	protected PiVector[] intersectFace(PiVector element) throws EvaluationException
	{
		int len = element.getSize();
		int crossingPoints[] = new int[len];
		int crossCount=0;
		if(PRINTDEBUG)
		{	System.out.println("intersectFace:");
			for(int i=0;i<element.getSize();++i)
			{
				int a = element.getEntry(i);
				PdVector A = inGeom.getVertex(a);
				System.out.println("\t"+i+","+a+"\t"+A.getEntry(0)+","+A.getEntry(1)+","+A.getEntry(2));
			}
			System.out.println("Sols:");
		}
		for(int i=0;i<element.getSize();++i)
		{
			int a = element.getEntry(i);
			int b = element.getEntry((i+1)%element.getSize());
			if(testIntersection(a,b))
			{	
				int crossing = findIntersection(a,b);
				if(crossing<0) continue; // May not have converged 
				//elementSwitch[crossCount]=i-1;
				crossingPoints[crossCount]=crossing;
				++crossCount;
				if(PRINTDEBUG){
					PdVector C = outGeom.getVertex(crossing);
					System.out.println("\t"+i+","+crossing+"\t"+C.getEntry(0)+","+C.getEntry(1)+","+C.getEntry(2));
				}
			}
		}
		if(crossCount==0) return new PiVector[0];
		if(crossCount==2)
		{
			PiVector poly = new PiVector(crossingPoints[0],crossingPoints[1]);
			return new PiVector[]{poly};
		}
		int res[]=new int[crossCount];
		for(int i=0;i<crossCount;++i) {
			res[i]=crossingPoints[i];
			if(res[i]<0) {
				System.out.println("Bad index in face intersect "+res[i]);
			}
		}
		PiVector poly = new PiVector(res);
		return new PiVector[]{poly};
	}

	protected void intersectPolygon(PiVector element) throws EvaluationException
	{
		int len = element.getSize();
		int crossingPoints[] = new int[len];
		int crossCount=0;
		for(int i=0;i<element.getSize();++i)
		{
			int a = element.getEntry(i);
			int b = element.getEntry((i+1)%element.getSize());
			if(testIntersection(a,b))
			{	
				int crossing = findIntersection(a,b);
				//elementSwitch[crossCount]=i-1;
				crossingPoints[crossCount]=crossing;
				++crossCount;
			}
		}
	}

	@Override
	public PgPointSet operatePoints(PgPointSet geom) {
		return null;
	}

	@Override
	public PgPointSet operateCurve(PgPolygonSet geom) throws EvaluationException {
		setup(geom);
		outGeom = new PgPointSet(dim);
		for(int i=0;i<geom.getNumPolygons();++i)
		{
			intersectPolygon(geom.getPolygon(i));
		}
		tidyUp();
		return (PgPointSet) outGeom;
	}

	@Override
	public PgPolygonSet operateSurface(PgElementSet geom) throws EvaluationException {
		setup(geom);
		outGeom = new PgPolygonSet(dim);
		for(int i=0;i<geom.getNumElements();++i)
		{
			PiVector lines[] = intersectFace(geom.getElement(i));
			for(int j=0;j<lines.length;++j)
			{
				for(int ind:lines[j].getEntries()) {
					if(ind<0) {
						System.out.println("Bad index"+Arrays.toString(lines[j].getEntries()));
					}
				}
				outGeom.addPolygon(lines[j]);
			}
		}
		tidyUp();
		return (PgPolygonSet) outGeom;
	}

	/** Tests whether there is an intersection between two points in the original geometry.
	 * 
	 * @param a index of first point
	 * @param b index of second point
	 * @return true if there is an intersection in [a,b)
	 * @throws EvaluationException 
	 */
	public boolean testIntersection(int a,int b) throws EvaluationException
	{
		if(this.paramsFromTexture) 
			return testIntersection(
					texrng.scale(((PgPointSet)inGeom).getVertexTexture(a)),
					texrng.scale(((PgPointSet)inGeom).getVertexTexture(b))
					);
		return testIntersection(inGeom.getVertex(a),inGeom.getVertex(b));
	}
	/**
	 * Tests whether there is an intersection between two points.
	 * @param A
	 * @param B
	 * @return true if there is an intersection
	 * @throws EvaluationException 
	 */
	public abstract boolean testIntersection(PdVector A,PdVector B) throws EvaluationException;
	
	/**
	 * Finds the intersection between two points on the original geometry.
	 * A hashtable is used to store previously found crossings.
	 * If no solution is found calculateIntersection will be called and 
	 * the point will be added to the result geometry.
	 * @param a index of first point in original geometry.
	 * @param b index of second point in original geometry.
	 * @return index of point in result geometry or -1 on error
	 * @throws EvaluationException 
	 */
	protected int findIntersection(int a,int b) throws EvaluationException {
		Pair p = new Pair(a,b);
		Integer index = pairSolutions.get(p);
		if(index!=null) return index;
		PdVector C = calculateIntersection(a,b);
		if(C==null)
		{
			pairSolutions.put(p,-1);
			return -1;
		}
		index = outGeom.addVertex(C);
		pairSolutions.put(p,index);
		return index;
	}

	/**
	 * Calculates the intersection between two point on the original geometry.
	 * @param a index of first point
	 * @param b index of second point
	 * @return the intersection point or null on error
	 * @throws EvaluationException 
	 */
	protected PdVector calculateIntersection(int a,int b) throws EvaluationException
	{
		if(this.paramsFromTexture) 
			return calculateIntersectionWithTexture(inGeom.getVertex(a),inGeom.getVertex(b),
					texrng.scale(((PgPointSet)inGeom).getVertexTexture(a)),
					texrng.scale(((PgPointSet)inGeom).getVertexTexture(b))
					);
			
		return calculateIntersection(inGeom.getVertex(a),inGeom.getVertex(b));
	}
	
	
	protected abstract PdVector calculateIntersectionWithTexture(PdVector vertex, PdVector vertex2,
			PdVector vertexTexture, PdVector vertexTexture2) throws EvaluationException;

	/**
	 * Calculates the crossing value from
	 * @param A input point
	 * @param B input point
	 * @return the intersection point
	 * @throws EvaluationException 
	 */
	protected abstract PdVector calculateIntersection(PdVector A,PdVector B) throws EvaluationException;
}
