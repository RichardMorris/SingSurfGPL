/*
Created 27 Nov 2006 - Richard Morris
*/
package org.singsurf.singsurf.jep;

import java.util.List;

import org.singsurf.singsurf.definitions.Parameter;

import com.singularsys.extensions.djep.DJep;
import com.singularsys.extensions.field.FieldI;
import com.singularsys.extensions.polynomials.PNodeI;
import com.singularsys.extensions.polynomials.PolynomialCreator;
import com.singularsys.jep.JepException;
import com.singularsys.jep.parser.Node;

/**
 * Converts equations to polynomial form
 * @author Richard Morris
 *
 */
public class EquationPolynomialConverter {
	DJep jep;
	PolynomialCreator pv;
	/**
	 * @param jep
	 */
	public EquationPolynomialConverter(DJep jep,FieldI f) {
		this.jep = jep;
		this.pv = new PolynomialCreator(jep,f);
	}

	public double[][] convert2D(List<Node> equations,String[] variables,List<Parameter> params) throws JepException {
		
		Node top = jep.deepCopy(equations.get(equations.size()-1));
		for(int i=equations.size()-2;i>=0;--i)
			top = jep.substitute(top,equations.get(i));
//		String[] names = new String[params.size()];
//		Double[] values = new Double[params.size()];
		for(Parameter p :params) {
			String name = p.getName();
			double value = p.getVal();
			top = jep.substitute(top, name, value);
		}
		PNodeI poly = pv.createPoly(top).expand();
		double[][] res =  pv.toDoubleArray(poly, variables[0],variables[1]);
		return res;
	}

    public double[][][] convert3D(List<Node> equations,String[] variables,List<Parameter> params) throws JepException {
        
        Node top = jep.deepCopy(equations.get(equations.size()-1));
        for(int i=equations.size()-2;i>=0;--i)
            top = jep.substitute(top,equations.get(i));
//      String[] names = new String[params.size()];
//      Double[] values = new Double[params.size()];
        for(Parameter p:params) {
            String name = p.getName();
            double value = p.getVal();
            top = jep.substitute(top, name, value);
        }
        Node proc = jep.preprocess(top);
        Node subst = jep.replaceVariableByExpressions(proc);
        Node simp = jep.clean(subst);
        PNodeI poly = pv.expand(pv.createPoly(simp));
        double[][][] res =  pv.toDoubleArray(poly, variables[0],variables[1],variables[2]);
        return res;
    }

    public Node subAll(List<Node> equations) throws JepException {
        
        Node top = jep.deepCopy(equations.get(equations.size()-1));
        for(int i=equations.size()-2;i>=0;--i)
            top = jep.substitute(top,equations.get(i));
        Node proc = jep.preprocess(top);
        Node simp = jep.clean(proc);
        return simp;
    }

    public Node subAllExpand(List<Node> equations) throws JepException {
        
        Node top = jep.deepCopy(equations.get(equations.size()-1));
        for(int i=equations.size()-2;i>=0;--i)
            top = jep.substitute(top,equations.get(i));
        Node proc = jep.preprocess(top);
        Node exp = pv.expand(proc);
        Node simp = jep.clean(exp);
        return simp;
    }

}
