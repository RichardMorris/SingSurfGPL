package org.singsurf.singsurf.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.text.DecimalFormat;
import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.singularsys.extensions.djep.DJep;
import com.singularsys.extensions.polynomials.Monomial;
import com.singularsys.extensions.polynomials.PConstant;
import com.singularsys.extensions.polynomials.PFunction;
import com.singularsys.extensions.polynomials.PNodeI;
import com.singularsys.extensions.polynomials.POperator;
import com.singularsys.extensions.polynomials.PVariable;
import com.singularsys.extensions.polynomials.Polynomial;
import com.singularsys.extensions.polynomials.PolynomialCreator;
import com.singularsys.extensions.polynomials.comparators.Divider;
import com.singularsys.extensions.polynomials.comparators.LexComparator;
import com.singularsys.extensions.polynomials.comparators.Divider.DivideResult;
import com.singularsys.jep.EvaluationException;
import com.singularsys.jep.JepException;
import com.singularsys.jep.NodeFactory;
import com.singularsys.jep.OperatorTable2;
import com.singularsys.jep.ParseException;
import com.singularsys.jep.parser.Node;

class RotderivTest {
	DJep jep;
	NodeFactory nf;
	private OperatorTable2 ot;
	PolynomialCreator pc;
	private Divider divider;
	
	@BeforeEach
	void setUp() throws Exception {
		jep = new DJep();
		jep.addStandardDiffRules();
		nf = jep.getNodeFactory();
		ot = (OperatorTable2) jep.getOperatorTable();
		ot.getMultiply().setPrintSymbol(" ");
		DecimalFormat fmt = new DecimalFormat();
		fmt.setMinimumFractionDigits(0);
		fmt.setMaximumFractionDigits(15);
		jep.getPrintVisitor().setNumberFormat(fmt);
		pc = new PolynomialCreator(jep);
		pc.setComparator(new LexComparator(pc));
		divider = new Divider(pc);

	}

	public Object parseeval(String fs) throws ParseException, EvaluationException {
		Node n = jep.parse(fs);
		return jep.evaluate(n);
	}

	public PNodeI substitute(PNodeI target,PVariable var,PNodeI sub) throws ParseException {
    	if(target instanceof PVariable) {
        	if(target.equalsPNode(var)) 
        		return sub;
    		return target;
    	}
    	else if(target instanceof Monomial mon) {
    		final int nVars = mon.getNVars();
			var terms = new PNodeI[nVars];
			var pows = new PNodeI[nVars];
			for(int i=0;i<nVars;++i) {
				terms[i] = substitute(mon.getVar(i),var,sub);
				pows[i] = substitute(mon.getPower(i),var,sub);
			}
			var mm = Monomial.valueOf(pc,mon.getCoeff(),terms,pows);
			return mm;
    	}
    	else if(target instanceof Polynomial poly) {
   		 	var nterms = poly.getNTerms();
   		 	var terms = new PNodeI[nterms];
   		 	for(var i=0;i<nterms;++i) {
   		 		terms[i] = substitute(poly.getTerm(i),var,sub);
   		 	}
   		 	return Polynomial.valueOf(pc, terms);
    	}
    	else if(target instanceof PConstant con) {
    		return con;
    	}
    	else if(target instanceof PFunction fun) {
    		var nargs = fun.getNArgs();
    		var terms = new PNodeI[nargs];
   		 	for(var i=0;i<nargs;++i) {
   		 		terms[i] = substitute(fun.getArg(i),var,sub);
   		 	}
  		 	return new PFunction(pc,fun.getName(),fun.getPfmc(),terms);
    	}
    	else if(target instanceof POperator op) {
    		var nargs = op.getNArgs();
    		var terms = new PNodeI[nargs];
   		 	for(var i=0;i<nargs;++i) {
   		 		terms[i] = substitute(op.getArg(i),var,sub);
   		 	}
  		 	return new POperator(pc,op.getOp(),terms);
    	}
    	else {
    		throw new ParseException("Bad type for target");
    	}
    }
    
	double rnd2dp() {
		double a =  Math.rint(Math.random()*200-100) /100;
		return a;
	}
	
	
	
