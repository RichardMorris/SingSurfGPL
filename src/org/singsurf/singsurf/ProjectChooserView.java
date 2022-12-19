/*
Created 23 May 2011 - Richard Morris
*/
package org.singsurf.singsurf;

import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import org.singsurf.singsurf.ProjectChooserModel.ListItem;

public class ProjectChooserView extends java.awt.List implements MouseListener {
	private static final long serialVersionUID = 350L;
	ProjectChooserModel model;
    //ProjectChooserController controller;
    
	PopupMenu projRightClickPopup;
	PopupMenu geomRightClickPopup;
    
    public ProjectChooserView(ProjectChooserController controller,ProjectChooserModel model) {
    	//this.controller = controller;
    	this.model = model;
    	model.setView(this);
    	
    	this.addItemListener(controller);
    	this.addMouseListener(this);
    	
		projRightClickPopup = new PopupMenu();
		geomRightClickPopup = new PopupMenu();
		this.add(projRightClickPopup);
		this.add(geomRightClickPopup);
		projRightClickPopup.add("Project Name");
		projRightClickPopup.addSeparator();
		geomRightClickPopup.add("Geometry Name");
		geomRightClickPopup.addSeparator();
		
		MenuItem loadItem = new MenuItem("Clone");
		loadItem.addActionListener(controller);
		projRightClickPopup.add(loadItem);

		loadItem = new MenuItem("Show/Hide");
		loadItem.addActionListener(controller);
		geomRightClickPopup.add(loadItem);

//		loadItem = new MenuItem("Save");
//		loadItem.addActionListener(this);
//		projRightClickPopup.add(loadItem);
//		geomRightClickPopup.add(loadItem);
		
		loadItem = new MenuItem("Delete (keep geometries)");
		loadItem.setActionCommand("DelKeep");
		loadItem.addActionListener(controller);
		projRightClickPopup.add(loadItem);

		loadItem = new MenuItem("Delete");
		loadItem.setActionCommand("DelGeom");
		loadItem.addActionListener(controller);
		geomRightClickPopup.add(loadItem);

		loadItem = new MenuItem("Delete (remove geometries)");
		loadItem.setActionCommand("DelRm");
		loadItem.addActionListener(controller);
		projRightClickPopup.add(loadItem);

		loadItem = new MenuItem("Delete (remove geometries and dependents)");
		loadItem.setActionCommand("DelRmDeps");
		loadItem.addActionListener(controller);
		projRightClickPopup.add(loadItem);

		loadItem = new MenuItem("Rename");
		loadItem.addActionListener(controller);
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



    
}
