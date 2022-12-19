package org.singsurf.singsurf;

import java.awt.Button;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Panel;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.singsurf.singsurf.ProjectChooserModel.ListItem;
import org.singsurf.singsurf.clients.AbstractOperatorProject;
import org.singsurf.singsurf.clients.AbstractProject;
import org.singsurf.singsurf.definitions.DefType;
import org.singsurf.singsurf.definitions.Definition;
import org.singsurf.singsurf.definitions.DefinitionReader;
import org.singsurf.singsurf.definitions.ProjectComponents;
import org.singsurf.singsurf.definitions.VisibleGeometries;
import org.singsurf.singsurf.geometries.GeomListener;

import jv.loader.PgJvxLoader;
import jv.loader.PgObjLoader;
import jv.project.PgGeometryIf;
import jv.project.PgJvxSrc;
import jv.project.PjProject;
import jv.project.PvDisplayIf;
import jv.viewer.PvViewer;
import jvx.loader.PgWrlLoader;

public class ProjectChooserController implements ActionListener, ItemListener {

	PvViewer m_viewer;
	SingSurf3D ssp;
	ProjectChooserModel model;
	ProjectFactory factory;
	AbstractProject currentProject;

	public ProjectChooserController(SingSurf3D ssp, PvViewer m_viewer, ProjectChooserModel model) {
		super();
		this.ssp = ssp;
		this.m_viewer = m_viewer;
		this.model = model;
		this.factory = new ProjectFactory(model, ssp.store);
	}


