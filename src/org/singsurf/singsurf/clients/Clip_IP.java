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
public class Clip_IP extends SingSurf_IP {
	private static final long serialVersionUID = 1L;

	public Clip_IP() {
		super(false);
		if (getClass() == Clip_IP.class)
			init();
	}

	@Override
	public void init() {
		super.init();
		addTitle("Clip");
	}

	@Override
	protected PsPanel getOptionPanel() {
		PsPanel p4 = new PsPanel();
		p4.addLabelComponent("Itterations", ((Clip) project).chItts);

		return p4;
	}

	@Override
	protected PsPanel getSouthPanel() {
		PsPanel pan = super.getSouthPanel();

		pan.add(((Clip) project).cbInvert);
		pan.addSubTitle("New Input Geometry");
		pan.add(((Clip) project).ch_inputSurf);
		return pan;
	}
}
