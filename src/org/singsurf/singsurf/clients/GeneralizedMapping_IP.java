/*
Created 6 Oct 2006 - Richard Morris
*/
package org.singsurf.singsurf.clients;

import jv.object.PsPanel;

public class GeneralizedMapping_IP extends SingSurf_IP {
	private static final long serialVersionUID = 1;

	public GeneralizedMapping_IP() {
		super(false);
		if (getClass() == GeneralizedMapping_IP.class)
			init();
	}

	@Override
	public void init() {
		super.init();
		addTitle("Chained Mapping");
	}

	@Override
	protected PsPanel getSouthPanel() {
		PsPanel pan = super.getSouthPanel();
		pan.add( ((GeneralizedMapping) project).cbParamsFromTexture);
		pan.addSubTitle("Ingredients:");
		pan.add(((GeneralizedMapping) project).ch_ingredient);
		pan.addSubTitle("New Input Geometry");
		pan.add(((GeneralizedMapping) project).ch_inputSurf);
		return pan;
	}

	protected PsPanel getOptionPanel() {

		PsPanel p4 = new PsPanel();

		p4.addLabelComponent("Clipping",((Mapping) project).m_Clipping);
		p4.addLabelComponent("Continuity",((Mapping) project).m_ContDist);

		return p4;
	}

}
