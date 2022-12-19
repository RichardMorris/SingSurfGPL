package org.singsurf.singsurf;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedSet;

import org.singsurf.singsurf.calculators.Calculator;
import org.singsurf.singsurf.clients.AbstractProject;
import org.singsurf.singsurf.definitions.VisibleGeometries;
import org.singsurf.singsurf.geometries.GeomListener;
import org.singsurf.singsurf.geometries.GeomStore;

import jv.project.PgGeometryIf;
import jv.project.PvDisplayIf;
import jv.viewer.PvViewer;

public class VisabilityController implements GeomListener {
	final VisibleGeometries visGeom;
	final Map<String,Boolean> visability = new HashMap<>();
	final GeomStore store;

	public VisabilityController(VisibleGeometries visGeom, GeomStore store2) {
		this.visGeom = visGeom;
		this.store = store2;
		visGeom.getDisplayAndGeoms().forEach((String k, Map<String,Boolean> v) -> {
			v.forEach((k2,v2) -> {
				visability.put(k2, v2);
			});
		});

	}
    
	@Override
	public void refreshList(SortedSet<String> list) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void geometryHasChanged(String geomName) {
		Boolean flag = visability.get(geomName);
		if(flag==null) return;
		PgGeometryIf geom = store.getGeom(geomName);

		
		PvViewer viewer = store.singsurf.m_viewer;
		
		for(Entry<String, Map<String, Boolean>> ent:visGeom.getDisplayAndGeoms().entrySet()) {
			for(PvDisplayIf d: viewer.getDisplays() ) {
				PvDisplayIf display=null;
				if(d.getName().equals(ent.getKey())) {
					display =d;
				}
				if(display==null) continue;
				
				final PvDisplayIf disp2 = display;
				
				ent.getValue().forEach((k,v) -> {
					if(k.equals(geomName)) {
						System.out.println("Adding "+geomName+" to "+disp2.getName()+" flag "+ v);
//						if(disp2.containsGeometry(geom))
//							disp2.removeGeometry(geom);
						geom.setVisible(v);
						disp2.addGeometry(geom);
					}
				});
			}
		}
//		for(PvDisplayIf display: viewer.getDisplays() ) {
//			System.out.println("Geometries");
//			for(PgGeometryIf g : display.getGeometries()) {
//				System.out.println(g.getName());
//			}
//			System.out.println("Visable Geometries");
//			for(PgGeometryIf g : display.getVisibleGeometries()) {
//				System.out.println(g.getName());
//			}
//		}
		
		
		visability.remove(geomName);
		if(visability.isEmpty()) {
			store.removeListerner(this);
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

}
