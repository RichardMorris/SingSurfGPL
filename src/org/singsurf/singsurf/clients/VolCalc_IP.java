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
public class VolCalc_IP extends SingSurf_IP {
	private static final long serialVersionUID = -5968740970457076690L;

	public VolCalc_IP() {
		super(false);
		if (getClass() == VolCalc_IP.class)
			init();
	}

	@Override
	public void init() {
		super.init();
		addTitle("Volume Calculator");
	}

	@Override
	protected PsPanel getOptionPanel() {
		PsPanel p4 = new PsPanel();
		return p4;
	}

	
	
	@Override
	protected PsPanel getSouthPanel() {
		PsPanel south = super.getSouthPanel();
		south.addSubTitle("New Input Geometry");
		south.add(((AbstractOperatorClient) project).ch_inputSurf);
		return south;
	}

	@Override
	protected PsPanel getDefinitionPanel() {
		PsPanel p4 = new PsPanel();
		p4.add(((VolCalc) project).output );
		return p4;
	}

}
