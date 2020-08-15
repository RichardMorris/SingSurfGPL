/*
	Created 23 May 2011 - Richard Morris
*/
package org.singsurf.singsurf;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.SortedSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.singsurf.singsurf.calculators.Calculator;
import org.singsurf.singsurf.clients.AbstractClient;
import org.singsurf.singsurf.geometries.GeomStore;
import org.singsurf.singsurf.geometries.SSGeomListener;

import jv.project.PgGeometryIf;
import jv.viewer.PvViewer;

public class ProjectChooserModel  implements SSGeomListener {
    List<AbstractClient> projects = new ArrayList<AbstractClient>();
    List<ListItem> items = new ArrayList<ListItem>();

    ProjectChooserView view;
	PvViewer m_viewer;

    
    class ListItem {
    	AbstractClient project;
    	PgGeometryIf geom;
		public ListItem(AbstractClient project, PgGeometryIf geom) {
			super();
			this.project = project;
			this.geom = geom;
		}
		@Override
		public String toString() {
			return "Proj "+project.getName()+" geom "+ (geom==null ? "null" : geom.getName() );
		}
    	
		
    }

    public ProjectChooserModel(GeomStore store, PvViewer m_viewer2) {
    	store.addGeomListner(this);
    	this.m_viewer = m_viewer2;
    }

    public void setView(ProjectChooserView view) {
    	this.view = view;
    }
    
    public void rebuild() {
//    	System.out.println("Model.rebuild ");
    	items.clear();
    	for(AbstractClient project:projects) {
    		if(project==null)
    			continue;
//        	System.out.println("Project "+project.getName());
            items.add(new ListItem(project,null));
            for(PgGeometryIf geom: project.getOutputGeoms()) {
            	if(geom!=null) {
//            	System.out.println("Project "+project.getName()+" "+geom.getName());
            	items.add(new ListItem(project,geom));
            	}
            }
    	}
    	if(view!=null)
    		view.rebuild();
    }

    public void addProject(AbstractClient project) {
        projects.add(project);
        rebuild();
    }

    public void removeProject(AbstractClient project) {
        projects.remove(project);
        rebuild();
    }
    
    public AbstractClient getProject(int i) {
        return projects.get(i);
    }

    public List<AbstractClient> getProjects() {
        return projects;
    }
 

	@Override
	public void refreshList(SortedSet<String> list) {
		this.rebuild();
	}

	@Override
	public void geometryHasChanged(String geomName) {
	}

	@Override
	public void geometryNameHasChanged(String oldName, String newName) {
		this.rebuild();
	}

	@Override
	public void removeGeometry(String geomName, boolean rmDependants) {
		rebuild();
	}

	@Override
	public void geometryDefHasChanged(AbstractClient client, Calculator inCalc) {
	} 
	
	boolean containsProjectName(String name) {
		for(AbstractClient proj : projects) {
			if(proj!=null && proj.getName().equals(name))
				return true;
		}
		return false;
	}
    
	/**
	 * Returns a unique name different to argument
	 * @param projName name of existing project
	 * @return a name not used by any project
	 */
    String getCloneName(String projName) {
    	Pattern pat =  Pattern.compile("(.*?)(\\d+)");
    	
    	String root = projName;
    	int num = 0;
    	Matcher m = pat.matcher(projName);
    	if(m.matches()) {
    		root = m.group(1);
    		num = Integer.parseInt(m.group(2));
    	}
    	String newName;
    	do {
        	++num;
        	newName = root + num;
    	} while( this.containsProjectName(newName) );
    	return newName;
    }

    /**
     * Returns a unique name which might be argument
     * @param shortName initial name to try
	 * @return a name not used by any project
     */
	public String getUniqueName(String shortName) {
		if(!this.containsProjectName(shortName))
			return shortName;
		return getCloneName(shortName);
	}

	public AbstractClient getProject(String name) {
		for(AbstractClient ele:projects) {
			if(ele.getName().equals(name))
				return ele;
		}
		return null;
	}

	public ListItem getNearbyItem(ListItem item) {
		int index = items.indexOf(item);
		Iterator<ListItem> itt = items.listIterator(index+1);
		while(itt.hasNext()) {
			ListItem nextItem = itt.next();
			if(nextItem.project != item.project)
				return nextItem;
		}
		ListIterator<ListItem> itt2 = items.listIterator(index);
		while(itt2.hasPrevious()) {
			ListItem prevItem = itt2.previous();
			if(prevItem.project != item.project)
				return prevItem;
		}
		return null;
	}
    
    
}
