package org.singsurf.singsurf.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

import java.util.Iterator;

import org.junit.Test;
import org.singsurf.singsurf.asurf.CyclicList;
import org.singsurf.singsurf.asurf.Facet_info;
import org.singsurf.singsurf.asurf.Key3D;
import org.singsurf.singsurf.asurf.Sol_info;

public class CyclicListTest {


	@Test
	public void test_list_with_five_elements() {
		CyclicList<String> list = new CyclicList<>();
		list.add("one");
		list.add("two");
		list.add("three");
		list.add("four");
		list.add("five");
		
		assertEquals("one", list.getCyclic(0));
		assertEquals("five", list.getCyclic(4));
		assertEquals("one", list.getCyclic(5));
		assertEquals("one", list.getCyclic(10));
		assertEquals("one", list.getCyclic(-5));
		assertEquals("one", list.getCyclic(-10));
		assertEquals(CyclicList.Direction.Forward,list.adjacent("one", "two"));
		assertEquals(CyclicList.Direction.Forward,list.adjacent("five", "one"));

		assertEquals(CyclicList.Direction.Backward,list.adjacent("two", "one"));
		assertEquals(CyclicList.Direction.Backward,list.adjacent("one", "five"));

		assertEquals(CyclicList.Direction.Separate,list.adjacent("one", "four"));

		assertEquals(CyclicList.Direction.NoFound,list.adjacent("one", "zero"));
		
		{
		String s1="";
		Iterator<String> forwardFromOne = list.forwardIteratorFrom("one");
		while(forwardFromOne.hasNext()) {
			s1 += forwardFromOne.next()+" ";
		}
		assertEquals("one two three four five ",s1);
		}
		
		{
		String s2="";
		Iterator<String> forwardFromTwo = list.forwardIteratorFrom("two");
		while(forwardFromTwo.hasNext()) {
			s2 += forwardFromTwo.next()+" ";
		}
		assertEquals("two three four five one ",s2);
		}
		
		{
		String s3="";
		Iterator<String> forwardFromFive = list.forwardIteratorFrom("five");
		while(forwardFromFive.hasNext()) {
			s3 += forwardFromFive.next()+" ";
		}
		assertEquals("five one two three four ",s3);
		}
		
		{
		String s1b="";
		Iterator<String> backwardFromOne = list.backwardIteratorFrom("one");
		while(backwardFromOne.hasNext()) {
			s1b += backwardFromOne.next()+" ";
		}
		assertEquals("one five four three two ",s1b);
		}
		
		{
		String s2b="";
		Iterator<String> backwardFromTwo = list.backwardIteratorFrom("two");
		while(backwardFromTwo.hasNext()) {
			s2b += backwardFromTwo.next()+" ";
		}
		assertEquals("two one five four three ",s2b);
		}
		
		{
		String s3b="";
		Iterator<String> backwardFromFive = list.backwardIteratorFrom("five");
		while(backwardFromFive.hasNext()) {
			s3b += backwardFromFive.next()+" ";
		}
		assertEquals("five four three two one ",s3b);
		}
		
		{
		String s4="";
		Iterator<String> forwardFromOneToFour = list.forwardIteratorFromTo("one","four");
		while(forwardFromOneToFour.hasNext()) {
			s4 += forwardFromOneToFour.next()+" ";
		}
		assertEquals("one two three ",s4);
		}

		{
		String s4="";
		Iterator<String> forwardFromFourToTwo = list.forwardIteratorFromTo("four","two");
		while(forwardFromFourToTwo.hasNext()) {
			s4 += forwardFromFourToTwo.next()+" ";
		}
		assertEquals("four five one ",s4);
		}

		{
		String s4="";
		Iterator<String> backwardFromOneToFour = list.backwardIteratorFromTo("one","four");
		while(backwardFromOneToFour.hasNext()) {
			s4 += backwardFromOneToFour.next()+" ";
		}
		assertEquals("one five ",s4);
		}

		{
		String s4="";
		Iterator<String> backwardFromFourToTwo = list.backwardIteratorFromTo("four","two");
		while(backwardFromFourToTwo.hasNext()) {
			s4 += backwardFromFourToTwo.next()+" ";
		}
		assertEquals("four three ",s4);
		}

	}