    @Test
    void find_varities_where_all_skeletons_trivial() throws JepException {
    	Node n1 = jep.parse("Det=a d f + 2 b c e - a e^2 - d c^2 - f b^2");
    	Node n2 = jep.parse("-Det*(a+d-2 b)");
    	Node n3 = jep.parse("-Det*(a+d+2 b)");
    	Node n4 = jep.parse("-Det*(a+f-2 c)");
    	Node n5 = jep.parse("-Det*(a+f+2 c)");
    	Node n6 = jep.parse("-Det*(d+f-2 e)");
    	Node n7 = jep.parse("-Det*(d+f+2 e)");
    	Node n8 = jep.parse("a d - b^2");
    	Node n9 = jep.parse("-Det*(a)");
    	Node n10 = jep.parse("-Det*(d)");
    	Node n11 = jep.parse("-Det*(f)");

    	
    	while(true) {
			jep.setVariable("a", rnd2dp());
			jep.setVariable("b", rnd2dp());
			jep.setVariable("c", rnd2dp());
			jep.setVariable("d", rnd2dp());
			jep.setVariable("e", rnd2dp());
			jep.setVariable("f", rnd2dp());

			double r1 = (Double) jep.evaluate(n1);
			double r2 = (Double) jep.evaluate(n2);
			double r3 = (Double) jep.evaluate(n3);
			double r4 = (Double) jep.evaluate(n4);
			double r5 = (Double) jep.evaluate(n5);
			double r6 = (Double) jep.evaluate(n6);
			double r7 = (Double) jep.evaluate(n7);
			double r8 = (Double) jep.evaluate(n8);
			double r9 = (Double) jep.evaluate(n9);
			double r10 = (Double) jep.evaluate(n10);
			double r11 = (Double) jep.evaluate(n11);
			
			boolean posdef = (((Double)jep.getVariableValue("a")) > 0) && (r8 > 0) && (r1 > 0);
			boolean negdef = (((Double)jep.getVariableValue("a")) < 0) && (r8 > 0) && (r1 < 0);
			if(!posdef && !negdef ) {
				System.out.printf("a %5.2f %5.2f %5.2f %5.2f %5.2f %5.2f ",
						jep.getVariableValue("a"),
						jep.getVariableValue("b"),
						jep.getVariableValue("c"),
						jep.getVariableValue("d"),
						jep.getVariableValue("e"),
						jep.getVariableValue("f"));
				System.out.printf("\tdet %5.2f xy %5.2f %5.2f xz %5.2f %5.2f yz %5.2f %5.2f x %5.2f %5.2f %5.2f %5.2f %b %b%n",
					r1, r2, r3, r4, r5, r6, r7, r9, r10, r11, r8,posdef,negdef);
				if(r2 <0 && r3 <0 && r4 <0 && r5 < 0 && r6 <0 && r7 < 0 && r9 < 0 && r10 < 0 && r11 <0 )
					break;
			}
		}
    }
    
    
	@Test
	void test_factorisation_by_dx_plus_dy() throws JepException {
		String fs = "a x^2 + 2 b x y + 2 c x z + d y^2 + 2 e y z + f z^2";
		Node fn = jep.parse(fs);
		PNodeI pn = pc.createPoly(fn);
		System.out.println("Base surface, F(x,y,z)=");
		System.out.println(pn);
		Node fx = jep.clean(jep.differentiate(fn, "x"));
		Node fy = jep.clean(jep.differentiate(fn, "y"));
		Node fz = jep.clean(jep.differentiate(fn, "z"));
		System.out.println("Derivatives");
		jep.println(fx);
		jep.println(fy);
		jep.println(fz);
		
		Node fxfy = nf.buildOperatorNode(ot.getAdd(),
				fx,fy);
		System.out.println("dF/dx+dF/dy");
		jep.println(fxfy);
		
		PNodeI fxfyp = pc.createPoly(fxfy);
		PNodeI[] fxfypx = pc.toCoefficientArray(fxfyp, "x");
		PNodeI[] fxfypy = pc.toCoefficientArray(fxfypx[0], "y");
		PNodeI[] fxfypz = pc.toCoefficientArray(fxfypy[0], "z");
		System.out.println("Coefficients of x, y, z and constant term");
		System.out.println(fxfypx[1]);
		System.out.println(fxfypy[1]);
		System.out.println(fxfypz[1]);
		System.out.println(fxfypz[0]);
		PNodeI[][][] co = (PNodeI[][][]) pc.toPNodeArray(fxfyp, "x","y","z");
		System.out.println("Coefficient array");
		System.out.println(Arrays.deepToString(co));
		
		final PVariable x = new PVariable(pc,jep.getVariable("x"));
		final PVariable y = new PVariable(pc,jep.getVariable("y"));
		final PVariable z = new PVariable(pc,jep.getVariable("z"));
		final PVariable M = new PVariable(pc,jep.addVariable("M"));
		
		PNodeI cox = fxfypx[1].div(M).mul(x);
		PNodeI coy = fxfypy[1].div(M).mul(y);
		PNodeI sub = cox.negate().sub(coy);
		System.out.println("Equation to substitute z=...  with M=coeff of z ");
		System.out.println(sub);
		Node n2 = jep.substitute(fn,"z", sub.toNode());
		System.out.println("Substituted into F");
		jep.println(n2);
		PNodeI p2 = pc.createPoly(n2);
		PNodeI p2a = substitute(pn,z,sub);
		//System.out.println(p2);
		System.out.println("Alternate subs algorithm");
		System.out.println(p2a);
		
		PNodeI p3 = p2.mul(M);
		PNodeI p4 = p3.mul(M);
		System.out.println("Multiply by M^2");
		System.out.println(p4);
		PNodeI p5 = pc.expand(p4);
		System.out.println("expanded");
		System.out.println(p5);
		PNodeI p5a = p2a.mul(M).mul(M).expand();
		System.out.println("Alternate subs algorithm");
		System.out.println(p5a);
		assertEquals(0,p5.compareTo(p5a));
		PNodeI[][] terms = (PNodeI[][]) pc.toPNodeArray(p5, "x","y");
		System.out.println("Coefficients of x and y");
		System.out.println(Arrays.deepToString(terms));

		PNodeI p6 = terms[1][1].mul(terms[1][1]).sub(terms[0][2].mul(terms[2][0]).mul(new PConstant(pc,4.0)));
		PNodeI p7 = pc.expand(p6);
		System.out.println("Discriminants");
		System.out.println(p7);
		Node n8 = p7.toNode();
		Node p9 = fxfypz[1].toNode();
		Node n10 = jep.substitute(n8, "M", p9);
		PNodeI p11 = pc.createPoly(n10).div(new PConstant(pc,64.0)).expand();
		System.out.println("Sub in M, divide by 64");
		System.out.println(p11);
		System.out.println();
		
		if(p11 instanceof Polynomial poly) {
			System.out.println("Individual terms");		
			int nterms = poly.getNTerms();
			System.out.println("nterms "+nterms);
			for(int i=0;i<nterms;++i) {
				System.out.println(poly.getTerm(i));
			}
		}
		
		Node n12 = p11.toNode();
		System.out.println("Random parameters");		
		for(int i=0;i<5;++i) {
			jep.setVariable("a", Math.random());
			jep.setVariable("b", Math.random());
			jep.setVariable("c", Math.random());
			jep.setVariable("d", Math.random());
			jep.setVariable("e", Math.random());
			jep.setVariable("f", Math.random());

			//jep.setVariable("b", -((Double)jep.getVariableValue("a")+(Double)jep.getVariableValue("d"))/2.0);
			//jep.setVariable("e", -((Double)jep.getVariableValue("c")));

			double res = (Double) jep.evaluate(n12);
//			assertEquals(0.0,res,1e-9);
			System.out.printf("a %4.1f b %4.1f c %4.1f d %4.1f e %4.1f f %4.1f\tdiscrim %6.3f%n",
					jep.getVariableValue("a"),
					jep.getVariableValue("b"),
					jep.getVariableValue("c"),
					jep.getVariableValue("d"),
					jep.getVariableValue("e"),
					jep.getVariableValue("f"),
					res
					);
			
		}
		
		Node n13 = jep.parse("(a+2b+d)( d c^4 + 2 (d - b) c^3 e -(a d - b^2) c^2 f "
				+ "+ (a - 4 b + d) c^2 e^2 + 2 (a -b) c e^3 -2(a d - b^2) c e f +  a e^4 -  (a d - b^2) e^2 f)");
		System.out.println("Factorisation of discriminant");
		jep.println(n13);
		PNodeI p14 = pc.createPoly(n13).expand();
		System.out.println("Expanded");		
		System.out.println(p14);
		System.out.println("Original discriminant");		
		System.out.println(p11);
		PNodeI p15 = p14.sub(p11);
		System.out.println("Difference (should be zero)");		
		System.out.println(p15);
		assertEquals(0,p14.compareTo(p11));

		System.out.println("Second factor");		
		Node n16 = jep.parse(" d c^4 + 2 (d - b) c^3 e -(a d - b^2) c^2 f "
							+ "+(a - 4 b + d) c^2 e^2 + 2 (a -b) c e^3 -2(a d - b^2) c e f +  a e^4 -  (a d - b^2) e^2 f");
		PNodeI p17 = pc.createPoly(n16).expand();
		System.out.println(p17);

		System.out.println("Factorisation of second term");		
		Node n18 = jep.parse(" -(c+e)^2 ( a d f + 2 b c e - a e^2 - d c^2 - f b^2) ");
		jep.println(n18);
		PNodeI p19 = pc.createPoly(n18).expand();
		System.out.println("Expanded");		
		System.out.println(p19);
		assertEquals(0,p17.compareTo(p19));
		
		PNodeI factor1 = pc.createPoly(jep.parse("a+2 b+d"));
		System.out.println("Divide by");
		System.out.println(factor1);
		DivideResult quot = divider.divisionAlgorithm(p11,factor1);
		System.out.println("Quotient");
		System.out.println(quot.quotient());
		System.out.println("Remainder");
		System.out.println(quot.remainder());
		assertTrue(quot.remainder().isZero());
		PNodeI factor2 = pc.createPoly(jep.parse("(c+e)^2")).expand();
		System.out.println("Divide by");
		System.out.println(factor2);
		DivideResult quot2 = divider.divisionAlgorithm(quot.quotient(),factor2);
		System.out.println("Quotient");
		System.out.println(quot2.quotient());
		System.out.println("Remainder");
		System.out.println(quot2.remainder());
		assertTrue(quot2.remainder().isZero());
		PNodeI factor3 = pc.createPoly(jep.parse("a*d*f-a*e^2-b^2*f+2*b*c*e-c^2*d"));
		System.out.println("Divide by");
		System.out.println(factor3);
		DivideResult quot3 = divider.divisionAlgorithm(p11,factor3);
		System.out.println("Quotient");
		System.out.println(quot3.quotient());
		System.out.println("Remainder");
		System.out.println(quot3.remainder());
		assertTrue(quot3.remainder().isZero());
	}

