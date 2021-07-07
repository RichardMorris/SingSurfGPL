package org.singsurf.singsurf.asurf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.singsurf.singsurf.acurve.AsurfException;

public abstract class BoxClevThreaded extends Boxclev {

	public BoxClevThreaded(String description) {
		super(description);
	}

	static class BoxBern {
		Box_info box;
		Bern3D bern;
		private Bern3DContext.OctBern oct;
		
		public BoxBern(Box_info box, Bern3D bern,Bern3DContext ctx) {
			super();
			this.box = box;
			this.bern = bern;
			box.sub_devide_box();
			oct = ctx.reduce(bern);
		}
		
		Box_info getBox(int i) {
			switch(i) {
			case 0:
				return box.lfd;
			case 1:
				return box.rfd;
			case 2:
				return box.lbd;
			case 3:
				return box.rbd;
			case 4:
				return box.lfu;
			case 5:
				return box.rfu;
			case 6:
				return box.lbu;
			case 7:
				return box.rbu;
			default:
				return null;
			}
		}

		Bern3D getBern(int i) {
			switch(i) {
			case 0:
				return oct.lfd;
			case 1:
				return oct.rfd;
			case 2:
				return oct.lbd;
			case 3:
				return oct.rbd;
			case 4:
				return oct.lfu;
			case 5:
				return oct.rfu;
			case 6:
				return oct.lbu;
			case 7:
				return oct.rbu;
			default:
				return null;
			}
		}
		
	}

	class ThreadWithContext extends Thread {
		final Facets facets;
		final Triangulator triang;
		
		public ThreadWithContext(Boxclev boxclev,Runnable runnable) {
			super(runnable);
			facets = new Facets(boxclev);
			triang = new Triangulator(boxclev);
			facets.init(new Bern3DContext(boxclev.b3context));
			triang.init(new Bern3DContext(boxclev.b3context));
		}		
	}
	

	class ContextRunnable implements Runnable {

		Box_info box;
		List<Face_info> faces;
		
		
		public ContextRunnable(Box_info box, List<Face_info> faces) {
			super();
			this.box = box;
			this.faces = faces;
		}


		@Override
		public void run() {
			ThreadWithContext twc = (ThreadWithContext) Thread.currentThread();
			incrementFacetsIn(); //facets_in.incrementAndGet();

			twc.facets.make_facets(box,faces);
			if (triangulate != 0)
				twc.triang.triangulate_facets(box);
			else
				twc.triang.count_edges(box);

			incrementFacetsOut(); // facets_out.incrementAndGet();
			plotExecutor.submit(new PlotRunnable(box));
		}		
	}
	
	public class PlotRunnable implements Runnable {
		Box_info box;
		
		
		public PlotRunnable(Box_info box) {
			super();
			this.box = box;
		}


		@Override
		public void run() {
			incrementPloterIn(); //plotter_in.incrementAndGet();
			plotter.plot_box(box);
			
			box.free_safe_bit(true, true, true); 
			box.release_from_parent();

			incrementPlotterOut(); // plotter_out.incrementAndGet();
		}

	}

	class ThreadWithContextFactory implements ThreadFactory {

		@Override
		public Thread newThread(Runnable r) {
			ThreadWithContext twc =  new ThreadWithContext(BoxClevThreaded.this,r);
			facetThread.add(twc);
			return twc;
		}
		
	}
	
	ExecutorService postProcessService;
	ExecutorService plotExecutor;
	Map<Predicate<Box_info>,Semaphore> dangerZones = new HashMap<>();
	List<Semaphore> signals = new ArrayList<>();
	BoxGenerator[] generators = new BoxGenerator[8];
	List<ThreadWithContext> facetThread = new ArrayList<>();

	boolean is_special_box(Box_info box) {
		if(box.xl * 2 == box.denom) return true;
		if((box.xl+1) * 2 == box.denom) return true;
		if(box.yl * 2 == box.denom) return true;
		if((box.yl+1) * 2 == box.denom) return true;
		if(box.zl * 2 == box.denom) return true;
		if((box.zl+1) * 2 == box.denom) return true;
		return false;
	}

