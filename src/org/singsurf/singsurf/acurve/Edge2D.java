/*
Created 25 Nov 2006 - Richard Morris
*/
package org.singsurf.singsurf.acurve;

public class Edge2D {
	int x,y,denom;
	Key2D key;
	/**
	 * @param x
	 * @param y
	 * @param denom
	 */
	public Edge2D(int x, int y, int denom,Key2D edge) {
		this.x = x;
		this.y = y;
		this.denom = denom;
		this.key = edge;
	}
	@Override
	public String toString() {
		switch(key) {
		case X: return "X-Edge2D" + "("+x+","+y+")/"+denom;
		case Y: return "Y-Edge2D" + "("+x+","+y+")/"+denom;
        case FACE: return "Face2D" + "("+x+","+y+")/"+denom;
        case NONE: return "None";
        case VERTEX: return "Vertex2D" + "("+x+","+y+")/"+denom;
		}
		return null;
	}
	public BinEdge split() {
		return new BinEdge(this);
	}
	public static class BinEdge {
		Edge2D l;
		Edge2D h;
		public BinEdge(Edge2D edge) {
			l = new Edge2D(edge.x*2,edge.y*2,edge.denom*2,edge.key);
			switch(edge.key) {
			case X: h = new Edge2D(edge.x*2+1,edge.y*2,edge.denom*2,edge.key); break;
			case Y: h = new Edge2D(edge.x*2,edge.y*2+1,edge.denom*2,edge.key); break;
            default:
                break;
			}
		}
	}
}
