package org.singsurf.singsurf.asurf;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Before;
import org.junit.Test;
import org.singsurf.singsurf.acurve.AsurfException;

public class Knitter {
	private static final boolean PRINT_QUEUE = false;
	BoxClevA boxclev;
	AtomicInteger knitter_in = new AtomicInteger();
	AtomicInteger knitter_out = new AtomicInteger();

	public Knitter(BoxClevA boxclev) {
		super();
		this.boxclev = boxclev;
	}
	
	public void init(BoxClevA boxclev2) {
		if(boxclev.global_denom > 0) {
			ll = new Face_info(Key3D.FACE_LL, boxclev.global_selx  , boxclev.global_sely, boxclev.global_selz, boxclev.global_denom);
			rr = new Face_info(Key3D.FACE_RR, boxclev.global_selx+1, boxclev.global_sely, boxclev.global_selz, boxclev.global_denom);

			ff = new Face_info(Key3D.FACE_FF, boxclev.global_selx, boxclev.global_sely  , boxclev.global_selz, boxclev.global_denom);
			bb = new Face_info(Key3D.FACE_BB, boxclev.global_selx, boxclev.global_sely+1, boxclev.global_selz, boxclev.global_denom);

			dd = new Face_info(Key3D.FACE_DD, boxclev.global_selx, boxclev.global_sely, boxclev.global_selz  , boxclev.global_denom);
			uu = new Face_info(Key3D.FACE_UU, boxclev.global_selx, boxclev.global_sely, boxclev.global_selz+1, boxclev.global_denom);
		} else {
			ll = new Face_info(Key3D.FACE_LL, 0, 0, 0, 1);
			rr = new Face_info(Key3D.FACE_RR, 1, 0, 0, 1);
			
			ff = new Face_info(Key3D.FACE_FF, 0, 0, 0, 1);
			bb = new Face_info(Key3D.FACE_BB, 0, 1, 0, 1);
			
			dd = new Face_info(Key3D.FACE_DD, 0, 0, 0, 1);
			uu = new Face_info(Key3D.FACE_UU, 0, 0, 1, 1);
		}

	}

	public static class MutableRational implements Comparable<MutableRational> {
		int numerator;
		int denominator;
		
		public MutableRational() {
			super();
            this.numerator = 0;
            this.denominator = 1;
		}
		
		public MutableRational(int numerator, int denominator) {
			super();
            int gcd = (int) gcd(numerator, denominator);
            this.numerator = numerator / gcd;
            this.denominator = denominator / gcd;
		}
		
		public void incrementOld(int num,int denom) {
			
			int top = numerator * denom + num * denominator;
			int bottom = denominator * denom;
			int gcd = gcd(top, bottom);
            this.numerator = top / gcd;
            this.denominator = bottom / gcd;	
		}
		
		public void increment(int num,int denom) {
			
			if(denom == denominator) {
				int top = numerator + num;
				int bottom = denom;
				int gcd = (int) gcd(top,bottom);
	            this.numerator = (int) (top / gcd);
	            this.denominator = (int) (bottom / gcd);	
			}
			else if(denom > denominator) {
				int mul = denom / denominator;
				int ck = denom % denominator;
				if(ck!=0) throw new ArithmeticException("incrementing "+this+" by "+num+"/"+denom);
				int top = mul * numerator + num;
				int bottom = denom;
				int gcd = (int) gcd(top,bottom);
	            this.numerator = (int) (top / gcd);
	            this.denominator = (int) (bottom / gcd);	
			}
			else if(denom < denominator) {
				int mul = denominator / denom;
				int ck = denominator % denom;
				if(ck!=0) throw new ArithmeticException("incrementing "+this+" by "+num+"/"+denom);
				int top = numerator + mul * num;
				int bottom = denominator;
				int gcd = (int) gcd(top,bottom);
	            this.numerator = (int) (top / gcd);
	            this.denominator = (int) (bottom / gcd);	
			}
		}
		
		@Override
		public String toString() {
			return "" + numerator + "/" + denominator;
		}
		
	    static int gcd(int a, int b) {
	        int a2 = a;
	        int b2 = b;
	        while (b2 != 0) {
	            int t = b2;
	            b2 = a2 % b2;
	            a2 = t;
	        }
	        return a2;
	    }

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + denominator;
			result = prime * result + numerator;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			MutableRational other = (MutableRational) obj;
			if (denominator != other.denominator)
				return false;
			if (numerator != other.numerator)
				return false;
			return true;
		}
		
		public boolean isComplete() {
			return this.numerator >= this.denominator;
		}

		public void incrementSqr(int t, int b) {
			increment(t*t,b*b);
		}

