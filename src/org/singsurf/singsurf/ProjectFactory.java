package org.singsurf.singsurf;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import org.singsurf.singsurf.clients.AbstractClient;
import org.singsurf.singsurf.definitions.DefType;
import org.singsurf.singsurf.definitions.Definition;
import org.singsurf.singsurf.definitions.DefinitionReader;
import org.singsurf.singsurf.geometries.GeomStore;

public class ProjectFactory {

	GeomStore store;

	ProjectChooserModel model;
	
	
	class ProjectType {
		DefType type;
		String longName;
		String shortName;
		String egPath;
		String className;
		Class<?> clientClass; //, ipClass;
		List<Definition> defs;
		
		public ProjectType(DefType type, String longName, String shortName, String egPath, String className) throws ClassNotFoundException, IOException {
			super();
			this.longName = longName;
			this.shortName = shortName;
			this.egPath = egPath;
			this.className = className;
			this.type = type;
			clientClass = Class.forName("org.singsurf.singsurf.clients." + className);
//			ipClass = Class.forName("org.singsurf.singsurf.clients." + className + "_IP");
			if(egPath.length()>0) {
				DefinitionReader ldr = store.loadDefs(egPath);
				defs = ldr.getDefs();
			}
			else
				defs = new ArrayList<>();
		}
	}

	List<ProjectType> projectTypes = new ArrayList<>();
	
	public ProjectFactory(ProjectChooserModel model,GeomStore store) {
		this.model = model;
		this.store = store;
		
		for(DefType dt:DefType.knownTypes) {
			String name = dt.toString();
			try {
				ProjectType pt = new ProjectType(dt,
						SingSurfMessages.getString(name+".longName"),
						SingSurfMessages.getString(name+".shortName"),
						SingSurfMessages.getString(name+".egFile"),
						SingSurfMessages.getString(name+".className"));
				projectTypes.add(pt);
			} catch (ClassNotFoundException e) {
//				System.err.println(e.toString());
			} catch (IOException e) {
				System.err.println(e.toString());
			}
		}
		
		
	}
	public ProjectType getProjectType(DefType type) {
		for(ProjectType pt:this.projectTypes) {
			if(pt.type.equals(type))
				return pt;
		}
		System.err.println("ProjectType for "+type+" not found");
		return null;
	}
	
	private AbstractClient createProject(ProjectType type,Definition def) {

		Constructor<?> cons;
		AbstractClient newsurf = null;
		try {
			cons = type.clientClass.getConstructor(new Class[] { GeomStore.class, Definition.class });
			newsurf = (AbstractClient) cons.newInstance(new Object[] { store, def });
			
			return newsurf;
		} catch (Exception e) {
			System.err.println(e.getCause().toString());
			for (StackTraceElement ste : e.getCause().getStackTrace()) {
				System.err.println(ste);
				if (ste.getClassName().equals(this.getClass().getName())) {
					break;
				}
			}
			return null;
		}
	}
	
	/**
	 * Builder for default def
	 * @param defType
	 * @return
	 */
	public AbstractClient createProject(DefType defType) {
		ProjectType type = this.getProjectType(defType);
		String projName = model.getUniqueName(type.shortName );

		Constructor<?> cons;
		AbstractClient newsurf = null;
		try {
			cons = type.clientClass.getConstructor(new Class[] { GeomStore.class, String.class });
			newsurf = (AbstractClient) cons.newInstance(new Object[] { store, projName });
			
			return newsurf;
		} catch (Exception e) {
			Throwable cause = e.getCause();
			if(cause!=null) {
			System.err.println(cause.toString());
			
			for (StackTraceElement ste : e.getCause().getStackTrace()) {
				System.err.println(ste);
				if (ste.getClassName().equals(this.getClass().getName())) {
					break;
				}
			}
			}
			else {
				System.err.println(e.toString());
				for (StackTraceElement ste : e.getStackTrace()) {
					System.err.println(ste);
					if (ste.getClassName().equals(this.getClass().getName())) {
						break;
					}
				}
			}
			return null;
		}
	}
	
	/**
	 * Builder with specified def
	 * @param def
	 * @return
	 */
	public AbstractClient createProject(Definition def) {
		ProjectType projType = this.getProjectType(def.getType());
		Definition uniqueDef = def.duplicate();
		String projName = model.getUniqueName(def.getName() );
		uniqueDef.setName(projName);
		
		AbstractClient client = this.createProject(projType, uniqueDef);

		return client;
	}
}
