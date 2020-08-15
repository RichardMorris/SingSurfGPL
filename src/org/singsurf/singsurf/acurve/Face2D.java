/*
Created 25 Nov 2006 - Richard Morris
*/
package org.singsurf.singsurf.acurve;


public class Face2D {
	int x,y,denom;

	/**
	 * @param x
	 * @param y
	 * @param denom
	 */
	public Face2D(int x, int y, int denom) {
		this.x = x;
		this.y = y;
		this.denom = denom;
	}
	@Override
	public String toString() {
		return "Face2D: ("+x+","+y+")/"+denom;
	}
	
	public QuadFace split() {
		return new QuadFace(this);
	}
	public static class QuadFace {
		Face2D ll,lh,hl,hh;
		public QuadFace(Face2D face) {
			ll = new Face2D(face.x*2,face.y*2,face.denom*2);
			lh = new Face2D(face.x*2,face.y*2+1,face.denom*2);
			hl = new Face2D(face.x*2+1,face.y*2,face.denom*2);
			hh = new Face2D(face.x*2+1,face.y*2+1,face.denom*2);
		}
	}
	public Edge2D bottom() {
		return new Edge2D(x,y,denom,Key2D.X);
	}
	public Edge2D top() {
		return new Edge2D(x,y+1,denom,Key2D.X);
	}
	public Edge2D left() {
		return new Edge2D(x,y,denom,Key2D.Y);
	}
	public Edge2D right() {
		return new Edge2D(x+1,y,denom,Key2D.Y);
	}
	
	enum faceEdge { OUTSIDE,LB,LT,RB,RT,L,R,T,B,INSIDE };
	public static class FaceSol {
		faceEdge fe;
		double x,y;

		public FaceSol(faceEdge fe, double x,double y) {
			this.fe = fe;
			this.x = x;
			this.y = y;
		}
	}
	public FaceSol whichEdge(Sol2D sol) {
		int left = sol.x * this.denom - this.x * sol.denom;
		if(left < 0) return new FaceSol(faceEdge.OUTSIDE,0,0);
		int right = sol.x * this.denom - (this.x+1) * sol.denom;
		if(right > 0) return new FaceSol(faceEdge.OUTSIDE,0,0);
		int bottom = sol.y * this.denom - this.y * sol.denom;
		if(bottom < 0) return new FaceSol(faceEdge.OUTSIDE,0,0);
		int top = sol.y * this.denom - (this.y+1) * sol.denom;
		if(top > 0) return new FaceSol(faceEdge.OUTSIDE,0,0);
		switch(sol.key) {
		case VERTEX:
			if(left==0 && bottom==0) return new FaceSol(faceEdge.LB,0,0);
			if(left==0 && top==0) return new FaceSol(faceEdge.LT,0,1);
			if(right==0 && bottom==0) return new FaceSol(faceEdge.RB,1,0);
			if(right==0 && top==0) return new FaceSol(faceEdge.RT,1,1);
			if(left==0) return new FaceSol(faceEdge.L,0,(sol.y+sol.val)*this.denom/sol.denom-this.y);
			if(right==0) return new FaceSol(faceEdge.R,1,(sol.y+sol.val)*this.denom/sol.denom-this.y);
			if(bottom==0) return new FaceSol(faceEdge.B,(sol.x+sol.val)*this.denom/sol.denom-this.x,0);
			if(top==0) return new FaceSol(faceEdge.T,(sol.x+sol.val)*this.denom/sol.denom-this.x,1);
			break;
		case X:
			if(bottom==0) return new FaceSol(faceEdge.B,(sol.x+sol.val)*this.denom/sol.denom-this.x,0);
			if(top==0) return new FaceSol(faceEdge.T,(sol.x+sol.val)*this.denom/sol.denom-this.x,1);
			break;
		case Y:
			if(left==0) return new FaceSol(faceEdge.L,0,(sol.y+sol.val)*this.denom/sol.denom-this.y);
			if(right==0) return new FaceSol(faceEdge.R,1,(sol.y+sol.val)*this.denom/sol.denom-this.y);
			break;
		case FACE:
			break;
        default:
            break;
		}
		return  new FaceSol(faceEdge.INSIDE,0,0);
	}
	

}
