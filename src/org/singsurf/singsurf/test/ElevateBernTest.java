package org.singsurf.singsurf.test;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.singsurf.singsurf.acurve.AsurfException;
import org.singsurf.singsurf.acurve.Bern1D;
import org.singsurf.singsurf.acurve.Bern2D;
import org.singsurf.singsurf.asurf.Bern3D;
import org.singsurf.singsurf.asurf.Box_info;
import org.singsurf.singsurf.asurf.Edge_info;
import org.singsurf.singsurf.asurf.Face_info;
import org.singsurf.singsurf.asurf.Face_info.Type;
import org.singsurf.singsurf.asurf.Key3D;

class ElevateBernTest {

	@BeforeEach
	void setUp() throws Exception {
	}

	@AfterEach
	void tearDown() throws Exception {
	}

	/**
	 * Checks raised Bernstein polynomials evaluate to same value as base
	 * 
	 * @throws AsurfException
	 */
	@Test
	void test_elevateBern1D() throws AsurfException {
		Bern1D b1 = new Bern1D(1);
		b1.coeff[0] = 0.3;
		b1.coeff[1] = 0.7;
		Bern1D b2 = b1.elevate();
		Bern1D b3 = b2.elevate();
		Bern1D b4 = b3.elevate();

		for (double x = 0; x <= 1.0; x += 0.1) {
			double r1 = b1.evaluate(x);
			double r2 = b2.evaluate(x);
			double r3 = b3.evaluate(x);
			double r4 = b4.evaluate(x);
			assertEquals(r1, r2, 1e-9, "x2=" + x);
			assertEquals(r1, r3, 1e-9, "x3=" + x);
			assertEquals(r1, r4, 1e-9, "x4=" + x);
		}

		b2.coeff[1] = 0.11;
		b3 = b2.elevate();
		b4 = b3.elevate();
		for (double x = 0; x <= 1.0; x += 0.1) {
			double r2 = b2.evaluate(x);
			double r3 = b3.evaluate(x);
			double r4 = b4.evaluate(x);
			assertEquals(r2, r3, 1e-9, "x23=" + x);
			assertEquals(r2, r4, 1e-9, "x24=" + x);
		}

		Bern1D b11 = b1.elevateTo(1);
		assertSame(b1, b11);
		b2 = b1.elevateTo(2);
		b3 = b1.elevateTo(3);
		b4 = b1.elevateTo(4);

		for (double x = 0; x <= 1.0; x += 0.1) {
			double r1 = b1.evaluate(x);
			double r2 = b2.evaluate(x);
			double r3 = b3.evaluate(x);
			double r4 = b4.evaluate(x);
			assertEquals(r1, r2, 1e-9, "x2=" + x);
			assertEquals(r1, r3, 1e-9, "x3=" + x);
			assertEquals(r1, r4, 1e-9, "x4=" + x);
		}

	}

	@Test
	void test_elevateBern3D() throws AsurfException {

		// a x + b y + c z + d
		double a = 0.3;
		double b = 0.5;
		double c = 0.7;
		double d = 1.1;

		Bern3D bb = new Bern3D(1, 1, 1);
		bb.setCoeff(1, 0, 0, a);
		bb.setCoeff(0, 1, 0, b);
		bb.setCoeff(0, 0, 1, c);
		bb.setCoeff(0, 0, 0, d);

		Bern3D cc = bb.elevateX(2);
		Bern3D dd = bb.elevateX(3);
		Bern3D ee = bb.elevateX(4);

		for (double x = 0; x <= 1; x += 0.5) {
			for (double y = 0; y <= 1; y += 0.5) {
				for (double z = 0; z <= 1; z += 0.5) {
					double r0 = bb.threadSafeEvalbern3D(x, y, z);
					double r1 = cc.threadSafeEvalbern3D(x, y, z);
					double r2 = dd.threadSafeEvalbern3D(x, y, z);
					double r3 = ee.threadSafeEvalbern3D(x, y, z);
					assertEquals(r0, r1, 1e-9);
					assertEquals(r0, r2, 1e-9);
					assertEquals(r0, r3, 1e-9);
				}
			}
		}

		cc = bb.elevateY(2);
		dd = bb.elevateY(3);
		ee = bb.elevateY(4);

		for (double x = 0; x <= 1; x += 0.5) {
			for (double y = 0; y <= 1; y += 0.5) {
				for (double z = 0; z <= 1; z += 0.5) {
					double r0 = bb.threadSafeEvalbern3D(x, y, z);
					double r1 = cc.threadSafeEvalbern3D(x, y, z);
					double r2 = dd.threadSafeEvalbern3D(x, y, z);
					double r3 = ee.threadSafeEvalbern3D(x, y, z);
					assertEquals(r0, r1, 1e-9);
					assertEquals(r0, r2, 1e-9);
					assertEquals(r0, r3, 1e-9);
				}
			}
		}

		cc = bb.elevateZ(2);
		dd = bb.elevateZ(3);
		ee = bb.elevateZ(4);

		for (double x = 0; x <= 1; x += 0.5) {
			for (double y = 0; y <= 1; y += 0.5) {
				for (double z = 0; z <= 1; z += 0.5) {
					double r0 = bb.threadSafeEvalbern3D(x, y, z);
					double r1 = cc.threadSafeEvalbern3D(x, y, z);
					double r2 = dd.threadSafeEvalbern3D(x, y, z);
					double r3 = ee.threadSafeEvalbern3D(x, y, z);
					assertEquals(r0, r1, 1e-9);
					assertEquals(r0, r2, 1e-9);
					assertEquals(r0, r3, 1e-9);
				}
			}
		}

		cc = bb.elevateX(2);
		dd = cc.elevateY(3);
		ee = dd.elevateZ(4);
		
		for (double x = 0; x <= 1; x += 0.5) {
			for (double y = 0; y <= 1; y += 0.5) {
				for (double z = 0; z <= 1; z += 0.5) {
					double r0 = bb.threadSafeEvalbern3D(x, y, z);
					double r1 = cc.threadSafeEvalbern3D(x, y, z);
					double r2 = dd.threadSafeEvalbern3D(x, y, z);
					double r3 = ee.threadSafeEvalbern3D(x, y, z);
					assertEquals(r0, r1, 1e-9);
					assertEquals(r0, r2, 1e-9);
					assertEquals(r0, r3, 1e-9);
				}
			}
		}
		
	}
	
