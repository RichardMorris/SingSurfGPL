/*
Created 23 May 2011 - Richard Morris
*/
package org.singsurf.singsurf;

import java.awt.Button;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.MenuItem;
import java.awt.Panel;
import java.awt.PopupMenu;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import org.singsurf.singsurf.ProjectChooserModel.ListItem;
import org.singsurf.singsurf.clients.AbstractClient;

public class ProjectChooserView extends java.awt.List implements ItemListener, MouseListener, ActionListener {
	private static final long serialVersionUID = 350L;
	ProjectChooserModel model;
    ProjectChooserController controller;
    
	PopupMenu projRightClickPopup;
	PopupMenu geomRightClickPopup;
	private Frame m_frame;

    
    public ProjectChooserView( ProjectChooserController controller,ProjectChooserModel model) {
    	this.controller = controller;
    	this.model = model;
    	model.setView(this);
    	
    	this.addItemListener(this);
    	this.addMouseListener(this);
    	
		projRightClickPopup = new PopupMenu();
		geomRightClickPopup = new PopupMenu();
		this.add(projRightClickPopup);
		this.add(geomRightClickPopup);
		projRightClickPopup.add("Project Name");
		projRightClickPopup.addSeparator();
		geomRightClickPopup.add("Geometry Name");
		geomRightClickPopup.addSeparator();
		
//		rightClickPopup.p
		// CheckboxMenuItem cbit = new CheckboxMenuItem("Show vertices");
		// cbit.addItemListener(new ItemListener(){
		// @Override
		// public void itemStateChanged(ItemEvent arg0) {
		//
		//
		// }});
		// rightClickPopup.add(cbit);
		// rightClickPopup.addSeparator();

		MenuItem loadItem = new MenuItem("Clone");
		loadItem.addActionListener(this);
		projRightClickPopup.add(loadItem);

		loadItem = new MenuItem("Show/Hide");
		loadItem.addActionListener(this);
		geomRightClickPopup.add(loadItem);

//		loadItem = new MenuItem("Save");
//		loadItem.addActionListener(this);
//		projRightClickPopup.add(loadItem);
//		geomRightClickPopup.add(loadItem);
		
		loadItem = new MenuItem("Delete (keep geometries)");
		loadItem.setActionCommand("DelKeep");
		loadItem.addActionListener(this);
		projRightClickPopup.add(loadItem);

		loadItem = new MenuItem("Delete");
		loadItem.setActionCommand("DelGeom");
		loadItem.addActionListener(this);
		geomRightClickPopup.add(loadItem);

		loadItem = new MenuItem("Delete (remove geometries)");
		loadItem.setActionCommand("DelRm");
		loadItem.addActionListener(this);
		projRightClickPopup.add(loadItem);

		loadItem = new MenuItem("Delete (remove geometries and dependents)");
		loadItem.setActionCommand("DelRmDeps");
		loadItem.addActionListener(this);
		projRightClickPopup.add(loadItem);

		loadItem = new MenuItem("Rename");
		loadItem.addActionListener(this);
		projRightClickPopup.add(loadItem);		
    }

    public void rebuild() {
    	System.out.println("view rebuild");
    	this.removeAll();
    	for(ProjectChooserModel.ListItem item:model.items) {
    		if(item.geom == null) {
    			this.add(item.project.getName());
//    			System.out.println(item.project.getName());
    		}
    		else {
            	this.add("- "+item.geom.getName());
//            	System.out.println("- "+item.geom.getName());
            }
    	}
    }

    


	@Override
	public String getItem(int index) {
		try {
		return super.getItem(index);
		} catch(Exception e) {
			System.out.println(e);
			return "";
		}
	}

	@Override
	public void itemStateChanged(ItemEvent evt) {
//		System.out.println("itemStateChanged "+evt);
		int i = this.getSelectedIndex();
		controller.modelItemSelected(model.items.get(i));
	}

	@Override
	public void mousePressed(MouseEvent event) {
		int modifiers = event.getModifiersEx();
		if ((modifiers & InputEvent.BUTTON3_DOWN_MASK) == InputEvent.BUTTON3_DOWN_MASK) {
//			Component comp = this.getComponentAt(event.getPoint());
		 
			int i = this.getSelectedIndex();
			if(i <= 0) return;
			
			ListItem item = model.items.get(i);

			if(item.geom == null) {
//			System.out.println("Right pressed "+event);
			
			projRightClickPopup.remove(0);
			projRightClickPopup.insert(this.getSelectedItem(), 0);
			projRightClickPopup.show(this, event.getX(), event.getY());
//			projRightClickPopup.show(event.getComponent(), event.getX(), event.getY());
			}
			else {
				geomRightClickPopup.remove(0);
				geomRightClickPopup.insert(this.getSelectedItem(), 0);
				geomRightClickPopup.show(this, event.getX(), event.getY());
			}
		}
		// else
		// System.out.println("Pressed "+modifiers);
	}

	@Override
	public void mouseClicked(MouseEvent arg0) {
//		System.out.println("mouseClicked "+arg0);
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();
		int i = this.getSelectedIndex();
		if(i < 0) return;
		
		ListItem item = model.items.get(i);
		
		if(command.equals("Clone")) {
			controller.clone(item);
		} else if(command.equals("Save")) {
			controller.saveProj(item);
		} else if(command.equals("Append")) {
			controller.appendProj(item);
		} else if(command.equals("DelKeep")) {
			controller.deleteProj(item,false,false);
		} else if(command.equals("DelRm")) {
			controller.deleteProj(item,true,false);
		} else if(command.equals("DelRmDeps")) {
			controller.deleteProj(item,true,true);
		} else if(command.equals("DelGeom")) {
			controller.deleteGeom(item,false);
		} else if(command.equals("Rename")) {
			renameProject(item);
		} else if(command.equals("Show/Hide")) {
			controller.showHide(item.geom);
		} else {
			System.err.println("Unknown action command "+command);
		}
	}

	private void renameProject(ListItem item) {
			System.err.println("changeProjectName");

			 AbstractClient proj = item.project;
			 
			ChangeNameDialog d = new ChangeNameDialog(this.m_frame, proj.getName());
			d.setVisible(true);
			if (d.state) {
				controller.changeProjectName(proj, d.tf.getText());
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

    
}