	public void modelItemSelected(ListItem listItem) {
		AbstractProject proj = listItem.project;
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
		if(proj instanceof AbstractOperatorProject) {
			((AbstractOperatorProject) proj).selectOutputGeometry(listItem.geom);
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

	public void cloneProject() {
		AbstractProject proj = currentProject;
		if(proj!=null)
			clone(proj);
		else {
			ssp.showStatus("No current project selected");
		}
	
	}
	
	public void clone(AbstractProject project) {
		String newName = model.getCloneName(project.getName());
		Definition def = project.getDefinition();
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
			AbstractProject proj = item.project;
			ListItem prevItem = model.getNearbyItem(item);
			delProjecty(proj, rmGeom, rmDep);
			PjProject curproj = m_viewer.getCurrentProject();
			if(curproj == proj || curproj == null) {
				this.modelItemSelected(prevItem);
			}
		}
		else {
			System.out.println("deleteGeometry "+item.toString());
			AbstractProject proj = item.project;
			if(proj instanceof AbstractOperatorProject) {
				((AbstractOperatorProject)proj).removeOutpuGeometry(item.geom,rmDep);

			} else {
				proj.removeGeometry(item.geom);
			}
		}
	}


	/**
	 * @param proj
	 * @param item
	 * @param rmGeom
	 * @param rmDep
	 */
	public void delProjecty(AbstractProject proj, boolean rmGeom, boolean rmDep) {
		if (rmGeom)
			for (PgGeometryIf geom : proj.getOutputGeoms()) {
				ssp.store.removeGeometry(geom, rmDep);
			}
		if(proj == ssp.store.globals)
			return;
		m_viewer.removeProject(proj);
		model.removeProject(proj);
		if(proj instanceof GeomListener)
			ssp.store.removeListerner((GeomListener) proj);
		proj.dispose();
	}

	ExecutorService executor = Executors.newCachedThreadPool();

	public void loadScene(String filename) {
		System.out.println("loadSceneFromFile: "+filename+" thread: "+ Thread.currentThread().toString());

		try {
			FileReader fr = new FileReader(filename);
			DefinitionReader ldr = new DefinitionReader(new BufferedReader(fr));
			ldr.read();
			fr.close();
			
			final VisibleGeometries visGeom = ldr.getVisGeom();
			if(visGeom!=null) {
				VisabilityController visCont = new VisabilityController(visGeom,ssp.store);
                
                // Create new displays as needed
                PvDisplayIf[] existingDisplays = ssp.m_viewer.getDisplays();
                
                for(String disp:visGeom.getDisplays()) {
                    boolean found=false;
                    for(PvDisplayIf d:existingDisplays) {
                        if(disp.equals(d.getName())) {
                            found = true;
                        }
                    }
                    if(!found) {
                        PvDisplayIf newdisp = ssp.m_viewer.newDisplay(disp,true,false);
                        Frame frame = newdisp.getFrame();
                        frame.setVisible(true);
                    }
                }
				ssp.store.addGeomListner(visCont);
			}
			for (Definition def : ldr.getDefs()) {
				if(def.getType().equals(DefType.globals)) {
					ssp.store.globals.loadDefinition(def);
				} else {
					createProject(def);
				}
			}

			for (ProjectComponents pc : ldr.getProjComp()) {
				if (!pc.isEmpty()) {
					AbstractProject client = model.getProject(pc.getName());
					if (client != null) {
						client.loadProjectComponents(pc);
						ProjectComponentEvaluator evaluator = new ProjectComponentEvaluator(pc,client,ssp.store);
						executor.execute(evaluator);
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
		AbstractProject newsurf = factory.createProject(def);
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
	public AbstractProject loadProject(DefType type) {
		AbstractProject newsurf = factory.createProject(type);
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

	public void changeProjectName(AbstractProject proj, String newName) {
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
			for (AbstractProject proj:model.projects) {
				if(proj==null) continue;
//				proj.visableGeometries(visable);
				Definition def = proj.getDefinition();
				proj.setDefinitionOptions(def);
				fw.write(def.toString());
				fw.write("\n");
			}
			fw.write("</definitions>\n");

			fw.write("<dependancies>\n");
			for (AbstractProject proj:model.projects) {
				if(proj==null) continue;
				ProjectComponents pc = proj.getProjectComponents();
				if (!pc.isEmpty()) {
					fw.write(pc.toString());
				}
			}
			fw.write("</dependancies>\n");
			
			VisibleGeometries vg = new VisibleGeometries(m_viewer);
			fw.write(vg.toString());
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
		AbstractProject proj = item.project;
		if(proj instanceof AbstractOperatorProject) {
			((AbstractOperatorProject)proj).removeOutpuGeometry(item.geom,rmDep);
		}
		else {
			proj.removeGeometry(item.geom);
		}
		model.rebuild();

	}


	public void saveProject() {
		AbstractProject proj = currentProject;
		if(proj!=null)
			proj.saveDef();
		else {
			ssp.showStatus("No current project selected");
		}
	}

	public void appendProject() {
		AbstractProject proj = currentProject;
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
			else if(file.endsWith(".jvx")) {
				PgJvxLoader loader = new PgJvxLoader();
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

	@Override
	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();
		int i = model.view.getSelectedIndex();
		if(i < 0) return;
		
		ListItem item = model.items.get(i);
		
		if(command.equals("Clone")) {
			clone(item.project);
		} else if(command.equals("Save")) {
			saveProj(item);
		} else if(command.equals("Append")) {
			appendProj(item);
		} else if(command.equals("DelKeep")) {
			deleteProj(item,false,false);
		} else if(command.equals("DelRm")) {
			deleteProj(item,true,false);
		} else if(command.equals("DelRmDeps")) {
			deleteProj(item,true,true);
		} else if(command.equals("DelGeom")) {
			deleteGeom(item,false);
		} else if(command.equals("Rename")) {
			renameProject(item);
		} else if(command.equals("Show/Hide")) {
			showHide(item.geom);
		} else {
			System.err.println("Unknown action command "+command);
		}
	}

	private void renameProject(ListItem item) {
		System.err.println("changeProjectName");

		 AbstractProject proj = item.project;
		 
		ChangeNameDialog d = new ChangeNameDialog(ssp.m_frame, proj.getName());
		d.setVisible(true);
		if (d.state) {
			changeProjectName(proj, d.tf.getText());
		}
	}
	
    
	class ChangeNameDialog extends Dialog implements ActionListener {
		private static final long serialVersionUID = 1L;
		TextField tf;
		boolean state = false;

		public ChangeNameDialog(Frame parent, String name) {
			super(parent, "Change name for project " + name, true);
			Panel p = new Panel();
			add(p);
			tf = new TextField(name, 20);
			p.add(tf);
			Button but1 = new Button("OK");
			Button but2 = new Button("Cancel");
			p.add(but1);
			p.add(but2);
			but1.addActionListener(this);
			but2.addActionListener(this);
			tf.addActionListener(this);
			this.addWindowListener(new WindowAdapter() {
				@Override
				public void windowClosing(WindowEvent e) {
					// ChangeNameDialog.this.setVisible(false);
					dispose();
				}
			});
			pack();
		}


		@Override
		public void actionPerformed(ActionEvent arg0) {
			// System.out.println("ActCom "+arg0.getActionCommand());
			if (arg0.getActionCommand().equals("Cancel"))
				state = false;
			else
				state = true;
			// this.setVisible(false);
			dispose();
		}
	}
	
	@Override
	public void itemStateChanged(ItemEvent evt) {
		int i = model.view.getSelectedIndex();
		if(i < 0) return;
		ListItem item = model.items.get(i);
		modelItemSelected(item);
	}


}
