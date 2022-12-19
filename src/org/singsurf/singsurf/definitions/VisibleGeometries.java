package org.singsurf.singsurf.definitions;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jv.project.PgGeometryIf;
import jv.project.PvDisplayIf;
import jv.viewer.PvViewer;

public class VisibleGeometries {
	Map<String,Map<String,Boolean>> displays = new HashMap<String,Map<String,Boolean>>();

	public VisibleGeometries() {
	}

	public VisibleGeometries(PvViewer m_viewer) {
		for(PvDisplayIf disp:m_viewer.getDisplays()) {
			List<PgGeometryIf> visable = Arrays.asList(disp.getVisibleGeometries());		
			addDisplay(disp.getName());

			for(PgGeometryIf geom:disp.getGeometries()) {
				addGeom(disp.getName(),geom.getName(),visable.contains(geom));
			}
		}
	}
	
	public void addDisplay(String name) {
		displays.put(name, new HashMap<String,Boolean>());
	}
	
	public void addGeom(String disp,String geom,boolean vis) {
		Map<String, Boolean> dispmap = displays.get(disp);
		if(dispmap==null) {
			addDisplay(disp);
			dispmap = displays.get(disp);
		}
		dispmap.put(geom, vis);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("<visibleGeometries>\n");
		displays.forEach((String k, Map<String,Boolean> v) -> {
			sb.append("  <display name=\""+k+"\">\n");
			v.forEach((k2,v2) -> {
				sb.append("    <visgeom name=\""+k2+"\" visible=\""+v2+"\" >\n");
			});
			sb.append("  </display>\n");
		});
		sb.append("</visibleGeometries>\n");
		return sb.toString();
	}

    public Set<String> getDisplays() {
        return displays.keySet();
    }

	public Map<String,Map<String,Boolean>> getDisplayAndGeoms() {
		return displays;
	}
	
	
}
