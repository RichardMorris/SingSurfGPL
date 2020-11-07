package org.singsurf.singsurf.calculators;

import java.util.HashMap;
import java.util.Map;

import org.lsmp.djep.matrixJep.MatrixVariableFactory;
import org.nfunk.jep.Variable;

class SpecialVariableFactory extends MatrixVariableFactory {

	Map<String,Variable> specialVariables = new HashMap<>();

	/**
	 * @param chainedCalculator
	 */
	SpecialVariableFactory() {
	}

	@Override
	public Variable createVariable(String name, Object value) {
		if(specialVariables.containsKey(name)) {
			Variable var = specialVariables.get(name);
			return var;
		}
		return super.createVariable(name, value);
	}

	@Override
	public Variable createVariable(String name) {
		if(specialVariables.containsKey(name)) {
			Variable var = specialVariables.get(name);
			return var;
		}
		return super.createVariable(name);
	}

	public void clear() {
		specialVariables.clear();
	}

	public Variable add(Variable var) {
		return specialVariables.put(var.getName(),var);
	}
	
	public Variable set(Variable var) {
		specialVariables.clear();
		return specialVariables.put(var.getName(),var);
	}
	
}