/*
Created 6 Oct 2006 - Richard Morris
*/
package org.singsurf.singsurf.clients;

import jv.object.PsPanel;

public class GeneralizedIntersection_IP extends SingSurf_IP {
	private static final long serialVersionUID = 1L;

	public GeneralizedIntersection_IP() {
		super(false);
		if (getClass() == GeneralizedIntersection_IP.class)
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
		pan.add( ((GeneralizedIntersection) project).cbProject);
		pan.add( ((GeneralizedIntersection) project).cbParamsFromTexture);
		pan.addSubTitle("Ingredients:");
		pan.add(((GeneralizedIntersection) project).ch_ingredient);
		pan.addSubTitle("New Input Geometry");
		pan.add(((GeneralizedIntersection) project).ch_inputSurf);
		return pan;
	}

	protected PsPanel getOptionPanel() {
		PsPanel p4 = new PsPanel();
		p4.addLabelComponent("Itterations", ((GeneralizedIntersection) project).chItts);
		return p4;
	}
}
