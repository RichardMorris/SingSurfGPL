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
public class Psurf_IP extends SingSurf_IP {
	private static final long serialVersionUID = -6486350319171058728L;

	public Psurf_IP() {
		super(true);
		if (getClass() == Psurf_IP.class)
			init();
	}

	public void init() {
		super.init();
		addTitle("PSurf");
	}

	@Override
	protected PsPanel getOptionPanel() {
		PsPanel p4 = new PsPanel();
		p4.addLabelComponent("clipping", ((Psurf) project).m_Clipping);
		p4.addLabelComponent("Surface colour", project.chSurfColours);
		p4.addLabelComponent("Max Colour val", ((Psurf) project).colourMax);
		p4.addLabelComponent("Min Colour val", ((Psurf) project).colourMin);
		p4.add(project.cbShowFace);
		p4.add(project.cbShowEdge);
		p4.add(project.cbShowVert);
		return p4;
	}
}
