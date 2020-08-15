package org.singsurf.singsurf.asurf;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;

import org.singsurf.singsurf.asurf.PlotAbstract.PlotMode;

public class BoxClevObj extends BoxClevThreaded {


	public BoxClevObj(PlotMode plot_mode,File file,String descript) {
		super(descript);
		triangulator = new Triangulator(this);
		facets = new Facets(this);
		plotter = new PlotObj(this, plot_mode,file);
		cleaner = new AbstractMeshCleaner() {
			@Override
			public void clean() {
				
			}};
		knitter = new ThreadedKnitter(this);
		try {
			log = new PrintStream(new File("asurf.log"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

	}
}
