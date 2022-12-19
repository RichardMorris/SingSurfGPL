
package org.singsurf.singsurf.asurf;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.singsurf.singsurf.acurve.AsurfException;

public abstract class Boxclev extends BoxClevA {

	/* flags for calculation */
	static final boolean NON_GENERIC_EDGE = true;

	static final int MODE_KNOWN_SING = 1;

	/* purposes of drawing */
	static final boolean PRINT_FIND_EDGE_SOL = false;
	static final boolean PRINT_GEN_BOXES = false;
	static final boolean CHECK_INTERUPTS = true;
	static final boolean USE_2ND_DERIV = false;
	static final boolean USE_ROT_DERIV = true;

	public Boxclev(String description) {
		super(description);
	}

	/**
	 * Main entry point, the constructor 
	 * must be called first.
	 * 
	 * @throws AsurfException
	 **/

	@Override
	public boolean marmain(double aa[][][], Region_info region, int coarse, int fine, int face, int edge)
			throws AsurfException {
		boolean flag;
		RESOLUTION = coarse;
		LINK_SING_LEVEL = fine;
		LINK_FACE_LEVEL = face;
		LINK_EDGE_LEVEL = edge;
		printInput(aa, region, coarse, fine, face, edge);
		globalRegion = region;
		unsafeRegion = region;
		Converger.GOOD_SOL_TOL = this.convtol;

		
		b3context = new Bern3DContext(aa);
		AA = aa;
		b3context.BB = b3context.makeBern3D(aa, region);
		unsafeBern = b3context.BB;

		System.out.println(getOptionsString());

		b3context.Dx = b3context.BB.diffX();
		b3context.Dy = b3context.BB.diffY();
		b3context.Dz = b3context.BB.diffZ();
		b3context.Dxx = b3context.Dx.diffX();
		b3context.Dxy = b3context.Dx.diffY();
		b3context.Dxz = b3context.Dx.diffZ();

		b3context.Dyy = b3context.Dy.diffY();
		b3context.Dyz = b3context.Dy.diffZ();

		b3context.Dzz = b3context.Dz.diffZ();

		// if(global_mode == MODE_KNOWN_SING)
		// calc_known_sings(pl,num_pts);
		whole_box = new Box_info(0, 0, 0, 1, null);
		topology = new Topology(whole_box);
		topology.create_new_faces(whole_box);
		if(global_denom>0) {
			this.global_sel_box = new Box_info(global_selx,global_sely,global_selz,global_denom,null);
			topology.create_new_faces(global_sel_box);
		} else {
			global_sel_box = whole_box;
		}
		this.parallel = 1;
		facets.init(new Bern3DContext(b3context));
		triangulator.init(new Bern3DContext(b3context));
		knitter.init(this);
		plotter.init(new Bern3DContext(b3context));
		if(known_sings!=null) {
			for(var s:known_sings) {
				plotter.plot_point(s);
			}
		}
		starttime = System.currentTimeMillis();
		
		lasttime = starttime;
		print_mem();

		flag = generate_boxes_top(whole_box, b3context.BB);
		knitter.fini();
		endtime = System.currentTimeMillis();
		plotter.fini();
		cleaner.clean();

		print_vertex_count();
		print_mem();
		System.out.printf("time %,dms%n", endtime - starttime);
		print_mem();

//		data.facesolver.printResults();
//		data.boxsolver.printResults();
//		converger.printResults();
		knitter.printResults();
		facets.printResults();
		triangulator.printResults();
		plotter.printResults();
//		if(global_denom<=0)
//				dumpRemaining(); 
		System.out.println();
		System.out.println(region);
		System.out.printf("coarse=%d; fine=%d; face=%d; edge=%d;%n", coarse, fine, face, edge);
		System.out.println(getOptionsString());

		return (flag);
	}

	/**
	 * Prints a summary of memory usage.
	 */
	public void print_mem() {
		Runtime rt = Runtime.getRuntime();
		System.out.printf("start mem used %,dM alloc %,dM  free %,dM max %,dM avaliable %,dM%n",
				(rt.totalMemory() - rt.freeMemory()) / 1024/ 1024, 
				rt.totalMemory() / 1024/ 1024, 
				rt.freeMemory() / 1024/ 1024, 
				rt.maxMemory() / 1024/ 1024,
				(rt.maxMemory() - rt.totalMemory() + rt.freeMemory()) / 1024/ 1024 );
	}

	/**
	 * The main routine for the first pass of the algorithm. This recursively
	 * creates a tree of boxes where each box contains eight smaller boxes, only
	 * those boxes where there might be a solution are considered (i.e. !allonesign
	 * ). The recursion ends when the none of the derivatives have solutions, and a
	 * set depth has been reach or when a greater depth has been reached.
	 * 
	 * @throws AsurfException
	 **/

	long lasttime = 0;
	long starttime;
	private long endtime;

//	final Semaphore available = new Semaphore(1, true);
//	BiPredicate<Box_info,Box_info> predicate = (s,t) -> Math.abs(s.xl-t.xl)>1 || Math.abs(s.yl-t.yl)>1 || Math.abs(s.zl-t.zl)>1;
//	final PermitIssuer<Box_info> permits = new PermitIssuer<Box_info>(predicate,10);

	
	/** Top level gen boxes
	 * Splits each box into separate threads. 
	 * @param box
	 * @param bb
	 * @return
	 * @throws AsurfException
	 */
	boolean generate_boxes_top(final Box_info box, final Bern3D bb) throws AsurfException {
		final BoxGenerator gen = new BoxGenerator(this,new Bern3DContext(this.b3context),box, bb);
		gen.run();
		return (true);
	}

