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
public class GeneralizedClip_IP extends SingSurf_IP {
	private static final long serialVersionUID = 1L;

	/** This button brings up a dialog to set all the ranges **/
//	protected	Button	bRngConfig;
	/** A Dialog box to specify the range **/
//	protected	RangeConfig myRngConfig;

	public GeneralizedClip_IP() {
		super(false);
		if (getClass() == GeneralizedClip_IP.class)
			init();
	}

	@Override
	public void init() {
		super.init();
		addTitle("GeneralizedClip");
	}

	@Override
	protected PsPanel getOptionPanel() {
		PsPanel p4 = new PsPanel();
//			p4.add(parent.m_Clipping.assureInspector(PsPanel.INFO, PsPanel.INFO_EXT));

		p4.addLabelComponent("colours", project.chCurveColours);
		p4.addLabelComponent("Itterations", ((Clip) project).chItts);
		return p4;
	}

	@Override
	protected PsPanel getSouthPanel() {
		PsPanel pan = super.getSouthPanel();

		pan.add(((Clip) project).cbInvert);
		pan.addSubTitle("Ingredients:");
		pan.add(((GeneralizedClip) project).ch_ingredient);
		pan.addSubTitle("New Input Geometry");
		pan.add(((Clip) project).ch_inputSurf);
		return pan;
	}
}
