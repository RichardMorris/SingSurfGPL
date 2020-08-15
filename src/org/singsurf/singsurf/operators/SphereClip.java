/*
Created 25-Apr-2006 - Richard Morris
*/
package org.singsurf.singsurf.operators;

import jv.vecmath.PdVector;

public class SphereClip extends SimpleClip {
	double radius;
	PdVector center=null;
	public SphereClip(double rad) {
		super();
		radius=rad;
		center = new PdVector(0,0,0);
	}

	public SphereClip(double rad,PdVector cen) {
		super();
		radius=rad;
		center = cen;
	}

	@Override
	public boolean testClip(PdVector vec) {
		if(center==null)
			return (vec.length() < radius);
		else
			return (vec.dist(center) < radius);
	}

	public PdVector getCenter() {
		return center;
	}

	public void setCenter(PdVector center) {
		this.center = center;
	}

	public double getRadius() {
		return radius;
	}

	public void setRadius(double radius) {
		this.radius = radius;
	}

}
