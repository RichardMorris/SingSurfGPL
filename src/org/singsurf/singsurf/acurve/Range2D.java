/*
Created 25 Nov 2006 - Richard Morris
*/
package org.singsurf.singsurf.acurve;

/**
 * Represents a 2D rectangular range.
 * @author Richard Morris
 *
 */
public class Range2D {
	final double xmin,xmax,ymin,ymax;
	final double width,height;
	public double getXmin() {
		return xmin;
	}
	public double getXmax() {
		return xmax;
	}
	public double getYmin() {
		return ymin;
	}
	public double getYmax() {
		return ymax;
	}
	public double getWidth() {
		return width;
	}
	public double getHeight() {
		return height;
	}
	/**
	 * @param xmin
	 * @param xmax
	 * @param ymin
	 * @param ymax
	 */
	public Range2D(double xmin, double xmax, double ymin, double ymax) {
		this.xmin = xmin;
		this.xmax = xmax;
		this.ymin = ymin;
		this.ymax = ymax;
		this.width = xmax-xmin;
		this.height = ymax-ymin;
	}
	
	
}
