/*
Created 25 Nov 2006 - Richard Morris
*/
package org.singsurf.singsurf.acurve;

import java.util.ArrayList;
import java.util.List;

import jv.geom.PgPolygonSet;
import jv.project.PgGeometryIf;
import jv.vecmath.PdVector;
import jv.vecmath.PiVector;

/**
 * Calculates the geometry for an algebraic curve.
 * @author Richard Morris
 *
 */
public class Plotter2D {
	protected PgGeometryIf outGeom;
	int minDepth;
	int maxDepth;
	int edgeDepth;
	Range2D range;
	
	/**
	 * @param minDepth
	 * @param maxDepth
	 * @param edgeDepth
	 */
	public Plotter2D(int minDepth, int maxDepth, int edgeDepth) {
		this.minDepth = minDepth;
		this.maxDepth = maxDepth;
		this.edgeDepth = edgeDepth;
	}

	public void setDepths(int minDepth, int maxDepth, int edgeDepth) {
		this.minDepth = minDepth;
		this.maxDepth = maxDepth;
		this.edgeDepth = edgeDepth;
	}

	/**
	 * Calculate the curve in a given range
	 * @param aa coefficients of the polynomial, aa[i][j] is coefficient of x^i y^j
	 * @param xlow
	 * @param xhigh
	 * @param ylow
	 * @param yhigh
	 * @return a geometry representing the output
	 * @throws AsurfException 
	 */
	public PgGeometryIf calculate(double aa[][],double xlow,double xhigh,double ylow,double yhigh) throws AsurfException {
		for(int i=0;i<aa.length;++i) {
			for(int j=0;j<aa[i].length;++j)
				System.out.print("\t"+aa[i][j]);
			System.out.println("");
		}
		range = new Range2D(xlow,xhigh,ylow,yhigh);
		outGeom = new PgPolygonSet(3);
		Face2D face = new Face2D(0,0,1);
		Bern2D bb = Bern2D.fromPolyCoeffs(aa, range);
		System.out.println(bb);
		if(bb.xord == 0 && bb.yord == 0) {
			System.out.println("Polynomial is a constant!");
		}
		else if(bb.xord == 0)
			solveYonly(face,bb);
		else if(bb.yord == 0)
			solveXonly(face,bb);
		else
			solveCourse(face,bb);
		return outGeom;
	}
	
	/** Case when yord = 0. */
	private void solveYonly(Face2D face, Bern2D bb) {
		List<Sol2D> sols = new ArrayList<Sol2D>();
		solveEdge(face.left(),bb.left(),sols);
		for(Sol2D sol:sols) {
			Sol2D sol2 = new Sol2D(sol.key,sol.denom,sol.y,sol.denom,sol.val);
			plotLine(sol,sol2);
		}
	}

	/** Case when xord = 0. */
	private void solveXonly(Face2D face, Bern2D bb) {
		List<Sol2D> sols = new ArrayList<Sol2D>();
		solveEdge(face.bottom(),bb.bottom(),sols);
		for(Sol2D sol:sols) {
			Sol2D sol2 = new Sol2D(sol.key,sol.x,sol.denom,sol.denom,sol.val);
			plotLine(sol,sol2);
		}
	}

	/**
	 * Solve the large faces
	 * @param face
	 * @param bern
	 * @throws AsurfException 
	 */
	void solveCourse(Face2D face,Bern2D bern) throws AsurfException {
//		System.out.println("solveCourse: "+face);
		if(face.denom >= minDepth) {
			solveFine(face,bern);
			return;
		}
		if(bern.allOneSign()!=0) return;
		Face2D.QuadFace qf = face.split();
		Bern2D.QuadBern qb = bern.reduce();
		solveCourse(qf.ll,qb.lb);
		solveCourse(qf.lh,qb.lt);
		solveCourse(qf.hl,qb.rb);
		solveCourse(qf.hh,qb.rt);
	}
	/**
	 * Solve fine faces
	 * @param face
	 * @param bb
	 * @throws AsurfException 
	 */
	void solveFine(Face2D face,Bern2D bb) throws AsurfException {
		//System.out.println("solveFine: "+face);
//		System.out.println(bb);
		if(bb.allOneSign()!=0)
			return;
		Bern2D xderiv = bb.diffX();
		Bern2D yderiv = bb.diffY();
		int xaos = xderiv.allOneSign(); 
		int yaos = yderiv.allOneSign(); 
		if( xaos!=0 && yaos!=0 ) {
			solveSimple(face,bb);
			return;
		}
		if(face.denom < maxDepth) {
			Bern2D.QuadBern qb = bb.reduce();
			Face2D.QuadFace qf = face.split();
			solveFine(qf.ll,qb.lb);
			solveFine(qf.lh,qb.lt);
			solveFine(qf.hl,qb.rb);
			solveFine(qf.hh,qb.rt);
			return;
		}	
		List<Sol2D> sols = solveEdges(face,bb);
		if(sols.size()==2) {
			plotLine(sols.get(0),sols.get(1));
			return;
		}
		if(sols.size() == 0 && (xaos!=0 || yaos!=0)) return;
		
		Bern2D.DerivBits[] db=new Bern2D.DerivBits[sols.size()];
		Bern2D.Tower tower = new Bern2D.Tower(xderiv,yderiv);
		int i=0;
		double x=0,y=0;
		for(Sol2D sol:sols) {
				System.out.println(sol);
				Face2D.FaceSol fs = face.whichEdge(sol);
				db[i] = tower.calc(fs);
				x+=fs.x; y+= fs.y;
				++i;
		}
		x /= sols.size(); y /= sols.size();
		if(sols.size()==4) {
			if(db[0].equals(db[1]) && db[2].equals(db[3]) && !db[0].equals(db[2]) ) { 
				plotLine(sols.get(0),sols.get(1));
				plotLine(sols.get(2),sols.get(3));
				return;
			}
			else if(db[0].equals(db[2]) && db[1].equals(db[3]) && !db[0].equals(db[1]) ) { 
				plotLine(sols.get(0), sols.get(2));
				plotLine(sols.get(1), sols.get(3));
				return;
			}
			else if(db[0].equals(db[3]) && db[1].equals(db[2]) && !db[0].equals(db[1]) ) { 
				plotLine(sols.get(0), sols.get(3));
				plotLine(sols.get(1), sols.get(2));
				return;
			}
		}
		System.out.println("Sols don't match: "+face);
		System.out.println(tower);
		for(int j=0;j<sols.size();++j) {
			Face2D.FaceSol fs = face.whichEdge(sols.get(j));
			PdVector Avec = sols.get(j).toPdVector(range);
			double ax = Avec.getFirstEntry();
			double ay = Avec.getEntry(1);
			System.out.println(sols.get(j)+"\t"+fs.x+" "+fs.y+"\t"+ax+" "+ay+"\t"+db[j]);
		}
			
		Sol2D sol = new Sol2D(face,x,y);
		for(int j=0;j<sols.size();++j)
			plotLine(sols.get(j),sol);
	}
	
