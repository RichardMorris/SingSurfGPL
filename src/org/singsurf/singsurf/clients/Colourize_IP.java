/* @author rich
 * Created on 31-Mar-2005
 *
 * See LICENSE.txt for license information.
 */
package org.singsurf.singsurf.clients;

import jv.object.PsPanel;
import jv.object.PsUpdateIf;

/**
 * @author Rich Morris
 */
public class Colourize_IP extends SingSurf_IP {
	private static final long serialVersionUID = 1L;
	Colourize colourizer;

	public Colourize_IP() {
		super(false);
		if (getClass() == Colourize_IP.class)
			init();
	}

	@Override
	public void init() {
		super.init();
		addTitle("Colourize");
	}

	
	@Override
	public void setParent(PsUpdateIf par) {
		colourizer = (Colourize)par;
		super.setParent(par);

	}

	@Override
	protected PsPanel getOptionPanel() {
		PsPanel p4 = new PsPanel();
		p4.add(project.cbShowFace);
		p4.add(project.cbShowEdge);
		p4.add(project.cbShowCurves);
		p4.add(project.cbShowVert);
		p4.add(project.cbShowPoints);
		return p4;
	}

	@Override
	protected PsPanel getSouthPanel() {
		PsPanel pan = super.getSouthPanel();
		pan.add(colourizer.cbUseTextureCoords);
		pan.add(colourizer.cbParamsFromTexture);
		pan.addSubTitle("New Input Geometry");
		pan.add(colourizer.ch_inputSurf);
		return pan;
	}

}
