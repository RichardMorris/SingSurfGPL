package org.singsurf.singsurf.test;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.singsurf.singsurf.asurf.BoxClevA;
import org.singsurf.singsurf.asurf.EdgeAdjacencyList;
import org.singsurf.singsurf.asurf.Facet_info;
import org.singsurf.singsurf.asurf.Key3D;
import org.singsurf.singsurf.asurf.Region_info;
import org.singsurf.singsurf.asurf.Sol_info;

public class AdjacencyListTest {

	EdgeAdjacencyList eal;
	
	@Before
	public void setUp() throws Exception {
		eal = new EdgeAdjacencyList();
		BoxClevA.unsafeRegion = new Region_info(0,1,0,1,0,1);
	}

	@Test
	public void test() {
		Facet_info facet1 = new Facet_info();

		Sol_info s1 = new Sol_info(Key3D.BOX,1, 0, 0, 8, 0);
		Sol_info s2 = new Sol_info(Key3D.BOX,2, 0, 0, 8, 0);
		Sol_info s3 = new Sol_info(Key3D.BOX,3, 0, 0, 8, 0);
		Sol_info s4 = new Sol_info(Key3D.BOX,4, 0, 0, 8, 0);
		Sol_info s5 = new Sol_info(Key3D.BOX,5, 0, 0, 8, 0);

		facet1.addSol(s5);
		facet1.addSol(s4);
		facet1.addSol(s3);
		facet1.addSol(s2);
		facet1.addSol(s1);

		eal.add(facet1);

		assertEquals(5,eal.edge_adjacent_to_vert.size());
//		assertEquals(5,eal.facets_adjacent_to_vert.size());
		System.out.println(eal.toString());
	}

	@Test
	public void testKummerBug4() {

		Sol_info s0 = new Sol_info(Key3D.BOX,0, 0, 0, 8, 0);
		Sol_info s1 = new Sol_info(Key3D.BOX,1, 0, 0, 8, 0);
		Sol_info s2 = new Sol_info(Key3D.BOX,2, 0, 0, 8, 0);
		Sol_info s3 = new Sol_info(Key3D.BOX,3, 0, 0, 8, 0);
		Sol_info s4 = new Sol_info(Key3D.BOX,4, 0, 0, 8, 0);
		Sol_info s5 = new Sol_info(Key3D.BOX,5, 0, 0, 8, 0);
		Sol_info s6 = new Sol_info(Key3D.BOX,6, 0, 0, 8, 0);
		Sol_info s7 = new Sol_info(Key3D.BOX,7, 0, 0, 8, 0);
		Sol_info s8 = new Sol_info(Key3D.BOX,8, 0, 0, 8, 0);
		Sol_info s9 = new Sol_info(Key3D.BOX,9, 0, 0, 8, 0);
		Sol_info s10 = new Sol_info(Key3D.BOX,10, 0, 0, 8, 0);

		s0.adjNum = 0;
		s1.adjNum = 1;
		s2.adjNum = 2;
		s3.adjNum = 3;
		s4.adjNum = 4;
		s5.adjNum = 5;
		s6.adjNum = 6;
		s7.adjNum = 7;
		s8.adjNum = 8;
		s9.adjNum = 9;
		s10.adjNum = 10;
		
		
		Facet_info facet1 = new Facet_info();
		facet1.dx= facet1.dy = facet1.dz = 0;
		facet1.addSol(s2);
		facet1.addSol(s1);
		facet1.addSol(s0);
		eal.add(facet1);

		Facet_info facet2 = new Facet_info();
		facet2.dx= facet2.dy = facet2.dz = 1;
		facet2.addSol(s5);
		facet2.addSol(s4);
		facet2.addSol(s3);
		eal.add(facet2);
		
		Facet_info facet3 = new Facet_info();
		facet3.dx= facet3.dy = facet3.dz = 2;
		facet3.addSol(s2);
		facet3.addSol(s0);
		facet3.addSol(s5);
		eal.add(facet3);

		Facet_info facet4 = new Facet_info();
		facet4.dx= facet4.dy = facet4.dz = 3;
		facet4.addSol(s3);
		facet4.addSol(s2);
		facet4.addSol(s5);
		eal.add(facet4);
		
		Facet_info facet5 = new Facet_info();
		facet5.dx= facet5.dy = facet5.dz = 4;
		facet5.addSol(s6);
		facet5.addSol(s7);
		facet5.addSol(s5);
		eal.add(facet5);

		Facet_info facet6 = new Facet_info();
		facet6.dx= facet6.dy = facet6.dz = 5;
		facet6.addSol(s8);
		facet6.addSol(s4);
		facet6.addSol(s5);
		eal.add(facet6);
		
		Facet_info facet7 = new Facet_info();
		facet7.dx= facet7.dy = facet7.dz = 6;
		facet7.addSol(s8);
		facet7.addSol(s5);
		facet7.addSol(s6);
		eal.add(facet7);

		Facet_info facet8 = new Facet_info();
		facet8.dx= facet8.dy = facet8.dz = 7;
		facet8.addSol(s7);
		facet8.addSol(s6);
		facet8.addSol(s8);
		eal.add(facet8);

		Facet_info facet9 = new Facet_info();
		facet9.dx= facet9.dy = facet9.dz = 8;
		facet9.addSol(s9);
		facet9.addSol(s10);
		facet9.addSol(s8);
		eal.add(facet9);
		
		Facet_info facet10 = new Facet_info();
		facet10.dx= facet10.dy = facet10.dz = 9;
		facet10.addSol(s5);
		facet10.addSol(s9);
		facet10.addSol(s8);
		eal.add(facet10);

		Facet_info facet11 = new Facet_info();
		facet11.dx= facet11.dy = facet11.dz = 10;
		facet11.addSol(s5);
		facet11.addSol(s7);
		facet11.addSol(s8);
		eal.add(facet11);

//		assertEquals(5,eal.edge_adjacent_to_vert.size());
//		assertEquals(5,eal.facets_adjacent_to_edge.size());
//		assertEquals(5,eal.facets_adjacent_to_vert.size());
//		System.out.println(eal.toString());

	}

}
