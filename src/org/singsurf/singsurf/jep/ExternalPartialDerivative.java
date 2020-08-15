/*
Created 17 Sep 2006 - Richard Morris
*/
package org.singsurf.singsurf.jep;


import com.singularsys.extensions.djep.DVariable;
import com.singularsys.extensions.djep.PartialDerivative;
import com.singularsys.extensions.matrix.DimensionVisitor;
import com.singularsys.jep.parser.Node;

public class ExternalPartialDerivative extends PartialDerivative {
   private static final long serialVersionUID = 350L;

    public ExternalPartialDerivative(DVariable var, String[] derivnames) {
		super(var, derivnames);
        this.setHook(DimensionVisitor.DIM_KEY, 
                var.getHook(DimensionVisitor.DIM_KEY));

	}

	/**
	 * @param var
	 * @param derivnames
	 * @param deriv
	 */
	public ExternalPartialDerivative(DVariable var, String[] derivnames, Node deriv) {
		super(var, derivnames);
        this.setHook(DimensionVisitor.DIM_KEY, 
                var.getHook(DimensionVisitor.DIM_KEY));
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean derivativeIsTrivallyZero() {
		return false;
	}

}