	@Test
	void test_addBern3D() throws AsurfException {

		// a x + b y + c z + d
		double a = 0.3;
		double b = 0.5;
		double c = 0.7;
		double d = 1.1;

		Bern3D bb = new Bern3D(1, 1, 1);
		bb.setCoeff(1, 0, 0, a);
		bb.setCoeff(0, 1, 0, b);
		bb.setCoeff(0, 0, 1, c);
		bb.setCoeff(0, 0, 0, d);
	
		// A x^2 + B y^2 + C z^2 
		double A = 1.3;
		double B = 1.7;
		double C = 1.9;
		Bern3D cc = new Bern3D(2, 2, 2);
		cc.setCoeff(2, 0, 0, A);
		cc.setCoeff(0, 2, 0, B);
		cc.setCoeff(0, 0, 2, C);
		
		Bern3D dd = Bern3D.addBern3D(bb, cc);
		Bern3D ee = Bern3D.addBern3D(cc, bb);
		Bern3D ff = Bern3D.subtractBern3D(bb, cc);
		Bern3D gg = Bern3D.subtractBern3D(cc, bb);
		
		for (double x = 0; x <= 1; x += 0.5) {
			for (double y = 0; y <= 1; y += 0.5) {
				for (double z = 0; z <= 1; z += 0.5) {
					double r0 = bb.threadSafeEvalbern3D(x, y, z);
					double r1 = cc.threadSafeEvalbern3D(x, y, z);
					double r2 = dd.threadSafeEvalbern3D(x, y, z);
					double r3 = ee.threadSafeEvalbern3D(x, y, z);
					double r4 = ff.threadSafeEvalbern3D(x, y, z);
					double r5 = gg.threadSafeEvalbern3D(x, y, z);
					assertEquals(r0+r1, r2, 1e-9);
					assertEquals(r0+r1, r3, 1e-9);
					assertEquals(r0-r1, r4, 1e-9);
					assertEquals(r1-r0, r5, 1e-9);
				}
			}
		}

		
	}

