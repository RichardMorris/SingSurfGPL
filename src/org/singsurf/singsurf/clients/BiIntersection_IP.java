/*
Created 6 Oct 2006 - Richard Morris
*/
package org.singsurf.singsurf.clients;

import jv.object.PsPanel;

public class BiIntersection_IP extends SingSurf_IP {
	private static final long serialVersionUID = -6967264854566009554L;

	public BiIntersection_IP() {
		super(false);
		if (getClass() == BiIntersection_IP.class)
			init();
	}

	@Override
	public void init() {
		super.init();
		addTitle("Chained Intersection");
	}

	@Override
	protected PsPanel getSouthPanel() {
		PsPanel pan = super.getSouthPanel();
		pan.addSubTitle("Ingredients:");
		pan.add(((BiIntersection) project).ch_ingredient1);
		pan.add(((BiIntersection) project).ch_ingredient2);
		pan.addSubTitle("New Input Geometry");
		pan.add(((BiIntersection) project).ch_inputSurf);
		return pan;
	}

	@Override
	protected PsPanel getOptionPanel() {
		PsPanel p4 = new PsPanel();
		p4.addLabelComponent("curves colours", project.chCurveColours);
		p4.addLabelComponent("Itterations", ((BiIntersection) project).chItts);
		return p4;
	}

}
