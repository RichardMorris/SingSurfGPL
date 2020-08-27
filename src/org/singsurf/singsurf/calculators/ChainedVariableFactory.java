package org.singsurf.singsurf.calculators;

import org.lsmp.djep.matrixJep.MatrixVariableFactory;
import org.nfunk.jep.Variable;

class ChainedVariableFactory extends MatrixVariableFactory {

	/**
	 * 
	 */
	private final ChainedCalculator SpecialVariableFactory;

	/**
	 * @param chainedCalculator
	 */
	ChainedVariableFactory(ChainedCalculator chainedCalculator) {
		SpecialVariableFactory = chainedCalculator;
	}

	@Override
	public Variable createVariable(String name, Object value) {
		if(SpecialVariableFactory.dependentVariable!=null && name.equals(SpecialVariableFactory.dependentVariable.getName()))
			return SpecialVariableFactory.jepVar;
		else
			return super.createVariable(name, value);
	}

	@Override
	public Variable createVariable(String name) {
		if(SpecialVariableFactory.dependentVariable!=null && name.equals(SpecialVariableFactory.dependentVariable.getName()))
			return SpecialVariableFactory.jepVar;
		else
			return super.createVariable(name);
	}
	
}