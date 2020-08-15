/*
Created 24-May-2006 - Richard Morris
*/
package org.singsurf.singsurf.geometries;

import jv.project.PgGeometryIf;

/**
 * Represents a linked pair of geometries.
 * @author Richard Morris
 */
public class GeomPair {
	PgGeometryIf input;
	PgGeometryIf output;
	
	@SuppressWarnings("unused")
    private GeomPair() {}
	
	/**
	 * @param input
	 * @param output
	 */
	public GeomPair(PgGeometryIf input, PgGeometryIf output) {
		// TODO Auto-generated constructor stub
		this.input = input;
		this.output = output;
	}
	synchronized public PgGeometryIf  getInput() {
		return input;
	}
	public PgGeometryIf getOutput() {
		return output;
	}

}
