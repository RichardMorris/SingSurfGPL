package org.singsurf.singsurf.asurf;

import org.singsurf.singsurf.asurf.PlotAbstract.PlotMode;

import jv.geom.PgElementSet;
import jv.geom.PgPointSet;
import jv.geom.PgPolygonSet;

public class BoxClevJavaView extends BoxClevThreaded {

 
	public BoxClevJavaView(PgElementSet outSurf, PgPolygonSet outCurve, PgPointSet outPoints, PlotMode plot_mode,String descript) {
		super(descript);
		triangulator = new Triangulator(this);
		facets = new Facets(this);
		plotter = new PlotJV(this, outSurf, outCurve, outPoints, plot_mode);
		cleaner = new MeshCleaner(this, outSurf, outCurve, outPoints);
		knitter = new ThreadedKnitter(this);
		log = System.out;

	}



}
