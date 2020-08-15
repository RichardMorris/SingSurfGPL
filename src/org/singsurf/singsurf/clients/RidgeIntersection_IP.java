/*
Created 6 Oct 2006 - Richard Morris
*/
package org.singsurf.singsurf.clients;

import jv.object.PsPanel;
import jv.object.PsUpdateIf;

public class RidgeIntersection_IP extends SingSurf_IP {
	private static final long serialVersionUID = -6967264854566009554L;
	private RidgeIntersection ridgeParent;

	public RidgeIntersection_IP() {
		super(false);
		if (getClass() == RidgeIntersection_IP.class)
			init();
	}

	
	@Override
	public void init() {
		super.init();
		addTitle("Chained Intersection");
	}

	@Override
	protected PsPanel getSouthPanel() {
		PsPanel pan = new PsPanel();
		pan.add(project.m_go);
		pan.addLabelComponent("Surface",ridgeParent.ch_ingredient1);
		pan.addLabelComponent("Field",ridgeParent.ch_ingredient2);
		pan.addLabelComponent("Input Geometry",ridgeParent.ch_inputSurf);
		return pan;
	}

	@Override
	protected PsPanel getOptionPanel() {
		PsPanel p4 = new PsPanel();
		p4.addLabelComponent("curves colours", ridgeParent.chCurveColours);
		p4.addLabelComponent("Itterations", ridgeParent.chItts);
		return p4;
	}


	@Override
	public void setParent(PsUpdateIf par) {
		ridgeParent = (RidgeIntersection) par;
		super.setParent(par);
	}

}
