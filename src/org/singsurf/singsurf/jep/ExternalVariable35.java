/*
Created 17 Sep 2006 - Richard Morris
*/
package org.singsurf.singsurf.jep;

import org.singsurf.singsurf.calculators.Calculator;

import com.singularsys.extensions.djep.DJep;
import com.singularsys.extensions.djep.DVariable;
import com.singularsys.extensions.djep.PartialDerivative;
import com.singularsys.extensions.matrix.DimensionVisitor;
import com.singularsys.extensions.matrix.Dimensions;
import com.singularsys.jep.ParseException;
import com.singularsys.jep.parser.Node;

public class ExternalVariable extends DVariable {
    private static final long serialVersionUID = 350L;
    Calculator calc;
    
	public ExternalVariable(Calculator calc,String name,Dimensions dim) {
		super(name);
		this.calc = calc;
		this.setDimensions(dim);
	}

	public void setDimensions(Dimensions dims) {
	    this.setHook(DimensionVisitor.DIM_KEY, dims);
    }

	public Dimensions detDimensions() {
	         return (Dimensions) this.getHook(DimensionVisitor.DIM_KEY);
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
	public boolean derivativeIsTrivallyZero() {
		return false;
	}

}
