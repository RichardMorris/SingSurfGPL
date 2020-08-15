/*
Created 26 Nov 2006 - Richard Morris
*/
package org.singsurf.singsurf.acurve;

import jv.vecmath.PdVector;

public class Sol2D {
    Key2D key;
    int x, y, denom;
    double val;
    double val2;

    /**
     * @param edge
     * @param x
     * @param y
     * @param denom
     * @param val
     */
    public Sol2D(Key2D edge, int x, int y, int denom, double val) {
	this.key = edge;
	this.x = x;
	this.y = y;
	this.denom = denom;
	this.val = val;
    }

    /**
     * @param edge
     * @param x
     * @param y
     * @param denom
     * @param val
     * @param val2
     */
    public Sol2D(Key2D edge, int x, int y, int denom, double val, double val2) {
	this.key = edge;
	this.x = x;
	this.y = y;
	this.denom = denom;
	this.val = val;
	this.val2 = val2;
    }

    public Sol2D(Face2D f, double val, double val2) {
	this.key = Key2D.FACE;
	this.x = f.x;
	this.y = f.y;
	this.denom = f.denom;
	this.val = val;
	this.val2 = val2;
    }

    public Sol2D(Face2D f, Key2D key, boolean opposite, double val) {
	this.x = f.x;
	this.y = f.y;
	this.denom = f.denom;
	this.val = val;
	this.key = key;
	if (opposite)
	    switch (key) {
	    case X:
		++this.x;
		break;
	    case Y:
		++this.y;
		break;
	    case FACE:
		break;
	    case NONE:
		break;
	    case VERTEX:
		break;
	    default:
		break;
	    }
    }

    public Sol2D(Edge2D edge, double val) {
	this.x = edge.x;
	this.y = edge.y;
	this.denom = edge.denom;
	this.key = edge.key;
	this.val = val;
    }

    /**
     * A solution at the lower end of the edge.
     * 
     * @param edge
     */
    public Sol2D(Edge2D edge) {
	this.x = edge.x;
	this.y = edge.y;
	this.denom = edge.denom;
	this.key = Key2D.VERTEX;
    }

    public Sol2D(int x, int y, int denom) {
	this.x = x;
	this.y = y;
	this.denom = denom;
	this.key = Key2D.VERTEX;
    }

    public PdVector toPdVector(Range2D range) {
	PdVector res = new PdVector(3);
	res.m_data[2] = 0.0;
	switch (key) {
	case VERTEX:
	    res.m_data[0] = range.xmin + (range.width * x) / denom;
	    res.m_data[1] = range.ymin + (range.height * y) / denom;
	    break;
	case X:
	    res.m_data[0] = range.xmin + (range.width * (x + val)) / denom;
	    res.m_data[1] = range.ymin + (range.height * y) / denom;
	    break;
	case Y:
	    res.m_data[0] = range.xmin + (range.width * x) / denom;
	    res.m_data[1] = range.ymin + (range.height * (y + val)) / denom;
	    break;
	case FACE:
	    res.m_data[0] = range.xmin + (range.width * (x + val)) / denom;
	    res.m_data[1] = range.ymin + (range.height * (y + val2)) / denom;
	    break;
	case NONE:
	    break;
	default:
	    break;
	}
	return res;
    }

    @Override
    public String toString() {
	return "sol " + key + " (" + x + "," + y + ")/" + denom + " root " + val;
    }
}
