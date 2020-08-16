/*
Created 17 Sep 2006 - Richard Morris
*/
package org.singsurf.singsurf.jep;

import org.lsmp.djep.djep.DJep;
import org.lsmp.djep.djep.PartialDerivative;
import org.lsmp.djep.matrixJep.MatrixVariable;
import org.lsmp.djep.vectorJep.Dimensions;
import org.lsmp.djep.vectorJep.values.MatrixValueI;
import org.nfunk.jep.Node;
import org.nfunk.jep.ParseException;
import org.singsurf.singsurf.calculators.Calculator;

public class ExternalVariable extends MatrixVariable {
	Calculator calc;

	public ExternalVariable(Calculator calc,String name,int dim) {
		super(name);
		this.calc = calc;
		this.setDimensions(Dimensions.valueOf(dim));
	}

	@Override
	public PartialDerivative createDerivative(String[] derivnames, Node eqn) {
		return new ExternalPartialDerivative(this,derivnames);
	}

	@Override
	protected PartialDerivative calculateDerivative(String[] derivnames, DJep jep) throws ParseException {
		// TODO Auto-generated method stub
		return createDerivative(derivnames, null);
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
