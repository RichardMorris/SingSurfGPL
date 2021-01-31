package org.singsurf.singsurf.clients;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.Label;

import org.singsurf.singsurf.LParamList;

import jv.object.PsPanel;
import jv.object.PsUpdateIf;
import jv.objectGui.PsTabPanel;
import jv.project.PjProject_IP;

public class Globals_IP extends PjProject_IP {
	private static final long serialVersionUID = 1L;
	Globals globals;

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

		globals = (Globals) par;
		globals.m_IP = this;
		this.setFont(globals.basicFont);
		this.getTitle().setFont(globals.basicFont.deriveFont(Font.BOLD));
		
		setLayout(new BorderLayout());
		
		PsTabPanel tabPanel = new PsTabPanel();
		add(tabPanel); // add tabbed panel like any other panel

		final PsPanel mainPanel = getMainPanel();
		mainPanel.setFont(globals.basicFont);
		tabPanel.addPanel("Main",mainPanel);
		final PsPanel geomPanel = getGeomPanel();
		geomPanel.setFont(globals.basicFont);
		tabPanel.addPanel("Geometries",geomPanel);
//		this.setLayout(new BorderLayout());
		final LParamList paramPanel = globals.newParams;
		paramPanel.setFont(globals.basicFont);
		tabPanel.addPanel("Global Variables",paramPanel);
	}

	protected PsPanel getMainPanel() {
		PsPanel p1 = new PsPanel();
		p1.add(new Label("SingSurf"));
		return p1;
	}
	
	protected PsPanel getGeomPanel() {
		PsPanel p1 = new PsPanel();
		Component comp = globals.activeGeomNames;
		p1.add(comp);
		p1.add(new Label("With selected:"));
		p1.add(globals.cbShowFace);
		p1.add(globals.cbShowEdge);
		p1.add(globals.cbShowVert);
		p1.add(globals.cbShowCurves);
		p1.add(globals.cbShowPoints);
		p1.add(globals.cbShowBoundary);

		p1.addLabelComponent("surf colours: ",globals.chSurfColours);
		p1.addLabelComponent("curves colours: ",globals.chCurveColours);

		
		p1.add( globals.removeGeomButton);
		
		return p1;
	}
}
