/*
Created 25-Apr-2006 - Richard Morris
*/
package org.singsurf.singsurf.operators;

/**
 * An exception thrown if the geometry type is not applicability.
 * @author Richard Morris
 *
 */
public class UnSuportedGeometryException extends Exception {
	private static final long serialVersionUID = 4763325901526268914L;

	public UnSuportedGeometryException(String message) {
		super(message);
	}
	
}
