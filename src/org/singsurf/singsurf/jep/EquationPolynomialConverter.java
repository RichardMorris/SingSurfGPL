/*
Created 27 Nov 2006 - Richard Morris
*/
package org.singsurf.singsurf.jep;

import java.util.List;
import java.util.Vector;

import org.lsmp.djep.groupJep.GroupJep;
import org.lsmp.djep.groupJep.PolynomialVisitor;
import org.lsmp.djep.groupJep.groups.ExtendedFreeGroup;
import org.lsmp.djep.groupJep.groups.Reals;
import org.lsmp.djep.groupJep.interfaces.RingI;
import org.lsmp.djep.groupJep.values.FreeGroupElement;
import org.lsmp.djep.groupJep.values.Polynomial;
import org.lsmp.djep.matrixJep.MatrixJep;
import org.nfunk.jep.Node;
import org.nfunk.jep.ParseException;
import org.singsurf.singsurf.definitions.Parameter;

/**
 * Converts equations to polynomial form
 * @author Richard Morris
 *
 */
public class EquationPolynomialConverter {
	MatrixJep jep;
	PolynomialVisitor pv;
	/**
	 * @param jep
	 */
	public EquationPolynomialConverter(MatrixJep jep) {
		this.jep = jep;
		this.pv = new PolynomialVisitor(jep);
	}

	public double[][] convert2D(Vector<Node> equations,String[] variables,List<Parameter> params) throws ParseException {
		
		Node top = jep.deepCopy(equations.lastElement());
		for(int i=equations.size()-2;i>=0;--i)
			top = jep.substitute(top,equations.elementAt(i));
		String[] names = new String[params.size()];
		Double[] values = new Double[params.size()];
		for(int i=0;i<params.size();++i) {
			names[i] = params.get(i).getName();
			values[i] = params.get(i).getVal();
		}
		top = jep.substitute(top, names, values);
		RingI ring = new Reals();
		ExtendedFreeGroup fg = new ExtendedFreeGroup(ring,variables[0]);
		ExtendedFreeGroup fg2 = new ExtendedFreeGroup(fg,variables[1]);
		GroupJep gj = new GroupJep(fg2);
		gj.addStandardConstants();
		Polynomial poly = pv.calcPolynomial(top,fg2);
		
		int xnum,ynum=0;
		Number co1[] = poly.getCoeffs();
		xnum = co1.length;
		for(int i=0;i<co1.length;++i)
		{
			FreeGroupElement fge3 = (FreeGroupElement) co1[i];
			Number co2[] = fge3.getCoeffs();
			if(co2.length > ynum) ynum = co2.length; 
		}
		double[][] res = new double[xnum][ynum];
		for(int i=0;i<co1.length;++i)
		{
			FreeGroupElement fge3 = (FreeGroupElement) co1[i];
			Number co2[] = fge3.getCoeffs();
			for(int j=0;j<co2.length;++j)
				res[i][j] = ((Double) co2[j]).doubleValue();
		}
		return res;
	}

	public double[][][] convert3D(List<Node> equations,String[] variables,List<Parameter> params)  throws ParseException {

		Node top = jep.deepCopy(equations.get(equations.size()-1));
		for(int i=equations.size()-2;i>=0;--i)
			top = jep.substitute(top,equations.get(i));
		String[] names = new String[params.size()];
		Double[] values = new Double[params.size()];
		for(int i=0;i<params.size();++i) {
			names[i] = params.get(i).getName();
			values[i] = params.get(i).getVal();
		}
		top = jep.substitute(top, names, values);
		RingI ring = new Reals();
		ExtendedFreeGroup fg = new ExtendedFreeGroup(ring,variables[2]);
		ExtendedFreeGroup fg2 = new ExtendedFreeGroup(fg,variables[1]);
		ExtendedFreeGroup fg3 = new ExtendedFreeGroup(fg2,variables[0]);
		GroupJep gj = new GroupJep(fg3);
		gj.addStandardConstants();
		Polynomial poly = pv.calcPolynomial(top,fg3);
		
		int xnum,ynum=0,znum=0;
		Number co1[] = poly.getCoeffs();
		xnum = co1.length;
		for(int i=0;i<co1.length;++i)
		{
			FreeGroupElement fge3 = (FreeGroupElement) co1[i];
			Number co2[] = fge3.getCoeffs();
			if(co2.length > ynum) ynum = co2.length;
			for(int j=0;j<co2.length;++j) {
				FreeGroupElement fge4 = (FreeGroupElement) co2[j];
				Number co3[] = fge4.getCoeffs();
				if(co3.length > znum) znum = co3.length;
			}
		}
		double[][][] res = new double[xnum][ynum][znum];
		for(int i=0;i<co1.length;++i)
		{
			FreeGroupElement fge3 = (FreeGroupElement) co1[i];
			Number co2[] = fge3.getCoeffs();
			for(int j=0;j<co2.length;++j) {
				FreeGroupElement fge4 = (FreeGroupElement) co2[j];
				Number co3[] = fge4.getCoeffs();
				
				for(int k=0;k<co3.length;++k) {
						res[i][j][k] = ((Double) co3[k]).doubleValue();
				}
			}
		}
		return res;

	}
}