		@Override
		public int compareTo(MutableRational num) {
	        if (this.denominator == num.denominator) {
	            return Integer.compare(numerator,num.numerator);
	        }
	        Integer ad = this.numerator * num.denominator;
	        Integer bc = this.denominator * num.numerator;
	        return ad.compareTo(bc);
		}
	}
	
	public class BoxWithAdjacency {
		Box_info box;
		MutableRational matched_ll;
		MutableRational matched_rr;
		MutableRational matched_ff;
		MutableRational matched_bb;
		MutableRational matched_dd;
		MutableRational matched_uu;		

		BoxWithAdjacency(Box_info box) {
			this.box = box;
			box.adjacency = this;
			matched_ll = (box.xl ==0 ? new MutableRational(1,1) : new MutableRational(0,1));
			matched_ff = (box.yl ==0 ? new MutableRational(1,1) : new MutableRational(0,1));
			matched_dd = (box.zl ==0 ? new MutableRational(1,1) : new MutableRational(0,1));
			matched_rr = (box.xl == box.denom - 1 ) ? new MutableRational(1,1) : new MutableRational(0,1);
			matched_bb = (box.yl == box.denom - 1 ) ? new MutableRational(1,1) : new MutableRational(0,1);
			matched_uu = (box.zl == box.denom - 1 ) ? new MutableRational(1,1) : new MutableRational(0,1);

			if(boxclev.global_denom >0) {
				if( box.xl * boxclev.global_denom == boxclev.global_selx * box.denom)
					matched_ll = new MutableRational(1,1);
				if( (box.xl+1) * boxclev.global_denom == (boxclev.global_selx+1) * box.denom)
					matched_rr = new MutableRational(1,1);

				if( box.yl * boxclev.global_denom == boxclev.global_sely * box.denom)
					matched_ff = new MutableRational(1,1);
				if( (box.yl+1) * boxclev.global_denom == (boxclev.global_sely+1) * box.denom)
					matched_bb = new MutableRational(1,1);

				if( box.zl * boxclev.global_denom == boxclev.global_selz * box.denom)
					matched_dd = new MutableRational(1,1);
				if( (box.zl+1) * boxclev.global_denom == (boxclev.global_selz+1) * box.denom)
					matched_uu = new MutableRational(1,1);
			}
		}
		
		@Override
		public String toString() {
			String  s = box.print_box_header() 
					+ " l " + matched_ll 
					+ " r " + matched_rr
					+ " f " + matched_ff
					+ " b " + matched_bb
					+ " d " + matched_dd
					+ " u " + matched_uu
					+ box.count_sols()+" sols";
			return s;
		}
		
		public boolean matchedAll() {
			return matched_ll.isComplete() && matched_rr.isComplete() 
				&& matched_ff.isComplete() && matched_bb.isComplete()
				&& matched_dd.isComplete() && matched_uu.isComplete();
		}

		List<Face_info> plotFaces = new ArrayList<>();
		public void addFace(Face_info common_face) {
			if(!plotFaces.contains(common_face))
				plotFaces.add(common_face);
		}
	}

	class AdjacentBoxItterator implements Iterator<BoxWithAdjacency> {
		BoxWithAdjacency bwa;
		Deque<Box_info> box_tree = new ArrayDeque<>();
		Box_info testbox;
		
		public AdjacentBoxItterator(BoxWithAdjacency testbox) {
			super();
			this.testbox = testbox.box;
			this.bwa = testbox;
			box_tree.add(boxclev.whole_box);
		}

		@Override
		public boolean hasNext() {
			if(box_tree.isEmpty())
				return false;
			
			Box_info tail = box_tree.peek();
			if(tail.adjacency != null) {
				if(tail.adjacency.matchedAll()) {
					box_tree.pop();
					return hasNext();
				}
				return true;
			}
			box_tree.pop();
			test_and_push(tail.lfd);
			test_and_push(tail.lfu);
			test_and_push(tail.lbd);
			test_and_push(tail.lbu);
			test_and_push(tail.rfd);
			test_and_push(tail.rfu);
			test_and_push(tail.rbd);
			test_and_push(tail.rbu);
			return hasNext();				
			
		}

		private void test_and_push(Box_info box) {
			if(box==null)
				return;
			if( bwa.box.xl  *  box.denom > (box.xl+1) * bwa.box.denom ) return; 
			if( (bwa.box.xl+1)  *  box.denom < (box.xl) * bwa.box.denom ) return;

			if( bwa.box.yl  *  box.denom > (box.yl+1) * bwa.box.denom ) return; 
			if( (bwa.box.yl+1)  *  box.denom < (box.yl) * bwa.box.denom ) return;

			if( bwa.box.zl  *  box.denom > (box.zl+1) * bwa.box.denom ) return;
			if( (bwa.box.zl+1)  *  box.denom < (box.zl) * bwa.box.denom ) return;

			if( bwa.box.denom == box.denom && bwa.box.xl == box.xl && bwa.box.yl == box.yl && bwa.box.zl == box.zl ) return;
			
			box_tree.push(box);
		}

		@Override
		public BoxWithAdjacency next() {
			return box_tree.pop().adjacency;
		}	
	}	
	
	private Face_info ll=null, rr=null, ff=null, bb=null, dd=null, uu=null;

	public void queuebox(Box_info box) throws AsurfException {
		if(box.lfd != null) {
			queuebox(box.lfd);
			queuebox(box.lfu);
			queuebox(box.lbd);
			queuebox(box.lbu);
			queuebox(box.rfd);
			queuebox(box.rfu);
			queuebox(box.rbd);
			queuebox(box.rbu);
			return;
		}
		queuebox_leaf(box);

	}
	
	public void queuebox_leaf(Box_info box) throws AsurfException {
		knitter_in.incrementAndGet();
		
		BoxWithAdjacency testbox = new BoxWithAdjacency(box);
		
		if(PRINT_QUEUE)
			BoxClevA.log.println("Queuing " + testbox.toString());
				
		addSelectionPlotFaces(testbox);

		
		Iterator<BoxWithAdjacency> itt = new AdjacentBoxItterator(testbox);
		while(itt.hasNext()) {
			BoxWithAdjacency existing = itt.next();
			Key3D key = existing.box.containsFace(testbox.box);

			if(key!=Key3D.NONE) {
				setMatches(key,existing,testbox);
				if(PRINT_QUEUE) {
				BoxClevA.log.println("Matched " + existing.toString());
				}
				addPlotFaces(key,existing,testbox);
			} else {
				key = testbox.box.containsFace(existing.box);
				if(key!=Key3D.NONE) {
					setMatches(key,testbox,existing);
					if(PRINT_QUEUE) {
					BoxClevA.log.println("Matched " + existing.toString());
					}
					addPlotFaces(key,testbox,existing);					
				}
			}
			if(existing.matchedAll()) {
				finished(existing);				
			}
			if(testbox.matchedAll()) {
				break;
			}
		}
		if(PRINT_QUEUE)
			BoxClevA.log.println("Done Q   " + testbox.toString()+"\n");
		
		if(testbox.matchedAll()) {
			finished(testbox);
		}
	}		

	private void finished(BoxWithAdjacency bwa) throws AsurfException {
		knitter_out.incrementAndGet();
		if(PRINT_QUEUE) {
			BoxClevA.log.println("Finished " + bwa );
			bwa.plotFaces.forEach(face -> BoxClevA.log.println(face.toString()));
		}
		List<Face_info> faces = bwa.plotFaces;
		bwa.plotFaces = null;
		Box_info box = bwa.box;
		bwa.box = null;
		bwa.matched_ll = bwa.matched_rr = bwa.matched_ff = bwa.matched_bb = bwa.matched_dd = bwa.matched_uu = null;
		box.adjacency = null;
		boxclev.facet_triangulate_plot_and_free(box, faces);
		bwa.plotFaces = null;
	}

	public String getProgress() {
		return "Knitter done "+knitter_out+" holding "+(knitter_in.get()-knitter_out.get());
	}
	
	private void setMatches(Key3D key, BoxWithAdjacency box1, BoxWithAdjacency box2) {
		switch(key) {
		case FACE_LL:
			box1.matched_ll.incrementSqr(box1.box.denom,box2.box.denom);
			box2.matched_rr.incrementSqr(box2.box.denom,box1.box.denom);
			break;
		case FACE_RR:
			box1.matched_rr.incrementSqr(box1.box.denom,box2.box.denom);
			box2.matched_ll.incrementSqr(box2.box.denom,box1.box.denom);
			break;
		case FACE_FF:
			box1.matched_ff.incrementSqr(box1.box.denom,box2.box.denom);
			box2.matched_bb.incrementSqr(box2.box.denom,box1.box.denom);
			break;
		case FACE_BB:
			box1.matched_bb.incrementSqr(box1.box.denom,box2.box.denom);
			box2.matched_ff.incrementSqr(box2.box.denom,box1.box.denom);
			break;
		case FACE_DD:
			box1.matched_dd.incrementSqr(box1.box.denom,box2.box.denom);
			box2.matched_uu.incrementSqr(box2.box.denom,box1.box.denom);
			break;
		case FACE_UU:
			box1.matched_uu.incrementSqr(box1.box.denom,box2.box.denom);
			box2.matched_dd.incrementSqr(box2.box.denom,box1.box.denom);
			break;
		case NONE:
			break;
		default:
			throw new IllegalStateException("Bad key" + key);
		}
	}

	/**
	 * 
	 * @param key of the larger facet
	 * @param larger
	 * @param smaller
	 */
	private void addPlotFaces(Key3D key, BoxWithAdjacency larger, BoxWithAdjacency smaller) {
		Face_info common_face = smaller.box.getFace(key.oppositeFace());
		if(common_face==null)
			return;
		larger.addFace(common_face);
		smaller.addFace(common_face);
//		if(PRINT_QUEUE)
//		BoxClevA.log.println("cleanFacets face "+common_face.print_face_brief());
	}

	private void addSelectionPlotFaces(BoxWithAdjacency bwa) {
		if( ll.contains(bwa.box))
			bwa.addFace(bwa.box.ll);
		if(rr.contains(bwa.box))
			bwa.addFace(bwa.box.rr);
		if(ff.contains(bwa.box))
			bwa.addFace(bwa.box.ff);
		if(bb.contains(bwa.box))
			bwa.addFace(bwa.box.bb);
		if(dd.contains(bwa.box))
			bwa.addFace(bwa.box.dd);
		if(uu.contains(bwa.box))
			bwa.addFace(bwa.box.uu);
	}

	public static class Tester {

		private BoxClevJavaView boxclev;
		Knitter knitter;

		@Before
		public void setUp() throws Exception {
			boxclev = new  BoxClevJavaView(null, null, null, null,"");
			knitter = new Knitter(boxclev);
			BoxClevA.unsafeRegion = new Region_info(0,1,0,1,0,1);
//			Bern3DContext ctx = new Bern3DContext(1, 1, 1);
	//		BoxClevA.unsafeBern =  ctx.zeroBern();
		}

		@Test
		public void testBoxWithAdjacency() {
			{
				Box_info b7 = new Box_info(0,0,0,8,null);
				BoxWithAdjacency a7 = knitter.new BoxWithAdjacency(b7);
				assertTrue(a7.matched_ll.isComplete());
				assertFalse(a7.matched_rr.isComplete());
				assertTrue(a7.matched_ff.isComplete());
				assertFalse(a7.matched_bb.isComplete());
				assertTrue(a7.matched_dd.isComplete());
				assertFalse(a7.matched_uu.isComplete());
			}
			{
				Box_info b7 = new Box_info(4,4,4,8,null);
				BoxWithAdjacency a7 = knitter.new BoxWithAdjacency(b7);
				assertFalse(a7.matched_ll.isComplete());
				assertFalse(a7.matched_rr.isComplete());
				assertFalse(a7.matched_ff.isComplete());
				assertFalse(a7.matched_bb.isComplete());
				assertFalse(a7.matched_dd.isComplete());
				assertFalse(a7.matched_uu.isComplete());
			}
			{
				Box_info b7 = new Box_info(7,7,7,8,null);
				BoxWithAdjacency a7 = knitter.new BoxWithAdjacency(b7);
				assertFalse(a7.matched_ll.isComplete());
				assertTrue(a7.matched_rr.isComplete());
				assertFalse(a7.matched_ff.isComplete());
				assertTrue(a7.matched_bb.isComplete());
				assertFalse(a7.matched_dd.isComplete());
				assertTrue(a7.matched_uu.isComplete());
			}
			boxclev.global_selx = 4;
			boxclev.global_sely = 3;
			boxclev.global_selz = 2;
			boxclev.global_denom = 8;
			{
				Box_info b7 = new Box_info(8,7,4,16,null);
				BoxWithAdjacency a7 = knitter.new BoxWithAdjacency(b7);
				assertTrue(a7.matched_ll.isComplete());
				assertFalse(a7.matched_rr.isComplete());
				assertFalse(a7.matched_ff.isComplete());
				assertTrue(a7.matched_bb.isComplete());
				assertTrue(a7.matched_dd.isComplete());
				assertFalse(a7.matched_uu.isComplete());
			}
			{
				Box_info b7 = new Box_info(9,6,5,16,null);
				BoxWithAdjacency a7 = knitter.new BoxWithAdjacency(b7);
				assertFalse(a7.matched_ll.isComplete());
				assertTrue(a7.matched_rr.isComplete());
				assertTrue(a7.matched_ff.isComplete());
				assertFalse(a7.matched_bb.isComplete());
				assertFalse(a7.matched_dd.isComplete());
				assertTrue(a7.matched_uu.isComplete());
			}
		}
		
		
		@Test
		public void testRational() {
			MutableRational r = new MutableRational(0,1);
			r.increment(1,2);
			r.increment(1,4);
			r.increment(1,8);
			r.increment(1,8);
			assertEquals(new MutableRational(1,1),r);
			assertTrue(r.isComplete());
		}

		@Test
		public void testRationalBug() {
			MutableRational r = new MutableRational(5409,16384);
			r.incrementSqr(4, 512);
			assertEquals(new MutableRational(2705,8192),r);
		}

	}

	public void printResults() {
	}

	/**
	 * Basic method does nothing.
	 */
	public void fini() {
	}



}
