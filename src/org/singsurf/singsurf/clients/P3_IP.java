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

public class P3_IP extends SingSurf_IP {
	private static final long serialVersionUID = 1L;

	private P3 p3;
	public P3_IP() {
		super(false);
		if (getClass() == P3_IP.class)
			init();
	}

	@Override
	public void init() {
		super.init();
		addTitle("Projective Varities");
	}

	@Override
	public void setParent(PsUpdateIf par) {
		p3 = (P3) par;
		super.setParent(par);
	}
	
	
	@Override
	protected PsPanel getSouthPanel() {
		PsPanel p4b = new PsPanel(new GridLayout(0, 2));
		p4b.add(project.m_go);
		p4b.add(p3.cb_autoUpdate);
		p4b.add(p3.Breset);
		p4b.add(p3.cb_stereographic);
		p4b.add(p3.Bxwp);
		p4b.add(p3.Bxwm);
		p4b.add(p3.Bywp);
		p4b.add(p3.Bywm);
		p4b.add(p3.Bzwp);
		p4b.add(p3.Bzwm);
		return p4b;
	}

	@Override
	protected PsPanel getOptionPanel() {
		PsPanel p4 = new PsPanel();
		p4.setLayout(new BoxLayout(p4,BoxLayout.Y_AXIS));

		p4.add("Clipping",p3.m_Clipping);
		
		Panel p4a = new PsPanel(new WrapLayout(FlowLayout.LEFT, 1, 0));
		p4a.add(new Label("Resolution:"));
		p4a.add(p3.cb_c_4);
		p4a.add(p3.cb_c_8);
		p4a.add(p3.cb_c_16);
		p4a.add(p3.cb_c_32);
		p4a.add(p3.cb_c_64);
		p4a.add(p3.cb_c_128);
		p4a.add(p3.cb_c_128);
		p4a.add(p3.cb_c_256);

		p4.add(p4a);

		PsPanel p4b = new PsPanel(new GridLayout(0, 2));
		p4b.add(p3.singPowerFrac.getLabel());
		p4b.add(p3.singPowerFrac.getControlPanel());
		p4b.add(p3.facePowerFrac.getLabel());
		p4b.add(p3.facePowerFrac.getControlPanel());
		p4b.add(p3.edgePowerFrac.getLabel());
		p4b.add(p3.edgePowerFrac.getControlPanel());

		p4b.add(p3.cb_adaptiveMesh);
		p4b.add(new Label());
		p4b.add(p3.cb_refineCurvature);
		p4b.add(new Label());
		p4b.add(p3.cb_triangulate);
		p4b.add(new Label());

		p4b.add(new Label());
		p4b.add(new Label());
		p4b.add(new Label("Surface colour"));
		p4b.add(p3.chSurfColours);
		p4b.add(new Label("High value"));
		p4b.add(p3.colourMax);
		p4b.add(new Label("Low value"));
		p4b.add(p3.colourMin);
		p4b.add(new Label("Edge colour"));
		p4b.add(p3.chCurveColours);
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
		p6.add(p3.cb_skeleton);
		p6.add(p3.cb_dgen);

		p4.add(p6);
		
		p4.add(new  Box.Filler(
				new Dimension((short) 0,(short) 0), 
				new Dimension((short) 0,(short) 500),
				new Dimension(Short.MAX_VALUE, Short.MAX_VALUE) ));
		return p4;
	}

	@Override
	protected PsPanel getDomainPanel() {
		return null;
	}


}
