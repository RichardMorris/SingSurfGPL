package org.singsurf.singsurf.test;

import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.lsmp.djep.djep.DJep;
import org.lsmp.djep.sjep.PNodeI;
import org.lsmp.djep.sjep.PolynomialCreator;
import org.nfunk.jep.Node;
import org.singsurf.singsurf.acurve.AsurfException;
import org.singsurf.singsurf.acurve.Bern1D;
import org.singsurf.singsurf.acurve.Bern2D;
import org.singsurf.singsurf.acurve.Range2D;
import org.singsurf.singsurf.jepwrapper.JepException;


class ReduceTest {
	DJep jep;
	PolynomialCreator pc;
	@BeforeEach
	void setUp() throws Exception {
		jep = new DJep();
		pc = new PolynomialCreator(jep);
		
	}

	@Test
	void testBern1D() throws JepException, AsurfException {
		Node n1 = jep.parse("x^2-0.5");
		PNodeI p2 = pc.createPoly(n1);
		var co = pc.toDoubleArray(p2, "x");
		System.out.println(Arrays.toString(co));
		var bern = Bern1D.formbernstein1D(co, 0., 1.);
		System.out.println(bern);
		var diff = bern.diff();
		System.out.println(diff);
		var bern_red = bern.reduce();
		var diff_red = diff.reduce();
		var left = bern_red.l;
		var right = bern_red.r;
		var left_diff = left.diff();
		var right_diff = right.diff();
		var diff_left = diff_red.l;
		var diff_right = diff_red.r;
		
		System.out.println(diff_left);
		System.out.println(left_diff);

		System.out.println(diff_right);
		System.out.println(right_diff);

	}

	@Test
	void testBern2D() throws JepException, AsurfException {
		Node n1 = jep.parse("x^3+y^2-0.5");
		PNodeI p2 = pc.createPoly(n1);
		var co = pc.toDoubleArray(p2, "x","y");
		System.out.println(Arrays.deepToString(co));
		var bern = Bern2D.fromPolyCoeffs(co, new Range2D(0,1,0,1));
		System.out.println(bern);
		var diff = bern.diffX();
		System.out.println(diff);
		var bern_red = bern.reduce();
		var diff_red = diff.reduce();
		var lb = bern_red.lb;
		var rt = bern_red.rt;
		var lb_diff = lb.diffX();
		var rt_diff = rt.diffX();
		var diff_lb = diff_red.lb;
		var diff_rt = diff_red.rt;
		
		System.out.println(diff_lb);
		System.out.println(lb_diff);

		System.out.println(rt_diff);
		System.out.println(diff_rt);

	}

}
