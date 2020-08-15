package org.singsurf.singsurf.clients;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.Panel;

import javax.swing.Box;
import javax.swing.BoxLayout;

import org.singsurf.singsurf.WrapLayout;

import jv.object.PsPanel;
import jv.object.PsUpdateIf;

public class ASurf_IP extends SingSurf_IP {
	private static final long serialVersionUID = 1L;

	private ASurf asurf;
	public ASurf_IP() {
		super(false);
		if (getClass() == ASurf_IP.class)
			init();
	}

	@Override
	public void init() {
		super.init();
		addTitle("Algebraic Surface");
	}

	@Override
	public void setParent(PsUpdateIf par) {
		asurf = (ASurf) par;
		super.setParent(par);
	}
	
	@Override
	protected PsPanel getSouthPanel() {
		PsPanel south = super.getSouthPanel();
		south.add(((ASurf) project).cb_autoUpdate);
		return south;
	}

	@Override
	protected PsPanel getOptionPanel() {
		PsPanel p4 = new PsPanel();
		p4.setLayout(new BoxLayout(p4,BoxLayout.Y_AXIS));

		Panel p4a = new PsPanel(new WrapLayout(FlowLayout.LEFT, 1, 0));
		p4a.add(new Label("Resolution:"));
		p4a.add(asurf.cb_c_4);
		p4a.add(asurf.cb_c_8);
		p4a.add(asurf.cb_c_16);
		p4a.add(asurf.cb_c_32);
		p4a.add(asurf.cb_c_64);
		p4a.add(asurf.cb_c_128);
		p4a.add(asurf.cb_c_128);
		p4a.add(asurf.cb_c_256);

		p4.add(p4a);

		PsPanel p4b = new PsPanel(new GridLayout(0, 2));
		p4b.add(asurf.singPowerFrac.getLabel());
		p4b.add(asurf.singPowerFrac.getControlPanel());
		p4b.add(asurf.facePowerFrac.getLabel());
		p4b.add(asurf.facePowerFrac.getControlPanel());
		p4b.add(asurf.edgePowerFrac.getLabel());
		p4b.add(asurf.edgePowerFrac.getControlPanel());

		p4b.add(asurf.cb_adaptiveMesh);
		p4b.add(new Label());
		p4b.add(asurf.cb_refineCurvature);
		p4b.add(new Label());
		p4b.add(asurf.cb_triangulate);
		p4b.add(new Label());

		p4b.add(new Label());
		p4b.add(new Label());
		p4b.add(new Label("Surface colour"));
		p4b.add(asurf.chSurfColours);
		p4b.add(new Label("High value"));
		p4b.add(asurf.colourMax);
		p4b.add(new Label("Low value"));
		p4b.add(asurf.colourMin);
		p4b.add(new Label("Edge colour"));
		p4b.add(asurf.chCurveColours);
		p4b.add(new Label());
		p4b.add(new Label());

		// p5.add(new Label("line colours: "));
		//		p5.add(project.chColours);

		p4.add(p4b);
				
		
		PsPanel p6 = new PsPanel(new WrapLayout(FlowLayout.LEFT, 3, 1));		
		// p4.add(parent.cbCreateNew);
		// p4.add(parent.cb_keepMat);
		p6.add(project.cbShowFace);
		p6.add(project.cbShowEdge);
		p6.add(project.cbShowVert);
		p6.add(project.cbShowCurves);
		p6.add(project.cbShowPoints);
		p6.add(project.cbShowBoundary);
		p6.add(asurf.cb_skeleton);
		p6.add(asurf.cb_dgen);

		p4.add(p6);
		
		p4.add(new  Box.Filler(
				new Dimension((short) 0,(short) 0), 
				new Dimension((short) 0,(short) 500),
				new Dimension(Short.MAX_VALUE, Short.MAX_VALUE) ));
		return p4;
	}


}