	Map<Box_info,Double> progress = new HashMap<>();

	protected void report_progress(Box_info box, double percent) {
		if(box!=null)
			progress.put(box, percent);
		long curtime = System.currentTimeMillis();
		long diff = curtime - lasttime;
		if (diff > 5000) {
			lasttime = curtime;
			double sum = progress.values().stream().reduce(0.0, (x,y) -> x+y);
			String string = String.format("%nDone %6.2f percent, ", sum * 100.0);
			System.out.print(string);
			print_mem();
			Comparator<Box_info> boxcomp = new Comparator<Box_info>() {

				@Override
				public int compare(Box_info b1, Box_info b2) {
					if(b1.denom != b2.denom) return Integer.compare(b1.denom, b2.denom);
					if(b1.xl != b2.xl) return Integer.compare(b1.xl, b2.xl);
					if(b1.yl != b2.yl) return Integer.compare(b1.yl, b2.yl);
					if(b1.zl != b2.zl) return Integer.compare(b1.zl, b2.zl);
					return 0;
				}
			};

			try {
				progress.entrySet().stream().sorted(Comparator.comparing(Map.Entry::getKey, boxcomp))
					.forEach(ent -> System.out.printf("%s done %.2f%%%n",ent.getKey().getHeader(),100*ent.getValue())); 
//			for(int i=0;i<8;++i) {
//				System.out.printf("Gen %d done %d percent %.2f%%%n",i,generators[i].boxesdone,
//						(100.0 * generators[i].boxesdone) / (RESOLUTION*RESOLUTION*RESOLUTION));
//			}
			} catch(Exception e) {
				e.printStackTrace();
			}
			
			System.out.println(knitter.getProgress());
			System.out.printf("Facets done %d waiting %d%n",
					getFacetsOut(),getFacetsIn()-getFacetsOut());
			System.out.printf("Plotter done %d waiting %d%n",
					getPlotterOut(),getPlotterIn()-getPlotterOut());
		}	
	}


	@Override
	protected long getComputeTime() {
		return endtime - starttime;
	}

	@Override
	protected long getMemoryUsed() {
		Runtime rt = Runtime.getRuntime();
		return (rt.totalMemory() - rt.freeMemory()) / 1024;
	}

	static boolean sameEdge(Sol_info s1, Sol_info s2) {
		if (s1.type != s2.type)
			return false;
		switch (s1.type) {
		case X_AXIS:
			if (s1.yl * s2.denom == s2.yl * s1.denom && s1.zl * s2.denom == s2.zl * s1.denom)
				return true;
			else
				return false;
		case Y_AXIS:
			if (s1.xl * s2.denom == s2.xl * s1.denom && s1.zl * s2.denom == s2.zl * s1.denom)
				return true;
			else
				return false;
		case Z_AXIS:
			if (s1.xl * s2.denom == s2.xl * s1.denom && s1.yl * s2.denom == s2.yl * s1.denom)
				return true;
			else
				return false;
		default:
			return false;
		}
	}


	protected boolean check_sane_facets(Box_info box) {
		double pos[];
		if (box.facets != null) {
			for (Facet_info f : box.facets) {
				Facet_sol s = f.sols;
				while (s != null) {
					pos = box.calc_pos_in_box(s.sol);
					if (pos[0] < 0.0 || pos[0] > 1.0 || pos[1] < 0.0 || pos[1] > 1.0 || pos[2] < 0.0 || pos[2] > 1.0)
						return false;
					s = s.next;
				}
			}
		}
		return true;
	}

	protected void triangulate_and_plot(Box_info box) {
		if (triangulate != 0)
			triangulator.triangulate_facets(box);
		else
			triangulator.count_edges(box);
		if (!check_sane_facets(box)) {
			System.out.println("Insane facets");
			System.out.print(box);
			System.out.print(box.facets);
		}
		plotter.plot_box(box);
	}

	@Override
	protected void facet_triangulate_plot_and_free(Box_info box,List<Face_info> faces) {
			facets.make_facets(box,faces);
			if (triangulate != 0)
				triangulator.triangulate_facets(box);
			else
				triangulator.count_edges(box);

			if (!check_sane_facets(box)) {
				System.out.println("Insane facets");
				System.out.print(box);
				System.out.print(box.facets);
			}
			plotter.plot_box(box);
			box.free_safe_bit(true, true, true); 
			box.release_from_parent();
			box=null;
	}

	protected void lockBox(Box_info box) {}

	protected void unlockBox(Box_info box) {}

	protected void incrementPloterIn() { ++plotterIn; }

	protected void incrementPlotterOut() { ++plotterOut; }

	protected void incrementFacetsOut() { ++facetsOut; }

	protected void incrementFacetsIn() { ++facetsIn; }

	private int plotterIn=0;
	private int plotterOut=0;
	private int facetsIn=0;
	private int facetsOut=0;
	
	/**
	 * @return
	 */
	int getPlotterIn() {
		return plotterIn;
	}

	/**
	 * @return
	 */
	int getPlotterOut() {
		return plotterOut;
	}

	/**
	 * @return
	 */
	int getFacetsIn() {
		return facetsIn;
	}

	/**
	 * @return
	 */
	int getFacetsOut() {
		return facetsOut;
	}

}