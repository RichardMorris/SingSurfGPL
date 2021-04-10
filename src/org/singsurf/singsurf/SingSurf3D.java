/* @author rich
 * Created on 20-Jun-2003
 *
 * This code is covered by a Creative Commons
 * Attribution, Non Commercial, Share Alike license
 * <a href="http://creativecommons.org/licenses/by-nc-sa/1.0">License</a>
 */
package org.singsurf.singsurf;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.CheckboxMenuItem;
import java.awt.Choice;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuItem;
import java.awt.Panel;
import java.awt.PopupMenu;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

import javax.swing.JSplitPane;

import org.singsurf.singsurf.ProjectFactory.ProjectType;
import org.singsurf.singsurf.clients.Globals;
import org.singsurf.singsurf.definitions.DefType;
import org.singsurf.singsurf.definitions.Definition;
import org.singsurf.singsurf.definitions.DefinitionReader;
import org.singsurf.singsurf.definitions.DefinitionReader.TreeNode;

import jv.project.PjProject;
import jv.project.PvCameraIf;
import jv.vecmath.PdVector;

/**
 * Advanced version which allows multiple projects at same time.
 * 
 * @author Rich Morris Created on 20-Jun-2003
 */
public class SingSurf3D extends PaSingSurf implements ActionListener {
	private static final long serialVersionUID = -7242387172848278637L;

	CheckboxMenuItem twoDview;
	Choice appletNewProj = new Choice();
	Choice appletSelProj = new Choice();

	private ProjectChooserModel model;

	private ProjectChooserController controller;

	public SingSurf3D() {
		super();
	}

	/** Project with help text */
	PjProject ssHelp;

