package org.singsurf.singsurf.util;

import java.text.DecimalFormat;

import com.singularsys.extensions.polynomials.PNodeI;
import com.singularsys.extensions.polynomials.PolynomialCreator;
import com.singularsys.extensions.rewrite.RewriteVisitor;
import com.singularsys.extensions.rewrite.SmallNumberRule;
import com.singularsys.extensions.rewrite.VariableShifter;
import com.singularsys.extensions.xjep.XJep;
import com.singularsys.jep.JepException;
import com.singularsys.jep.ParseException;
import com.singularsys.jep.parser.Node;

/**
 * 
 */
public class JetExtractor {
	XJep jep;
	RewriteVisitor ev;
	public JetExtractor() {
		jep = new XJep();
		ev = jep.getRewriteVisitor();
	}
	
	private String monomial(String name,int power) {
		switch(power) {
		case 0:
			return "";
		case 1:
			return name;
		default:
			return name + "^" + power;
		}
	}
	
	Node shiftOrigin(Node expression,String[] names, double values[],double tol) throws ParseException {
		ev.clear();
		for(int i=0;i<names.length;++i) {
			ev.add(new VariableShifter(names[i],-values[i]));
		}
		if(tol!=0.0) {
			ev.add(new SmallNumberRule(tol));
		}
		jep.reinitializeComponents();
		Node rewritten = ev.rewrite(expression, true);
		return rewritten;
	}

	Node findJet(Node expression, String[] names,int deg) throws JepException {
		PolynomialCreator pc = jep.getPolynomialCreator();
		PNodeI poly = pc.expand(pc.createPoly(expression));
		StringBuilder sb = new StringBuilder();
		switch(names.length) {
		case 1:
		{
			double[] coeffs = pc.toDoubleArray(poly, names[0]);
			for(int i=coeffs.length;i>=0;--i) {
				if(i>deg) continue;
				sb.append(" + ");
				sb.append(Double.toString(coeffs[i]));
				sb.append(" ");
				sb.append(monomial(names[0],i));
			}
			break;
		}
		case 2:
		{
			double[][] coeffs = pc.toDoubleArray(poly, names[0],names[1]);
			int xdeg = coeffs.length-1;
			int ydeg = coeffs[0].length-1;

			for(int j=ydeg;j>=0;--j) {
			for(int i=xdeg;i>=0;--i) {
				if(i+j>deg) continue;				
				sb.append(" + ");
				sb.append(Double.toString(coeffs[i][j]));
				sb.append(" ");
				sb.append(monomial(names[0],i));
				sb.append(" ");
				sb.append(monomial(names[1],j));
			}
			}
			break;
		}
		case 3:
		{
			double[][][] coeffs = pc.toDoubleArray(poly, names[0],names[1],names[2]);
			int xdeg = coeffs.length-1;
			int ydeg = coeffs[0].length-1;
			int zdeg = coeffs[0][0].length-1;

			for(int k=zdeg;k>=0;--k) {
			for(int j=ydeg;j>=0;--j) {
			for(int i=xdeg;i>=0;--i) {
				if(i+j+k>deg) continue;				
				sb.append(" + ");
				sb.append(Double.toString(coeffs[i][j][k]));
				sb.append(" ");
				sb.append(monomial(names[0],i));
				sb.append(" ");
				sb.append(monomial(names[1],j));
				sb.append(" ");
				sb.append(monomial(names[2],k));
			}
			}
			}
			break;
		}
		default:
				throw new JepException("Too many variables");
		}
		Node truncated = jep.parse(sb.toString());
		return truncated;
	}

	
	public static void main(String[] args) {
		JetExtractor je = new JetExtractor();
		XJep jep = je.jep;

		Node barth;
		try {
			barth = jep.parse("4 ( t^2 x^2 - y^2 ) ( t^2 y^2 - z^2 ) ( t^2 z^2 - x^2 ) - ( 1 + 2 t) (x^2 + y^2 + z^2 - 1)^2");
			Node subst = jep.substitute(barth, "t", (1+Math.sqrt(5.0))/2.0);

			String[] names = new String[] {"x","y","z"};
			double[] values = new double[] {0.3090169943749441,0.5,-0.8090169943749501};
			double[] values2 = new double[] {-0.3090169943749441,-0.5,0.8090169943749501};
			Node shifted = je.shiftOrigin(subst, names, values, 1e-9);
			Node jet = je.findJet(shifted, names, 2);
			Node unshifted = je.shiftOrigin(jet, names, values2, 1e-9);
		
			DecimalFormat format = new DecimalFormat();
			format.setMaximumFractionDigits(304);
			format.setMinimumFractionDigits(0);

			jep.getOperatorTable().getAdd().setPrintSymbol(" + ");
			jep.getOperatorTable().getMultiply().setPrintSymbol(" ");
			jep.getPrintVisitor().setNumberFormat(format);
			jep.getPrintVisitor().setMaxLen(80);

			jep.println(unshifted);
		
		} catch (JepException e) {
			System.out.println(e);
		}

	}
	
		/*
		// TODO Auto-generated method stub
		Node barth = jep.parse("4 ( t^2 x^2 - y^2 ) ( t^2 y^2 - z^2 ) ( t^2 z^2 - x^2 ) - ( 1 + 2 t) (x^2 + y^2 + z^2 - 1)^2");
		Node subst = jep.substitute(barth, "t", (1+Math.sqrt(5.0))/2.0);
		ev.add(new VariableShifter("x",0.3090169943749441));
		ev.add(new VariableShifter("y",0.5));
		ev.add(new VariableShifter("z",-0.8090169943749501));
		ev.add(new SmallNumberRule(1e-9));
		jep.reinitializeComponents();
		Node chgd = ev.rewrite(subst, true);
		jep.println(chgd);
		PolynomialCreator pc = jep.getPolynomialCreator();
		PNodeI poly = pc.expand(pc.createPoly(chgd));
		System.out.println();
		double[][][] coeff = pc.toDoubleArray(poly, "x", "y", "z");
		int xdeg = coeff.length-1;
		int ydeg = coeff[0].length-1;
		int zdeg = coeff[0][0].length-1;
		StringBuffer sb = new StringBuffer();
		for(int k=0;k<=zdeg;++k) {
		for(int j=0;j<=ydeg;++j) {
		for(int i=0;i<=xdeg;++i) {
					if(i+j+k<=3) {						
//					System.out.printf("%8.3f ", coeff[i][j][k]);
//					System.out.print(" x^"+i+" y^"+j+" z^"+k+"\n");
					sb.append("+ ");
					sb.append(coeff[i][j][k]);
					sb.append(" x^"+i+" y^"+j+" z^"+k+"\n");
					}
				}
				System.out.println();
			}
			System.out.println();
		}
		System.out.println(sb.toString());
		System.out.println();

		ev.getVariableRules().clear();
//		ev.add(new VariableShifter("x",-0.3090169943749441));
//		ev.add(new VariableShifter("y",-0.5));
//		ev.add(new VariableShifter("z",+0.8090169943749501));
		ev.add(new SmallNumberRule(1e-9));
		jep.reinitializeComponents();
		
		DecimalFormat format = new DecimalFormat();
		format.setMaximumFractionDigits(3);
		format.setMinimumFractionDigits(0);

		jep.getOperatorTable().getAdd().setPrintSymbol(" + ");
		jep.getOperatorTable().getMultiply().setPrintSymbol(" ");
		jep.getPrintVisitor().setNumberFormat(format);
		Node quad = jep.parse(sb.toString()); 
		Node quad2 = ev.rewrite(quad, true);
		jep.println(quad2);
	

	}

*/
}
