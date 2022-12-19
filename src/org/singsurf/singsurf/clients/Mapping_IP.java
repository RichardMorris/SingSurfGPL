/* @author rich
 * Created on 31-Mar-2005
 *
 * See LICENSE.txt for license information.
 */
package org.singsurf.singsurf.clients;

import jv.object.PsPanel;

/**
 * @author Rich Morris Created on 31-Mar-2005
 */
public class Mapping_IP extends SingSurf_IP {
	private static final long serialVersionUID = -5968740970457076690L;

	public Mapping_IP() {
		super(false);
		if (getClass() == Mapping_IP.class)
			init();
	}

	@Override
	public void init() {
		super.init();
		addTitle("Mapping");
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
		PsPanel south = super.getSouthPanel();
		south.addSubTitle("New Input Geometry");
		south.add(((AbstractOperatorProject) project).ch_inputSurf);
		return south;
	}

}
