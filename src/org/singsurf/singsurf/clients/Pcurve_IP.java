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
public class Pcurve_IP extends SingSurf_IP {
	private static final long serialVersionUID = 1;

	public Pcurve_IP() {
		super(true);
		if (getClass() == Pcurve_IP.class)
			init();
	}

	@Override
	public void init() {
		super.init();
		addTitle("Parameterised Curve");
	}

	@Override
	protected PsPanel getOptionPanel() {
		PsPanel p4;
		p4 = new PsPanel();

		p4.add(((Pcurve) project).m_Clipping.assureInspector(PsPanel.INFO, PsPanel.INFO_EXT));
		// p4.add(project.cb_colour);
		p4.addLabelComponent("colours", project.chCurveColours);
		p4.add(project.cbCreateNew);
		// p4.add(project.cb_keepMat);
		p4.add(project.cbShowCurves);
		p4.add(project.cbShowVert);

		return p4;
	}

}
