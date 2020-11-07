package org.singsurf.singsurf;

import java.awt.Dimension;
import java.awt.Panel;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.singsurf.singsurf.ProjectChooserModel.ListItem;
import org.singsurf.singsurf.clients.AbstractClient;
import org.singsurf.singsurf.clients.AbstractOperatorClient;
import org.singsurf.singsurf.definitions.DefType;
import org.singsurf.singsurf.definitions.Definition;
import org.singsurf.singsurf.definitions.DefinitionReader;
import org.singsurf.singsurf.definitions.ProjectComponents;
import org.singsurf.singsurf.geometries.SSGeomListener;

import jv.loader.PgObjLoader;
import jv.project.PgGeometryIf;
import jv.project.PgJvxSrc;
import jv.project.PjProject;
import jv.viewer.PvViewer;
import jvx.loader.PgWrlLoader;

public class ProjectChooserController {

	PvViewer m_viewer;
	SingSurf3D ssp;
	ProjectChooserModel model;
	ProjectFactory factory;
	AbstractClient currentProject;

	public ProjectChooserController(SingSurf3D ssp, PvViewer m_viewer, ProjectChooserModel model) {
		super();
		this.ssp = ssp;
		this.m_viewer = m_viewer;
		this.model = model;
		this.factory = new ProjectFactory(model, ssp.store);
	}


	public void modelItemSelected(ListItem listItem) {
		AbstractClient proj = listItem.project;
		if(proj==null)
			return;
		this.currentProject = proj;
		proj.setEnabledAutoFit(false);
		m_viewer.selectProject(proj);
		displayProjectPanel(proj);
		PgGeometryIf geom = listItem.geom != null 
				? listItem.geom  
						: proj.getGeometry();

		if (geom != null) {
			m_viewer.getDisplay().selectGeometry(geom);
		}
		if(proj instanceof AbstractOperatorClient) {
			((AbstractOperatorClient) proj).selectOutputGeometry(listItem.geom);
		}
	}

	public void displayProjectPanel(PjProject proj) {
		int loc = ssp.splitPaneLeft.getDividerLocation();
		loc = loc < 300 ? 300 : loc;

		Panel pProject = proj.getInfoPanel();
		pProject.setMinimumSize(new Dimension(300,800));
		ssp.splitPaneLeft.setLeftComponent(pProject);
		ssp.splitPaneLeft.setDividerLocation(loc);
		ssp.validate();
	}

	public void clone(ListItem listItem) {
		String newName = model.getCloneName(listItem.project.getName());
		Definition def = listItem.project.getDefinition();
		Definition newdef = def.duplicate();
		newdef.setName(newName);
		createProject(newdef);
	}

	public void saveProj(ListItem item) {
		item.project.saveDef();
	}

	public void appendProj(ListItem item) {
		item.project.saveAppendDef(true);
	}

	public void deleteAll() {
		for(int i=model.items.size()-1;i>0;--i) {
			delProj(model.items.get(i),true,true);
		}
		currentProject=null;
		model.rebuild();
		m_viewer.update(null);
	}

	public void deleteProj(ListItem item,boolean rmGeom, boolean rmDep) {
		delProj(item,rmGeom,rmDep);
		model.rebuild();
		m_viewer.update(null);
	}
	
	private void delProj(ListItem item,boolean rmGeom, boolean rmDep) {
		if(item.geom == null) {

			System.out.println("deleteProject "+item.toString());
			AbstractClient proj = item.project;
			ListItem prevItem = model.getNearbyItem(item);
			if (rmGeom)
				for (PgGeometryIf geom : proj.getOutputGeoms()) {
					ssp.store.removeGeometry(geom, rmDep);
				}
			if(proj == ssp.store.globals)
				return;
			PjProject curproj = m_viewer.getCurrentProject();
			m_viewer.removeProject(proj);
			model.removeProject(proj);
			if(proj instanceof SSGeomListener)
				ssp.store.removeListerner((SSGeomListener) proj);
			if(curproj == proj || curproj == null) {
				this.modelItemSelected(prevItem);
			}
			proj.dispose();
		}
		else {
			System.out.println("deleteGeometry "+item.toString());
			AbstractClient proj = item.project;
			if(proj instanceof AbstractOperatorClient) {
				((AbstractOperatorClient)proj).removeOutpuGeometry(item.geom,rmDep);

			} else {
				proj.removeGeometry(item.geom);
			}
		}
	}