	@Test
	void test_factorisation_by_dx_plus_dy_plus_dz() throws JepException {
		String fs = "a x^2 + 2 b x y + 2 c x z + d y^2 + 2 e y z + f z^2";
		Node fn = jep.parse(fs);
		PNodeI pn = pc.createPoly(fn);
		System.out.println("F(x,y,z)=");
		System.out.println(pn);
		Node fx = jep.clean(jep.differentiate(fn, "x"));
		Node fy = jep.clean(jep.differentiate(fn, "y"));
		Node fz = jep.clean(jep.differentiate(fn, "z"));
		System.out.println("Derivatives");
		jep.println(fx);
		jep.println(fy);
		jep.println(fz);
		Node xpypz = nf.buildOperatorNode(ot.getAdd(),
				fx,
				nf.buildOperatorNode(ot.getAdd(), fy, fz));
		Node xpymz = nf.buildOperatorNode(ot.getAdd(),
				fx,
				nf.buildOperatorNode(ot.getSubtract(), fy, fz));
		Node xmypz = nf.buildOperatorNode(ot.getAdd(),
				fx,
				nf.buildOperatorNode(ot.getSubtract(), fz, fy));
		Node xmymz = nf.buildOperatorNode(ot.getSubtract(),
				fx,
				nf.buildOperatorNode(ot.getAdd(), fy, fz));
		System.out.println("dF/dx+dF/dy+dF/dz=");
		jep.println(xpypz);
		System.out.println("dF/dx+dF/dy-dF/dz=");
		jep.println(xpymz);
		System.out.println("dF/dx-dF/dy+dF/dz=");
		jep.println(xmypz);
		System.out.println("dF/dx-dF/dy-dF/dz=");
		jep.println(xmymz);
		
		PNodeI pxpypz = pc.createPoly(xpypz);
		PNodeI pxpymz = pc.createPoly(xpymz);
		PNodeI pxmypz = pc.createPoly(xmypz);
		PNodeI pxmymz = pc.createPoly(xmymz);

		PNodeI[][][] cxpypz = (PNodeI[][][]) pc.toPNodeArray(pxpypz, "x","y","z");
		PNodeI[][][] cxpymz = (PNodeI[][][]) pc.toPNodeArray(pxpymz, "x","y","z");
		PNodeI[][][] cxmypz = (PNodeI[][][]) pc.toPNodeArray(pxmypz, "x","y","z");
		PNodeI[][][] cxmymz = (PNodeI[][][]) pc.toPNodeArray(pxmymz, "x","y","z");
		System.out.println("Coefficients of deriv combinations");
		System.out.println(Arrays.deepToString(cxpypz));
		System.out.println(Arrays.deepToString(cxpymz));
		System.out.println(Arrays.deepToString(cxmypz));
		System.out.println(Arrays.deepToString(cxmymz));

		final PVariable x = new PVariable(pc,jep.getVariable("x"));
		final PVariable y = new PVariable(pc,jep.getVariable("y"));
		final PVariable z = new PVariable(pc,jep.getVariable("z"));
		final PVariable P = new PVariable(pc,jep.addVariable("P")); // 2*c+2*e+2*f
		final PVariable Q = new PVariable(pc,jep.addVariable("Q")); // 2*c+2*e-2*f
		final PVariable R = new PVariable(pc,jep.addVariable("R")); // 2*c+2*e+2*f
		final PVariable S = new PVariable(pc,jep.addVariable("S")); // 2*c+2*e-2*f
		final PConstant four = new PConstant(pc,4.0);
		final PConstant sixtyfour = new PConstant(pc,64.0);
		System.out.println("Substitution equations: z = ");
		PNodeI sub1 = cxpypz[1][0][0].mul(x).add(cxpypz[0][1][0].mul(y)).div(P);
		PNodeI sub2 = cxpymz[1][0][0].mul(x).add(cxpymz[0][1][0].mul(y)).div(Q);
		PNodeI sub3 = cxmypz[1][0][0].mul(x).add(cxmypz[0][1][0].mul(y)).div(R);
		PNodeI sub4 = cxmymz[1][0][0].mul(x).add(cxmymz[0][1][0].mul(y)).div(S);
		PNodeI sub1a = cxpypz[0][0][1].negate();
		PNodeI sub2a = cxpymz[0][0][1].negate();
		PNodeI sub3a = cxmypz[0][0][1].negate();
		PNodeI sub4a = cxmymz[0][0][1].negate();
		System.out.println(sub1);
		//System.out.println(sub1a);
		System.out.println(sub2);
		System.out.println(sub3);
		System.out.println(sub4);

		PNodeI subs1 = pn.substitute(z, sub1).mul(P).mul(P).expand();
		PNodeI subs2 = pn.substitute(z, sub2).mul(Q).mul(Q).expand();
		PNodeI subs3 = pn.substitute(z, sub3).mul(R).mul(R).expand();
		PNodeI subs4 = pn.substitute(z, sub4).mul(S).mul(S).expand();
		System.out.println("After sub");
		System.out.println(subs1);
		System.out.println(subs2);
		System.out.println(subs3);
		System.out.println(subs4);
		PNodeI[][] co1 = (PNodeI[][]) pc.toPNodeArray(subs1, "x","y");
		PNodeI[][] co2 = (PNodeI[][]) pc.toPNodeArray(subs2, "x","y");
		PNodeI[][] co3 = (PNodeI[][]) pc.toPNodeArray(subs3, "x","y");
		PNodeI[][] co4 = (PNodeI[][]) pc.toPNodeArray(subs4, "x","y");
		System.out.println("Cooeficient");
		System.out.println(Arrays.deepToString(co1));
		System.out.println(Arrays.deepToString(co2));
		System.out.println(Arrays.deepToString(co3));
		System.out.println(Arrays.deepToString(co4));
		PNodeI A1 = co1[2][0];
		PNodeI B1 = co1[1][1];
		PNodeI C1 = co1[0][2];

		PNodeI A2 = co2[2][0];
		PNodeI B2 = co2[1][1];
		PNodeI C2 = co2[0][2];

		PNodeI A3 = co3[2][0];
		PNodeI B3 = co3[1][1];
		PNodeI C3 = co3[0][2];

		PNodeI A4 = co4[2][0];
		PNodeI B4 = co4[1][1];
		PNodeI C4 = co4[0][2];

		System.out.println("Discriminants");
		PNodeI discrim1 = B1.mul(B1).sub(four.mul(A1).mul(C1));
		System.out.println(discrim1);
		PNodeI discrim1a = discrim1.substitute(P,sub1a);
		System.out.println(discrim1a);
		Node discrim1b = discrim1a.expand().toNode();
		
		PNodeI discrim2 = B2.mul(B2).sub(four.mul(A2).mul(C2));
		System.out.println(discrim2);
		PNodeI discrim2a = discrim2.substitute(Q,sub2a);
		System.out.println(discrim2a);
		Node discrim2b = discrim2a.expand().toNode();

		PNodeI discrim3 = B3.mul(B3).sub(four.mul(A3).mul(C3));
		System.out.println(discrim3);
		PNodeI discrim3a = discrim3.substitute(R,sub3a);
		System.out.println(discrim3a);
		Node discrim3b = discrim3a.expand().toNode();

		PNodeI discrim4 = B4.mul(B4).sub(four.mul(A4).mul(C4));
		System.out.println(discrim4);
		PNodeI discrim4a = discrim4.substitute(S,sub4a);
		System.out.println(discrim4a);
		Node discrim4b = discrim4a.expand().toNode();

    	Node det = jep.parse("Det=a d f + 2 b c e - a e^2 - d c^2 - f b^2");
    	
    	Node n2 = jep.parse("-Det*(a+d-2 b)");
    	Node n3 = jep.parse("-Det*(a+d+2 b)");
    	Node n4 = jep.parse("-Det*(a+f-2 c)");
    	Node n5 = jep.parse("-Det*(a+f+2 c)");
    	Node n6 = jep.parse("-Det*(d+f-2 e)");
    	Node n7 = jep.parse("-Det*(d+f+2 e)");
    	Node n8 = jep.parse("a d - b^2");
    	Node n9 = jep.parse("-Det*(a)");
    	Node n10 = jep.parse("-Det*(d)");
    	Node n11 = jep.parse("-Det*(f)");
    	//Node n12 = jep.parse("64(-2b-2c-2e-a-d-f)*Det*(c+e+f)^2");

    	//Node detZero = jep.parse("f = (a e^2 + d c^2 - 2 b c e)/(a d- b^2)");
    	int count=0;
		while(true) {
			jep.setVariable("a", rnd2dp());
			jep.setVariable("b", rnd2dp());
			jep.setVariable("c", rnd2dp());
			jep.setVariable("d", rnd2dp());
			jep.setVariable("e", rnd2dp());
			jep.setVariable("f", rnd2dp());
			//double f = (Double) jep.evaluate(detZero);
			//jep.setVariable("f", - ((Double)jep.getVariableValue("c"))-((Double)jep.getVariableValue("e")));

			double rdet = (Double) jep.evaluate(det); 
			double r2 = (Double) jep.evaluate(n2);
			double r3 = (Double) jep.evaluate(n3);
			double r4 = (Double) jep.evaluate(n4);
			double r5 = (Double) jep.evaluate(n5);
			double r6 = (Double) jep.evaluate(n6);
			double r7 = (Double) jep.evaluate(n7);
			double r8 = (Double) jep.evaluate(n8);
			double r9 = (Double) jep.evaluate(n9);
			double r10 = (Double) jep.evaluate(n10);
			double r11 = (Double) jep.evaluate(n11);
			//double r12 = (Double) jep.evaluate(n12);

			double rdet1 = (Double) jep.evaluate(discrim1b);
			double rdet2 = (Double) jep.evaluate(discrim2b);
			double rdet3 = (Double) jep.evaluate(discrim3b);
			double rdet4 = (Double) jep.evaluate(discrim4b);
			
			final Double a = (Double)jep.getVariableValue("a");
			
			boolean posdef = (a > 0) && (r8 > 0) && (rdet > 0);
			boolean negdef = (a < 0) && (r8 > 0) && (rdet < 0);

//				System.out.printf("\tdet %4.2f x+y+z %4.2f x+y-z %4.2f x-y+z %4.2f x-y-z %4.2f ad-b^2 %4.2f",
//						rdet, rdet1, rdet2, rdet3, rdet4, r8, posdef,negdef);
//				
//				System.out.printf("\txy %4.2f %4.2f xz %4.2f %4.2f yz %4.2f %4.2f x %4.2f %4.2f %4.2f %4.2f %b %b%n",
//						 r2, r3, r4, r5, r6, r7, r9, r10, r11, r8,posdef,negdef);
				
//				int basecount = ( r9 < 0 ? 0 : 1 ) + ( r10 < 0 ? 0 : 1 ) + ( r11 <0 ? 0 : 1);
//				int bicount = (r2 <0 ? 0 : 1) + (r3 <0 ? 0 : 1) + (r4 <0 ? 0: 1) + (r5 < 0?0:1)+(r6 <0?0:1)+(r7 < 0?0:1) ;
//				int tricount = (rdet1 <0?0:1) + (rdet2 <0?0:1)+ (rdet3 <0?0:1) +(rdet4 < 0?0:1);

				System.out.printf("%b,%b,%b\t%b,%b,%b,%b,%b,%b\t%b,%b,%b,%b\t%b,%b%n",
						r9 > 0, r10 > 0, r11> 0,
						r2 > 0, r3>0, r4>0, r5>0, r6>0,r7>0,
						rdet1 > 0, rdet2 > 0, rdet3 > 0, rdet4 > 0, posdef, negdef
						);
				if(++count>100) break;
//			}
		}
    	Node cond = jep.parse("-(a d f + 2 b c e - a e^2 - d c^2 - f b^2)*(-f+c-e)*(-f+c-e)*(a+d+f-2b-2c+2e)");
    	final PNodeI factorized = pc.createPoly(cond);
    	System.out.println("Factorized");
		System.out.println(factorized);
		PNodeI pcond = factorized.expand();
    	final PNodeI expand = discrim4a.div(sixtyfour).expand();
    	System.out.println("Expanded discriminant");
		System.out.println(expand);
    	System.out.println("Number of terms "+((Polynomial)expand).getNTerms());
		System.out.println("Expanded factorization");
    	System.out.println(pcond);
    	System.out.println("Discrim - factorisation");
    	System.out.println(expand.sub(pcond));
    	assertEquals(pcond,expand);
    	
    	PNodeI r15 = pc.createPoly(jep.parse("a+d+f-2b-2c+2e"));
    	System.out.println("Factor");
    	System.out.println(r15);
    	DivideResult res = divider.divisionAlgorithm(expand, r15);
		System.out.println("Quotient");
		System.out.println(res.quotient());
		System.out.println("Remainder");
		System.out.println(res.remainder());
		assertTrue(res.remainder().isZero());
		PNodeI factor2 = pc.createPoly(jep.parse("(-f+c-e)^2")).expand();
		System.out.println("Divide by");
		System.out.println(factor2);
		DivideResult quot2 = divider.divisionAlgorithm(res.quotient(),factor2);
		System.out.println("Quotient");
		System.out.println(quot2.quotient());
		System.out.println("Remainder");
		System.out.println(quot2.remainder());
		assertTrue(quot2.remainder().isZero());

	}

}
