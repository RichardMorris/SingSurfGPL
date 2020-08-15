package org.singsurf.singsurf.test;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.singsurf.singsurf.asurf.Matrix3D;

public class MatrixTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testInverse() {
		Matrix3D mat = new Matrix3D( 1,2,3, 0,1,4, 5,6,0);
		Matrix3D inv = mat.inverse();
		Matrix3D mult = mat.mult(inv);
		assertTrue(Matrix3D.identity().equalsMat3D(mult));
		Matrix3D mult2 = inv.mult(mat);
		assertTrue(Matrix3D.identity().equalsMat3D(mult2));
		
		double det = mat.det();
		assertEquals(1.0,det,1e-9);

	}

}
