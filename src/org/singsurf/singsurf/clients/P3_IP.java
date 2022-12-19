package org.singsurf.singsurf.clients;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.Panel;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JTextField;

import org.singsurf.singsurf.WrapLayout;

import jv.object.PsPanel;
import jv.object.PsUpdateIf;
import jv.vecmath.PdMatrix;

public class P3_IP extends SingSurf_IP {
	private static final long serialVersionUID = 1L;

	private P3 p3;

	private JTextField rxx = new JTextField();
	private JTextField rxy = new JTextField();
	private JTextField rxz = new JTextField();
	private JTextField rxw = new JTextField();

	private JTextField ryx = new JTextField();
	private JTextField ryy = new JTextField();
	private JTextField ryz = new JTextField();
	private JTextField ryw = new JTextField();

	private JTextField rzx = new JTextField();
	private JTextField rzy = new JTextField();
	private JTextField rzz = new JTextField();
	private JTextField rzw = new JTextField();

	private JTextField rwx = new JTextField();
	private JTextField rwy = new JTextField();
	private JTextField rwz = new JTextField();
	private JTextField rww = new JTextField();

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
		
		PsPanel p4 = new PsPanel();
		p4.setLayout(new BoxLayout(p4,BoxLayout.Y_AXIS));

		p4.add(new Label("Rotation Matrix:"));

		PsPanel p4b = new PsPanel(new GridLayout(4, 4));
		p4b.add(rxx);
		p4b.add(rxy);
		p4b.add(rxz);
		p4b.add(rxw);

		p4b.add(ryx);
		p4b.add(ryy);
		p4b.add(ryz);
		p4b.add(ryw);

		p4b.add(rzx);
		p4b.add(rzy);
		p4b.add(rzz);
		p4b.add(rzw);

		p4b.add(rwx);
		p4b.add(rwy);
		p4b.add(rwz);
		p4b.add(rww);

		p4.add(p4b);
		p4.add(p3.BloadMatrix);
		p4.add(p3.BsaveMatrix);
		return p4;
	}

	public void updateRotMatDisp(PdMatrix mat) {
		rxx.setText(String.format("%6.3f",mat.getEntry(0, 0)));
		rxy.setText(String.format("%6.3f",mat.getEntry(0, 1)));
		rxz.setText(String.format("%6.3f",mat.getEntry(0, 2)));
		rxw.setText(String.format("%6.3f",mat.getEntry(0, 3)));
		
		ryx.setText(String.format("%6.3f",mat.getEntry(1, 0)));
		ryy.setText(String.format("%6.3f",mat.getEntry(1, 1)));
		ryz.setText(String.format("%6.3f",mat.getEntry(1, 2)));
		ryw.setText(String.format("%6.3f",mat.getEntry(1, 3)));
		
		rzx.setText(String.format("%6.3f",mat.getEntry(2, 0)));
		rzy.setText(String.format("%6.3f",mat.getEntry(2, 1)));
		rzz.setText(String.format("%6.3f",mat.getEntry(2, 2)));
		rzw.setText(String.format("%6.3f",mat.getEntry(2, 3)));
		
		rwx.setText(String.format("%6.3f",mat.getEntry(3, 0)));
		rwy.setText(String.format("%6.3f",mat.getEntry(3, 1)));
		rwz.setText(String.format("%6.3f",mat.getEntry(3, 2)));
		rww.setText(String.format("%6.3f",mat.getEntry(3, 3)));
		
	}


}
