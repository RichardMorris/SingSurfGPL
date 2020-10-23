/* @author rich
 * Created on 31-Mar-2005
 *
 * See LICENSE.txt for license information.
 */
package org.singsurf.singsurf.clients;

import jv.object.PsPanel;

/**
 * @author Rich Morris Created on 11-April-2020
 */
public class Extrude_IP extends SingSurf_IP {
	private static final long serialVersionUID = 1L;

	/** This button brings up a dialog to set all the ranges **/
//	protected	Button	bRngConfig;
	/** A Dialog box to specify the range **/
//	protected	RangeConfig myRngConfig;

	public Extrude_IP() {
		super(true);
		if (getClass() == Extrude_IP.class)
			init();
	}

	@Override
	public void init() {
		super.init();
		addTitle("Extrude");
	}

	@Override
	protected PsPanel getOptionPanel() {
		PsPanel p4 = new PsPanel();

		return p4;
	}

	@Override
	protected PsPanel getSouthPanel() {
		PsPanel pan = super.getSouthPanel();
		pan.add(((Extrude) project).cbAsLineBundle);

		pan.addSubTitle("New Input Geometry");
		pan.add(((Extrude) project).ch_inputSurf);
		return pan;
	}
}