	@Test
	public void test_list_with_two_elements() {
		CyclicList<String> list = new CyclicList<>();
		list.add("one");
		list.add("two");
		
		assertEquals("one", list.getCyclic(0));
		assertEquals("two", list.getCyclic(1));
		assertEquals("two", list.getCyclic(5));
		assertEquals("one", list.getCyclic(10));
		assertEquals("two", list.getCyclic(-5));
		assertEquals("one", list.getCyclic(-10));
		assertEquals(CyclicList.Direction.Forward,list.adjacent("one", "two"));
		assertEquals(CyclicList.Direction.Backward,list.adjacent("two", "one"));
		assertEquals(CyclicList.Direction.NoFound,list.adjacent("one", "zero"));
		
		String s1="";
		Iterator<String> forwardFromOne = list.forwardIteratorFrom("one");
		while(forwardFromOne.hasNext()) {
			s1 += forwardFromOne.next()+" ";
		}
		assertEquals("one two ",s1);
		
		String s2="";
		Iterator<String> forwardFromTwo = list.forwardIteratorFrom("two");
		while(forwardFromTwo.hasNext()) {
			s2 += forwardFromTwo.next()+" ";
		}
		assertEquals("two one ",s2);

		String s1b="";
		Iterator<String> backwardFromOne = list.backwardIteratorFrom("one");
		while(backwardFromOne.hasNext()) {
			s1b += backwardFromOne.next()+" ";
		}
		assertEquals("one two ",s1b);
		
		String s2b="";
		Iterator<String> backwardFromTwo = list.backwardIteratorFrom("two");
		while(backwardFromTwo.hasNext()) {
			s2b += backwardFromTwo.next()+" ";
		}
		assertEquals("two one ",s2b);
	}

	@Test
	public void test_list_with_one_elements() {
		CyclicList<String> list = new CyclicList<>();
		list.add("one");
		
		assertEquals("one", list.getCyclic(0));
		assertEquals("one", list.getCyclic(1));
		assertEquals("one", list.getCyclic(10));
		assertEquals("one", list.getCyclic(-5));
		assertEquals("one", list.getCyclic(-10));
		assertEquals(CyclicList.Direction.Identical,list.adjacent("one", "one"));
		assertEquals(CyclicList.Direction.NoFound,list.adjacent("one", "zero"));
		
		String s1="";
		Iterator<String> forwardFromOne = list.forwardIteratorFrom("one");
		while(forwardFromOne.hasNext()) {
			s1 += forwardFromOne.next()+" ";
		}
		assertEquals("one ",s1);
		
		String s1b="";
		Iterator<String> backwardFromOne = list.backwardIteratorFrom("one");
		while(backwardFromOne.hasNext()) {
			s1b += backwardFromOne.next()+" ";
		}
		assertEquals("one ",s1b);		
	}

	@Test
	public void test_list_with_no_elements() {
		CyclicList<String> list = new CyclicList<>();

		assertEquals(CyclicList.Direction.NoFound,list.adjacent("one", "zero"));
		
		Iterator<String> forwardFromOne = list.forwardIteratorFrom("one");
		assertFalse(forwardFromOne.hasNext());
	}
	

	@Test
	public void testFacetNextPrev() {
		Facet_info facet1 = new Facet_info();

		Sol_info s1 = new Sol_info(Key3D.BOX,1, 0, 0, 8, 0);
		Sol_info s2 = new Sol_info(Key3D.BOX,2, 0, 0, 8, 0);
		Sol_info s3 = new Sol_info(Key3D.BOX,3, 0, 0, 8, 0);
		Sol_info s4 = new Sol_info(Key3D.BOX,4, 0, 0, 8, 0);
		Sol_info s5 = new Sol_info(Key3D.BOX,5, 0, 0, 8, 0);
		Sol_info s6 = new Sol_info(Key3D.BOX,6, 0, 0, 8, 0);

		facet1.addSol(s5);
		facet1.addSol(s4);
		facet1.addSol(s3);
		facet1.addSol(s2);
		facet1.addSol(s1);
		
		assertEquals(s1,facet1.nextSol(s5));
		assertEquals(s4,facet1.prevSol(s5));

		assertEquals(s4,facet1.nextSol(s3));
		assertEquals(s2,facet1.prevSol(s3));

		assertEquals(s2,facet1.nextSol(s1));
		assertEquals(s5,facet1.prevSol(s1));
		assertNull(facet1.nextSol(s6));
		assertNull(facet1.prevSol(s6));
		
		Facet_info facet2 = new Facet_info();
		facet2.addSol(s2);
		facet2.addSol(s1);
		assertEquals(s1,facet2.nextSol(s2));
		assertEquals(s1,facet2.prevSol(s2));

		assertEquals(s2,facet2.nextSol(s1));
		assertEquals(s2,facet2.prevSol(s1));

		Facet_info facet3 = new Facet_info();
		facet3.addSol(s1);
		assertEquals(s1,facet3.nextSol(s1));
		assertEquals(s1,facet3.prevSol(s1));
	
	}

}
