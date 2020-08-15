/*
Created 25 Nov 2006 - Richard Morris
*/
package org.singsurf.singsurf.acurve;

public class Vertex2D {
    int x, y, denom;

    /**
     * @param x
     * @param y
     * @param denom
     */
    public Vertex2D(int x, int y, int denom) {
	this.x = x;
	this.y = y;
	this.denom = denom;
    }

    @Override
    public String toString() {
	return "Vertex2D: (" + x + "," + y + ")/" + denom;
    }
}
