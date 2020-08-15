/*
Created 6 Oct 2006 - Richard Morris
*/
package org.singsurf.singsurf.clients;

import jv.object.PsPanel;

public class BiMap_IP extends SingSurf_IP {
	private static final long serialVersionUID = 1;

	public BiMap_IP() {
		super(false);
				if (getClass() == BiMap_IP.class)
					init();
	}

	@Override
	public void init() {
		super.init();
		addTitle("Bi Mapping");
	}

	@Override
	protected PsPanel getOptionPanel() {
		PsPanel p4 = new PsPanel();
		p4.addLabelComponent("Clipping",((Mapping) project).m_Clipping);
		p4.addLabelComponent("Continuity",((Mapping) project).m_ContDist);

		return p4;
	}

	@Override
	protected PsPanel getSouthPanel() {
		PsPanel pan = super.getSouthPanel();
		pan.addSubTitle("Ingredients:");
		pan.add(((BiMap)project).ch_ingredient1);
        pan.add(((BiMap)project).ch_ingredient2);
		pan.addSubTitle("New Input Geometry");
		pan.add(((BiMap)project).ch_inputSurf);
		return pan;
	}

}
