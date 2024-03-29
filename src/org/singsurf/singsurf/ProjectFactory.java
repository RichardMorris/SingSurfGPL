package org.singsurf.singsurf;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import org.singsurf.singsurf.clients.AbstractProject;
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
		String defaultDef;
		Class<?> clientClass; //, ipClass;
		List<Definition> defs;

		public ProjectType(DefType type, String longName, String shortName, String egPath, String className, String def) throws ClassNotFoundException, IOException {
			super();
			this.longName = longName;
			this.shortName = shortName;
			this.egPath = egPath;
			this.className = className;
			this.type = type;
			this.defaultDef = def;
			clientClass = Class.forName("org.singsurf.singsurf.clients." + className);
//			ipClass = Class.forName("org.singsurf.singsurf.clients." + className + "_IP");
			if(defaultDef==null) {
				System.out.println("Empty default def for "+type.getType()+" found ["+defaultDef+"]");				
			}
			
			if(egPath!= null && egPath.length()>0) {
				DefinitionReader ldr = store.loadDefs(egPath);
				defs = ldr.getDefs();
			}
			else {
				System.out.println("Empty def file for "+type.getType()+" found ["+egPath+"]");
				defs = new ArrayList<>();
			}
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
						SingSurfMessages.getString(name+".className"),
						SingSurfMessages.getString(name+".defaultDef")
						);
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
	
	private AbstractProject createProject(ProjectType type,Definition def) {

		Constructor<?> cons;
		AbstractProject newproj = null;
		try {
			cons = type.clientClass.getConstructor(new Class[] { GeomStore.class, Definition.class });
			newproj = (AbstractProject) cons.newInstance(new Object[] { store, def });
			return newproj;
			
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
	public AbstractProject createProject(DefType defType) {
		ProjectType type = this.getProjectType(defType);
		Definition def = DefinitionReader.createLsmpDef(type.defaultDef);
		return createProject(type,def);
	}
	/**
	 * Builder for default def
	 * @param defType
	 * @return
	 */
	public AbstractProject createProjectOld(DefType defType) {
		ProjectType type = this.getProjectType(defType);
		String projName = model.getUniqueName(type.shortName );

		Constructor<?> cons;
		AbstractProject newsurf = null;
		try {
			cons = type.clientClass.getConstructor(new Class[] { GeomStore.class, String.class });
			newsurf = (AbstractProject) cons.newInstance(new Object[] { store, projName });
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
	public AbstractProject createProject(Definition def) {
		ProjectType projType = getProjectType(def.getType());
		Definition uniqueDef = def.duplicate();
		String projName = model.getUniqueName(def.getName() );
		uniqueDef.setName(projName);
		
		AbstractProject client = createProject(projType, uniqueDef);

		return client;
	}
}
