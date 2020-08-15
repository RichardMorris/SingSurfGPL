package org.singsurf.singsurf.clients;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.Label;

import jv.object.PsPanel;
import jv.object.PsUpdateIf;
import jv.objectGui.PsTabPanel;
import jv.project.PjProject_IP;

public class Globals_IP extends PjProject_IP {
	private static final long serialVersionUID = 1L;
	Globals parent;

	/**
	 * 
	 */
	public Globals_IP() {
		super();
		// System.out.println("IP constructor");
		if (getClass() == Globals_IP.class)
			init();
		// System.out.println("IP constructor done");
	}

	// Initialization.

	public void init() {
		// System.out.println("IP init");

		super.init();
		addTitle("Global Variables");
		// System.out.println("IP init done");
	}

	@Override
	public void setParent(PsUpdateIf par) {
		// System.out.println("IP setParent");

		super.setParent(par);

		parent = (Globals) par;
		parent.m_IP = this;
		this.setFont(parent.basicFont);
		this.getTitle().setFont(parent.basicFont.deriveFont(Font.BOLD));
		
		setLayout(new BorderLayout());
		
		PsTabPanel tabPanel = new PsTabPanel();
		add(tabPanel); // add tabbed panel like any other panel

		tabPanel.addPanel("Main",getMainPanel());
		tabPanel.addPanel("Geometries",getGeomPanel());
//		this.setLayout(new BorderLayout());
		tabPanel.addPanel("Global Variables",parent.newParams);
	}

	protected PsPanel getMainPanel() {
		PsPanel p1 = new PsPanel();
		p1.add(new Label("SingSurf"));
		return p1;
	}
	
	protected PsPanel getGeomPanel() {
		PsPanel p1 = new PsPanel();
		Component comp = parent.activeGeomNames;
		p1.add(comp);
		p1.add(new Label("With selected:"));
		p1.add(parent.cbShowFace);
		p1.add(parent.cbShowEdge);
		p1.add(parent.cbShowVert);
		p1.add(parent.cbShowCurves);
		p1.add(parent.cbShowPoints);
		p1.add(parent.cbShowBoundary);

		p1.addLabelComponent("surf colours: ",parent.chSurfColours);
		p1.addLabelComponent("curves colours: ",parent.chCurveColours);

		
		p1.add( parent.removeGeomButton);
		
		return p1;
	}
}