	@Override
	public void init() {
		super.init();
		if (PRINT_DEBUG)
			System.out.println("SSP init");
		// autoFit = m_viewer.getParameter("AutoFit").equals("true");

		m_viewer.getDisplay().setAutoCenter(false);

//		/** The location the sing surf panels is displayed in. */
//		String SSPanelPos = m_viewer.getParameter("SSPanel");

		setLayout(new BorderLayout());

		setUpMVC();
		splitPaneLeft = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,new Panel(),splitPaneRight);
//		this.removeAll();
		this.add(splitPaneLeft,BorderLayout.CENTER);
		buildMenus();
		Globals globals = (Globals) controller.loadProject(DefType.globals);
		store.globals = globals;

//		ssHelp = new SSHelp();
//		m_viewer.addProject(ssHelp);
//		validate();
	}

	private void setUpMVC() {
		model = new ProjectChooserModel(store, this.m_viewer);
		controller = new ProjectChooserController(this, this.m_viewer, model);
		ProjectChooserView view = new ProjectChooserView(controller, model);
		view.setMinimumSize(new Dimension(100,800));
		
		Component disp = (Component) m_viewer.getDisplay();
		splitPaneRight = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,disp,view);
		splitPaneRight.setResizeWeight(1);
	}

	/** Start viewer, e.g. start animation if requested */
	@Override
	public void start() {
		if (PRINT_DEBUG)
			System.out.println("applet: start");
		// System.out.println("Cur Proj"+m_viewer.getCurrentProject());

		m_viewer.start();
		// System.out.println("Viewer Started");
	}

	void buildMenus() {
		MenuBar mb = new MenuBar();

		Menu fileMenu = new Menu("File");
		mb.add(fileMenu);

		Menu nmi = new Menu("New");
		for (ProjectType proj : this.controller.factory.projectTypes) {
			MenuItem it = new MenuItem(proj.longName);
			it.addActionListener(this);
			it.setActionCommand(proj.shortName + ':');
			nmi.add(it);
		}
		fileMenu.add(nmi);

		MenuItem loadItem = new MenuItem("Load");
		loadItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				loadProject();
			}
		});
		fileMenu.add(loadItem);

		MenuItem sv2 = new MenuItem("Save project");
		sv2.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				saveProject();
			}
		});
		fileMenu.add(sv2);

		MenuItem sv3 = new MenuItem("Append project");
		sv3.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				appendProject();
			}
		});
		fileMenu.add(sv3);

		MenuItem sv = new MenuItem("Save scene");
		sv.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				saveScene();
			}
		});
		fileMenu.add(sv);

		MenuItem del = new MenuItem("Delete all");
		del.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				controller.deleteAll();
			}
		});
		fileMenu.add(del);


		MenuItem quit = new MenuItem("Quit");
		quit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				System.exit(0);
			}
		});
		fileMenu.add(quit);

		Menu newProj = new Menu("Examples");
		mb.add(newProj);
		for (ProjectType proj : controller.factory.projectTypes) {
			if(proj.egPath.length()>0) {
				Menu m = buildExamplesSubMenu(proj);
				newProj.add(m);
			}
		}

		Menu options = new Menu("Options");
		mb.add(options);

		twoDview = new CheckboxMenuItem("2D view", false);
		twoDview.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent arg0) {
				if (twoDview.getState()) {
//		    PdVector interest = new PdVector(0, 0, 0);
					PdVector posn = new PdVector(0, 0, -1);
					PdVector up = new PdVector(0, 1, 0);

					m_viewer.getDisplay().getCamera().setViewDir(posn);
					m_viewer.getDisplay().getCamera().setUpVector(up);
					// m_viewer.getDisplay().getCamera().setFullPosition(interest, posn, up);
					m_viewer.getDisplay().getCamera().setProjectionMode(PvCameraIf.CAMERA_ORTHO_XY);
					m_viewer.getDisplay().setEnabled3DLook(false);
				} else {
					m_viewer.getDisplay().getCamera().setProjectionMode(PvCameraIf.CAMERA_PERSPECTIVE);
					m_viewer.getDisplay().setEnabled3DLook(true);
				}
			}
		});
		options.add(twoDview);

		if (m_frame != null)
			m_frame.setMenuBar(mb);
		else {
			final Button b = new Button("but");
			final PopupMenu pum = new PopupMenu();
			pum.add(loadItem);
			b.add(pum);
			this.add("North", b);
			b.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					pum.show(b, 0, 0);
				}
			});
		}
		Menu help = new Menu("Help");
		help.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("help event");
				m_viewer.selectProject(ssHelp);
			}
		});
		mb.setHelpMenu(help);
	}

	Menu buildExamplesSubMenu(ProjectType gen) {
		Menu m = new Menu(gen.longName);

		MenuItem it = new MenuItem(gen.longName);
		it.addActionListener(this);
		it.setActionCommand(gen.shortName + ':');
		m.add(it);
		m.addSeparator();
		try {
			DefinitionReader ldr = store.loadDefs(gen.egPath);
			addDefsFromGroupToMenu(ldr.getRoot(),m,gen);
			
		} catch (IOException e) {
			System.out.println(e);
		}
		return m;
	}

	private void addDefsFromGroupToMenu(TreeNode root, Menu m, ProjectType gen) {
		for(TreeNode child:root.getChildren()) {
			Menu sub = new Menu(child.getName());
			addDefsFromGroupToMenu(child,sub,gen);
			m.add(sub);
		}
		for(Definition def : root.getDefs()) {
			MenuItem it = new MenuItem(def.getName());
			it.addActionListener(this);
			it.setActionCommand(gen.shortName + ':' + def.getName());
			m.add(it);			
		}

	}

	/**
	 * Stand-alone application support. The main() method acts as the applet's entry
	 * point when it is run as a stand-alone application. It is ignored if the applet
	 * is run from within an HTML page.
	 */

	public static void main(String args[]) {
		PaSingSurf va = new SingSurf3D();
		commonMain(va, args);
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		String command = event.getActionCommand();
		int colon = command.lastIndexOf(':');
		
//		StringTokenizer st = new StringTokenizer(command, ":");
		String prefix = colon >=0 ? command.substring(0, colon) : command;
		String suffix = colon >=0 ? command.substring(colon+1) : null;
//		if (st.countTokens() > 0) {
//			st.
//			suffix = st.nextToken();
//		}
		for (ProjectType proj : this.controller.factory.projectTypes) {
			if (prefix.equals(proj.shortName)) {
				try {
					if (suffix == null || suffix.length()==0) {
						controller.loadProject(proj.type);
					} else {
						for (Definition def : proj.defs) {
							if (def.getName().equals(suffix))
								controller.loadProject(def);
						}
					}
				} catch (Exception e) {
					System.out.println(e.toString());
				}
			}
		}
	}

	void loadProject() {
		FileDialog fd = new FileDialog(m_frame, "Load definition", FileDialog.LOAD);
		fd.setVisible(true);
		if(fd.getDirectory() == null || fd.getFile() == null) {
			System.out.println("File dialog canceled");
			return;
		}
		String imagefilename = fd.getDirectory() + fd.getFile();
		System.out.println("Load from " + imagefilename);
		
		if(imagefilename.endsWith(".wrl")
				|| imagefilename.endsWith(".jvx")
				|| imagefilename.endsWith(".obj")) {
			controller.loadModel(fd.getDirectory(),fd.getFile());
			return;
		}
		controller.loadProjectFromFile(imagefilename);
	}

	void saveScene() {
		FileDialog fd = new FileDialog(m_frame, "Save scene", FileDialog.SAVE);
		fd.setVisible(true);
		if(fd.getDirectory() == null || fd.getFile() == null) {
			System.out.println("File dialog canceled");
			return;
		}
		String filename = fd.getDirectory() + fd.getFile();
		controller.saveScene(filename);
	}

	void saveProject() {
		controller.saveProject();
	}

	void appendProject() {
		controller.appendProject();
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

}