	public void loadProjectFromFile(String filename) {

		try {
			FileReader fr = new FileReader(filename);
			DefinitionReader ldr = new DefinitionReader(new BufferedReader(fr));
			ldr.read();
			fr.close();
			for (Definition def : ldr.getDefs()) {
				if(def.getType().equals(DefType.globals)) {
				} else {
					createProject(def);
				}
			}

			for (ProjectComponents pc : ldr.getProjComp()) {
				if (!pc.isEmpty()) {
					AbstractClient client = model.getProject(pc.getName());
					if (client != null) {
						client.loadProjectComponents(pc, this.ssp);
					} else {
						System.err.println("Null client for project " + pc.getName());
					}
				}

			}
			for (ProjectComponents pc : ldr.getProjComp()) {
				if (!pc.isEmpty()) {
					AbstractClient client = model.getProject(pc.getName());
					if (client != null) {
						client.calcGeoms();
					} else {
						System.err.println("Null client for project " + pc.getName());
					}
				}

			}

		} catch (IOException e) {
			System.out.println("Failed to write to " + filename);
		}

	}

	private void createProject(Definition def) {
		AbstractClient newsurf = factory.createProject(def);
		model.addProject(newsurf);
		//		newsurf.setEnabledAutoFit(fit.getState());
		newsurf.init2();
		m_viewer.addProject(newsurf);
		displayProjectPanel(newsurf);
		currentProject = newsurf;

		if (newsurf.getGeometry() != null)
			m_viewer.getDisplay().selectGeometry(newsurf.getGeometry());
		ssp.validate();
	}

	public void loadProject(Definition def) {
		this.createProject(def);
	}

	/** Method called when no definition specified. */
	public AbstractClient loadProject(DefType type) {
		AbstractClient newsurf = factory.createProject(type);
		model.addProject(newsurf);
		currentProject = newsurf;
		//		newsurf.setEnabledAutoFit(fit.getState());
		newsurf.init2();
		m_viewer.addProject(newsurf);
		displayProjectPanel(newsurf);

		if (newsurf.getGeometry() != null)
			m_viewer.getDisplay().selectGeometry(newsurf.getGeometry());
		return newsurf;
	}

	public void changeProjectName(AbstractClient proj, String newName) {
		model.removeProject(proj);
		m_viewer.removeProject(proj);
		proj.rename(newName);
		model.addProject(proj);
		m_viewer.addProject(proj);
		m_viewer.selectProject(proj);
		for(PgGeometryIf geom:proj.getOutputGeoms()) {
			ssp.store.setName(geom, newName);
		}
	}

	public void saveScene(String filename) {

		try {
			FileWriter fw = new FileWriter(filename);
			fw.write("<" + "definitions" + ">\n");
			for (AbstractClient proj:model.projects) {
				if(proj==null) continue;
				Definition def = proj.getDefinition();
				proj.setDefinitionOptions(def);
				fw.write(def.toString());
				fw.write("\n");
			}
			fw.write("</definitions>\n");

			fw.write("<dependancies>\n");
			for (AbstractClient proj:model.projects) {
				if(proj==null) continue;
				ProjectComponents pc = proj.getProjectComponents();
				if (!pc.isEmpty()) {
					fw.write(pc.toString());
				}
			}
			fw.write("</dependancies>\n");
			fw.close();
		} catch (IOException e) {
			ssp.showStatus("Failed to write to " + filename);
		}
		System.out.println("Scene saved to " + filename);

	}

	public void showHide(PgGeometryIf geom) {
		boolean flag = geom.isVisible();
		System.out.println("show hide "+geom.getName()+" flag "+flag);
		geom.setVisible(!flag);
		ssp.store.geomApperenceChanged(geom);
	}

	public void deleteGeom(ListItem item, boolean rmDep) {
		AbstractClient proj = item.project;
		if(proj instanceof AbstractOperatorClient) {
			((AbstractOperatorClient)proj).removeOutpuGeometry(item.geom,rmDep);
		}
		else {
			proj.removeGeometry(item.geom);
		}
		model.rebuild();

	}


	public void saveProject() {
		AbstractClient proj = currentProject;
		if(proj!=null)
			proj.saveDef();
		else {
			ssp.showStatus("No current project selected");
		}
	}

	public void appendProject() {
		AbstractClient proj = currentProject;
		if(proj!=null)
			proj.saveAppendDef(true);
		else {
			ssp.showStatus("No current project selected");
		}
	}


	public void loadModel(String dir, String file) {

		try {
			if(file.endsWith(".wrl")) {
				PgWrlLoader loader = new PgWrlLoader();
				PgJvxSrc[] jvxs;
				jvxs = loader.read(new BufferedReader(new FileReader(dir+file)));
				for(PgJvxSrc jvx:jvxs) {
					ssp.store.globals.addOrphan(jvx,file);
				}
			}
			else if(file.endsWith(".obj")) {
				PgObjLoader loader = new PgObjLoader();
				PgJvxSrc[] jvxs;
				jvxs = loader.read(new BufferedReader(new FileReader(dir+file)));
				for(PgJvxSrc jvx:jvxs) {
					ssp.store.globals.addOrphan(jvx,file);
				}
			}
		} catch (FileNotFoundException e) {
			ssp.showStatus(e.toString());
		}


	}

}
