package org.singsurf.singsurf;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.concurrent.CountDownLatch;

import org.singsurf.singsurf.calculators.Calculator;
import org.singsurf.singsurf.clients.AbstractOperatorProject;
import org.singsurf.singsurf.clients.AbstractProject;
import org.singsurf.singsurf.definitions.ProjectComponents;
import org.singsurf.singsurf.geometries.GeomListener;
import org.singsurf.singsurf.geometries.GeomStore;

/**
 * A Runnable that can be used to evaluate a ProjectComponent
 * 
 * The {@link #run()} will block until all the dependent geometries are available.
 */
public class ProjectComponentEvaluator implements GeomListener, Runnable {
	final ProjectComponents pc;
	final AbstractProject proj;
	final CountDownLatch latch;
	final List<String> done = new ArrayList<>();
	final GeomStore store;
	
	public ProjectComponentEvaluator(ProjectComponents pc, AbstractProject client, GeomStore store) {
		this.pc = pc;
		this.proj = client;
		this.latch = new CountDownLatch(pc.getInputs().size());
		this.store = store;
		store.addGeomListner(this);
		System.out.println("ProjectComponentEvaluator:\n"+pc);
	}

	@Override
	public void refreshList(SortedSet<String> list) {
		// TODO Auto-generated method stub

	}

	@Override
	public void geometryHasChanged(String geomName) {
		if(done.contains(geomName)) {
			System.out.println("PCE: Geometry already done "+geomName);
			return;
		}

		if(pc.getInputs().contains(geomName)) {
			System.out.println("PCE: Geometry completed"+geomName);
			((AbstractOperatorProject) proj).newActiveInput(geomName);
			done.remove(geomName);
			latch.countDown();
		}
	}

	@Override
	public void geometryNameHasChanged(String oldName, String newName) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeGeometry(String geomName, boolean rmDependants) {
		// TODO Auto-generated method stub

	}

	@Override
	public void geometryDefHasChanged(AbstractProject client, Calculator inCalc) {
		// TODO Auto-generated method stub

	}

	@Override
	public void run() {
		try {
			latch.await();
			store.removeListerner(this);
			proj.loadProjectComponents(pc);		
		} catch (InterruptedException e) {
			System.out.println(e.toString());
		}

		
	}

}
