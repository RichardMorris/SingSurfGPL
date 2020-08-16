/*
Created 17 Sep 2006 - Richard Morris
*/
package org.singsurf.singsurf.jep;

import org.lsmp.djep.matrixJep.MatrixVariable;
import org.lsmp.djep.vectorJep.values.MatrixValueI;
import org.nfunk.jep.Node;

public class ExternalPartialDerivative extends org.lsmp.djep.matrixJep.MatrixPartialDerivative {

	public ExternalPartialDerivative(MatrixVariable var, String[] derivnames) {
		super(var, derivnames);
	}

	/**
	 * @param var
	 * @param derivnames
	 * @param deriv
	 */
	public ExternalPartialDerivative(MatrixVariable var, String[] derivnames, Node deriv) {
		super(var, derivnames);
		// TODO Auto-generated constructor stub
	}

	@Override
	public MatrixValueI getMValue() {
		// TODO Auto-generated method stub
		return super.getMValue();
	}

	@Override
	public boolean derivativeIsTrivallyZero() {
		return false;
	}

}