	/**
	 * Solve a case where derivatives are all of one sign
	 * 
	 * @param face
	 * @param bb
	 */
	void solveSimple(Face2D face,Bern2D bb) {
// System.out.println("solveSimple: "+face);
//		System.out.println(bb);
		List<Sol2D> sols = solveEdges(face,bb);
		if(sols.size()==2)
			plotLine(sols.get(0),sols.get(1));
		else
			System.out.println(face.toString()+"\tSols = "+sols.size());
	}
	

	List<Sol2D> solveEdges(Face2D face,Bern2D bb) {
		List<Sol2D> sols = new ArrayList<Sol2D>(4);
		
		if(bb.getCoeffs(0,0) == 0.0)
			sols.add(new Sol2D(face.x,face.y,face.denom));
		if(bb.getCoeffs(bb.xord,0) == 0.0)
			sols.add(new Sol2D(face.x+1,face.y,face.denom));
		if(bb.getCoeffs(0,bb.yord) == 0.0)
			sols.add(new Sol2D(face.x,face.y+1,face.denom));
		if(bb.getCoeffs(bb.xord,bb.yord) == 0.0)
			sols.add(new Sol2D(face.x+1,face.y+1,face.denom));
		
		solveEdge(face.bottom(),bb.bottom(),sols);
		solveEdge(face.top(),bb.top(),sols);
		solveEdge(face.left(),bb.left(),sols);
		solveEdge(face.right(),bb.right(),sols);
		return sols;
	}
	
	void solveEdge(Edge2D edge, Bern1D bern, List<Sol2D> sols) {
//		System.out.println("solveEdge: "+edge);
//		System.out.println(bern);
		if (bern.getSign()!=0)
			return;
		if (!bern.allOneSignDeriv() && edge.denom < edgeDepth) {
			Edge2D.BinEdge be = edge.split();
			Bern1D.BinBern bb = bern.reduce();
			solveEdge(be.l, bb.l, sols);
			solveEdge(be.h, bb.r, sols);
			if(bb.r.coeff[0]==0.0)
				sols.add(new Sol2D(be.h));
			return;
		}
		double vall = bern.coeff[0];
		double valh = bern.coeff[bern.xord];
		if (vall == 0) {return;	}
		if (valh == 0) {return;	}
		if((vall >0 && valh >0 ) || (vall < 0 && valh <0) )	return;
		
		double rootm = 0.5;
		double valm;
		double rootl = 0.0;
		double rooth = 1.0;

		for (int depth = edge.denom; depth <= edgeDepth; depth *= 2) {
			rootm = (rootl + rooth) * 0.5;
			valm = bern.evaluate(rootm);
			if ((vall < 0) != (valm < 0))
				rooth = rootm;
			else {
				vall = valm;
				rootl = rootm;
			}
		}
		sols.add(new Sol2D(edge, rootm));
	}

	private void plotLine(Sol2D A, Sol2D B) {
		PdVector Avec = A.toPdVector(range);
		PdVector Bvec = B.toPdVector(range);
//		double ax = Avec.getFirstEntry();
//		double ay = Avec.getEntry(1);
//		double bx = Bvec.getFirstEntry();
//		double by = Bvec.getEntry(1);
//		System.out.println("Line:");
//		System.out.println("\t"+A+"\t"+ax+" "+ay);
//		System.out.println("\t"+B+"\t"+bx+" "+by);
		int indexA = outGeom.addVertex(Avec);
		int indexB = outGeom.addVertex(Bvec);
		PiVector line = new PiVector(indexA,indexB);
		outGeom.addPolygon(line);
	}

}