	/** Top level gen boxes
	 * Splits each box into seperate threads. 
	 * @param box
	 * @param bb
	 * @return
	 * @throws AsurfException
	 */
	boolean generate_boxes_top(final Box_info box, final Bern3D bb) throws AsurfException {

		dangerZones.put(b -> ((b.xl *2 == b.denom) || (2*(b.xl+1) == b.denom)) && 2*b.yl <= b.denom && 2*b.zl <= b.denom ,new Semaphore(1,true));
		dangerZones.put(b -> ((b.xl *2 == b.denom) || (2*(b.xl+1) == b.denom)) && 2*b.yl >= b.denom && 2*b.zl <= b.denom ,new Semaphore(1,true));
		dangerZones.put(b -> ((b.xl *2 == b.denom) || (2*(b.xl+1) == b.denom)) && 2*b.yl <= b.denom && 2*b.zl >= b.denom ,new Semaphore(1,true));
		dangerZones.put(b -> ((b.xl *2 == b.denom) || (2*(b.xl+1) == b.denom)) && 2*b.yl >= b.denom && 2*b.zl >= b.denom ,new Semaphore(1,true));

		dangerZones.put(b -> 2*b.xl <= b.denom && ((b.yl *2 == b.denom) || (2*(b.yl+1) == b.denom)) &&  2*b.zl <= b.denom ,new Semaphore(1,true));
		dangerZones.put(b -> 2*b.xl >= b.denom && ((b.yl *2 == b.denom) || (2*(b.yl+1) == b.denom)) &&  2*b.zl <= b.denom ,new Semaphore(1,true));
		dangerZones.put(b -> 2*b.xl <= b.denom && ((b.yl *2 == b.denom) || (2*(b.yl+1) == b.denom)) &&  2*b.zl >= b.denom ,new Semaphore(1,true));
		dangerZones.put(b -> 2*b.xl >= b.denom && ((b.yl *2 == b.denom) || (2*(b.yl+1) == b.denom)) &&  2*b.zl >= b.denom ,new Semaphore(1,true));

		dangerZones.put(b -> 2*b.xl <= b.denom &&  2*b.yl <= b.denom && ((b.zl *2 == b.denom) || (2*(b.zl+1) == b.denom)) ,new Semaphore(1,true));
		dangerZones.put(b -> 2*b.xl >= b.denom &&  2*b.yl <= b.denom && ((b.zl *2 == b.denom) || (2*(b.zl+1) == b.denom)) ,new Semaphore(1,true));
		dangerZones.put(b -> 2*b.xl <= b.denom &&  2*b.yl >= b.denom && ((b.zl *2 == b.denom) || (2*(b.zl+1) == b.denom)) ,new Semaphore(1,true));
		dangerZones.put(b -> 2*b.xl >= b.denom &&  2*b.yl >= b.denom && ((b.zl *2 == b.denom) || (2*(b.zl+1) == b.denom)) ,new Semaphore(1,true));

		
		BoxBern boxbern = new BoxBern(box,bb,this.b3context);

		
		if(!knitFacets || this.littleFacets || this.parallel ==0 ) {
			final BoxGenerator gen = new BoxGenerator(this,new Bern3DContext(this.b3context),box, bb);
			Thread thread = new Thread( gen, "GenBox");
			thread.start();
			try {
				thread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		else {
			for(int i=0;i<8;++i) {
				final Box_info box2 = boxbern.getBox(i);
				final Bern3D bern = boxbern.getBern(i);
				final BoxGenerator gen = new BoxGenerator(this,new Bern3DContext(this.b3context),box2,bern);
				generators[i] = gen;
			}
			
			ExecutorService executor = Executors.newFixedThreadPool(8);
			postProcessService =  Executors.newFixedThreadPool(1,new ThreadWithContextFactory());
			plotExecutor = Executors.newSingleThreadExecutor();

			
			try {
			for(int i=0;i<8;++i) {
				executor.submit(generators[i]);
			}
			executor.shutdown();
			executor.awaitTermination(1, TimeUnit.DAYS);
			} catch (Exception e) {
				e.printStackTrace();
			}
			System.out.println("All boxes generated " + (System.currentTimeMillis() - starttime));
			knitter.fini();
			System.out.println("Knitter in queue empty " + (System.currentTimeMillis() - starttime));
			try {
				Thread.sleep(1);
				postProcessService.shutdown();
				while(!postProcessService.isTerminated()) {
					report_progress(null,0);
					postProcessService.awaitTermination(10, TimeUnit.SECONDS);
				}
				plotExecutor.shutdown();
				while(!plotExecutor.isTerminated()) {
					report_progress(null,0);
					plotExecutor.awaitTermination(10, TimeUnit.SECONDS);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			for(int i=0;i<8;++i) {
				generators[i].PrintResults();
			}
		}
		return (true);
	}

	private AtomicInteger facets_in = new AtomicInteger(0);
	private AtomicInteger facets_out = new AtomicInteger(0);
	private AtomicInteger plotter_in = new AtomicInteger(0);
	private AtomicInteger plotter_out = new AtomicInteger(0);
	
	
	/**
	 * @return
	 */
	@Override 
	int getPlotterIn() {
		return plotter_in.get();
	}

	/**
	 * @return
	 */
	@Override 
	int getPlotterOut() {
		return plotter_out.get();
	}

	/**
	 * @return
	 */
	@Override
	int getFacetsIn() {
		return facets_in.get();
	}

	/**
	 * @return
	 */
	@Override
	int getFacetsOut() {
		return facets_out.get();
	}

	@Override
	protected void facet_triangulate_plot_and_free(Box_info box,List<Face_info> faces) {
		switch(parallel) {
		case 1:
			postProcessService.submit(new ContextRunnable(box,faces) );
			break;
		case 0: {
			incrementFacetsIn();
			facets.make_facets(box,faces);
			if (triangulate != 0)
				triangulator.triangulate_facets(box);
			else
				triangulator.count_edges(box);
			incrementFacetsOut();

			if (!check_sane_facets(box)) {
				BoxClevA.log.println("Insane facets");
				BoxClevA.log.print(box);
				BoxClevA.log.print(box.facets);
			}
			incrementPloterIn();
			plotter.plot_box(box);
			incrementPlotterOut();
//			box.free_safe_bit(false, false, false); 
			box.free_bit(true, true, true); 
			box.release_from_parent();
			box=null;
			
			break; 
		}
		}
	}

	/**
	 * 
	 */
	@Override
	protected void incrementFacetsIn() {
		facets_in.incrementAndGet();
	}

	/**
	 * 
	 */
	@Override
	protected void incrementFacetsOut() {
		facets_out.incrementAndGet();
	}

	/**
	 * 
	 */
	@Override
	protected void incrementPlotterOut() {
		plotter_out.incrementAndGet();
	}

	/**
	 * 
	 */
	@Override
	protected void incrementPloterIn() {
		plotter_in.incrementAndGet();
	}

	/**
	 * @param box
	 */
	protected void unlockBox(final Box_info box) {
		if(is_special_box(box)) {				
			for(Entry<Predicate<Box_info>, Semaphore> ent:dangerZones.entrySet()) {
				if(ent.getKey().test(box)) {
					ent.getValue().release();
				}
			}
		}
	}

	/**
	 * @param box
	 */
	protected void lockBox(final Box_info box) {
		if(is_special_box(box)) {
//			boxclev.permits.aquire(box);			
//			long start = System.nanoTime();
			
			for(Entry<Predicate<Box_info>, Semaphore> ent:dangerZones.entrySet()) {
				if(ent.getKey().test(box)) {
					ent.getValue().acquireUninterruptibly();
				}
			}
		}
	}

	@Override
	public int getNumEdges() {
		int num = triangulator.numDoubleEdges();
		num += facetThread.stream().collect(Collectors.summingInt(t -> t.triang.numDoubleEdges()));
		return num/2;
	}

	public int getNumDoubleEdges() {
		int num = triangulator.numDoubleEdges();
		num += facetThread.stream().collect(Collectors.summingInt(t -> t.triang.numDoubleEdges()));
		return num;
	}

}
