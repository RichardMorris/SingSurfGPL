/* @author rich
 * Created on 31-Mar-2005
 *
 * See LICENSE.txt for license information.
 */
package org.singsurf.singsurf.clients;

import java.awt.Label;

import jv.object.PsPanel;

/**
 * @author Rich Morris Created on 31-Mar-2005
 */
public class Intersection_IP extends SingSurf_IP {
	private static final long serialVersionUID = 1;

	/** This button brings up a dialog to set all the ranges **/
//	protected	Button	bRngConfig;
	/** A Dialog box to specify the range **/
//	protected	RangeConfig myRngConfig;

	public Intersection_IP() {
		super(false);
		if (getClass() == Intersection_IP.class)
			init();
	}

	@Override
	public void init() {
		super.init();
		addTitle("Intersect");
	}

	@Override
	protected PsPanel getOptionPanel() {
		PsPanel p4 = new PsPanel();
//		p4.add(parent.m_Clipping.assureInspector(PsPanel.INFO, PsPanel.INFO_EXT));
		p4.add(new Label("Select geometry in Current Inputs to change appearance"));

		p4.addLabelComponent("colours", project.chCurveColours);
		p4.addLabelComponent("Itterations", ((Intersection) project).chItts);
		p4.add(project.cbCreateNew);
		p4.add(project.cbShowFace);
		p4.add(project.cbShowEdge);
		p4.add(project.cbShowCurves);
		p4.add(project.cbShowVert);
		p4.add(project.cbKeepMat);

		return p4;
	}

	@Override
	protected PsPanel getSouthPanel() {
		PsPanel pan = super.getSouthPanel();

		pan.addSubTitle("New Input Geometry");
		pan.add(((Intersection) project).ch_inputSurf);
//		pan.addSubTitle("Current Inputs");
//		pan.add(((Intersection) project).activeInputNames);
//
		return pan;
	}

}