	@Test
	void test_addBern2D() throws AsurfException {

		// a x + b y  + d
		double a = 0.3;
		double b = 0.5;
		double d = 1.1;

		Bern2D bb = new Bern2D(1, 1);
		bb.setCoeff(1, 0, a);
		bb.setCoeff(0, 1, b);
		bb.setCoeff(0, 0, d);
	
		// A x^2 + B y^2 + C z^2 
		double A = 1.3;
		double B = 1.7;
		Bern2D cc = new Bern2D(2, 2);
		cc.setCoeff(2, 0, A);
		cc.setCoeff(0, 2, B);
		
		Bern2D dd = Bern2D.addBern2D(bb, cc);
		Bern2D ee = Bern2D.addBern2D(cc, bb);
		Bern2D ff = Bern2D.subtractBern2D(bb, cc);
		Bern2D gg = Bern2D.subtractBern2D(cc, bb);
		
		for (double x = 0; x <= 1; x += 0.5) {
			for (double y = 0; y <= 1; y += 0.5) {
					double r0 = bb.evalbern2D(x, y);
					double r1 = cc.evalbern2D(x, y);
					double r2 = dd.evalbern2D(x, y);
					double r3 = ee.evalbern2D(x, y);
					double r4 = ff.evalbern2D(x, y);
					double r5 = gg.evalbern2D(x, y);
					assertEquals(r0+r1, r2, 1e-9);
					assertEquals(r0+r1, r3, 1e-9);
					assertEquals(r0-r1, r4, 1e-9);
					assertEquals(r1-r0, r5, 1e-9);
				}
			}
		
	}

	
	@Test
	void test_EdgeOfBox() throws AsurfException {
		Box_info box = new Box_info(0,0,0,1, null);
		Bern3D bb = new Bern3D(1,2,3);
		for(int i=0;i<=1;++i) {
			for(int j=0;j<=2;++j) {
				for(int k=0;k<=3;++k) {
					bb.setCoeff(i, j, k, Math.random());
				}
			}
		}
		
		check_edge(box, bb, Key3D.FACE_LL, Face_info.Type.X_LOW, Key3D.EDGE_LF);
		check_edge(box, bb, Key3D.FACE_LL, Face_info.Type.X_HIGH, Key3D.EDGE_LB);
		check_edge(box, bb, Key3D.FACE_LL, Face_info.Type.Y_LOW, Key3D.EDGE_LD);
		check_edge(box, bb, Key3D.FACE_LL, Face_info.Type.Y_HIGH, Key3D.EDGE_LU);
		
		check_edge(box, bb, Key3D.FACE_RR, Face_info.Type.X_LOW, Key3D.EDGE_RF);
		check_edge(box, bb, Key3D.FACE_RR, Face_info.Type.X_HIGH, Key3D.EDGE_RB);
		check_edge(box, bb, Key3D.FACE_RR, Face_info.Type.Y_LOW, Key3D.EDGE_RD);
		check_edge(box, bb, Key3D.FACE_RR, Face_info.Type.Y_HIGH, Key3D.EDGE_RU);
		
		check_edge(box, bb, Key3D.FACE_FF, Face_info.Type.X_LOW, Key3D.EDGE_LF);
		check_edge(box, bb, Key3D.FACE_FF, Face_info.Type.X_HIGH, Key3D.EDGE_RF);
		check_edge(box, bb, Key3D.FACE_FF, Face_info.Type.Y_LOW, Key3D.EDGE_FD);
		check_edge(box, bb, Key3D.FACE_FF, Face_info.Type.Y_HIGH, Key3D.EDGE_FU);
		
		check_edge(box, bb, Key3D.FACE_BB, Face_info.Type.X_LOW, Key3D.EDGE_LB);
		check_edge(box, bb, Key3D.FACE_BB, Face_info.Type.X_HIGH, Key3D.EDGE_RB);
		check_edge(box, bb, Key3D.FACE_BB, Face_info.Type.Y_LOW, Key3D.EDGE_BD);
		check_edge(box, bb, Key3D.FACE_BB, Face_info.Type.Y_HIGH, Key3D.EDGE_BU);
		
		check_edge(box, bb, Key3D.FACE_DD, Face_info.Type.X_LOW, Key3D.EDGE_LD);
		check_edge(box, bb, Key3D.FACE_DD, Face_info.Type.X_HIGH, Key3D.EDGE_RD);
		check_edge(box, bb, Key3D.FACE_DD, Face_info.Type.Y_LOW, Key3D.EDGE_FD);
		check_edge(box, bb, Key3D.FACE_DD, Face_info.Type.Y_HIGH, Key3D.EDGE_BD);
		
		check_edge(box, bb, Key3D.FACE_UU, Face_info.Type.X_LOW, Key3D.EDGE_LU);
		check_edge(box, bb, Key3D.FACE_UU, Face_info.Type.X_HIGH, Key3D.EDGE_RU);
		check_edge(box, bb, Key3D.FACE_UU, Face_info.Type.Y_LOW, Key3D.EDGE_FU);
		check_edge(box, bb, Key3D.FACE_UU, Face_info.Type.Y_HIGH, Key3D.EDGE_BU);
		
		
	}

	/**
	 * @param box
	 * @param bb
	 * @param k1
	 * @param k2
	 * @param k3
	 * @throws AsurfException
	 */
	public void check_edge(Box_info box, Bern3D bb, final Key3D k1, final Type k2, final Key3D k3)
			throws AsurfException {
		Face_info ll = box.make_box_face(k1);
		Edge_info edge = ll.make_face_edge(k2);
		Edge_info edge1 = box.make_box_edge(k3);
		assertEquals(edge,edge1);

		Bern2D cc = bb.make_bern2D_of_box(k1);
		Bern1D dd = cc.make_bern1D_of_face(k2);
		Bern1D ee = bb.make_bern1D_of_box(k3);
		
		edge = ll.make_face_edge(k2);
		edge1 = box.make_box_edge(k3);
		assertEquals(edge,edge1);
		for(double x=0;x<=1.0;x+=0.1) {
			double d = dd.evaluate(x);
			double e = ee.evaluate(x);
			assertEquals(d,e,1e-9);
		}
	}
}
