/* @author rich
 * Created on 31-Mar-2005
 *
 * See LICENSE.txt for license information.
 */
package org.singsurf.singsurf.clients;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Label;

import javax.swing.Box;

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
		PsPanel p4 = new PsPanel(new GridLayout(0, 2));
		p4.add(new Label("Volume"));
		p4.add(((VolCalc) project).volume );
		p4.add(new Label("Area"));
		p4.add(((VolCalc) project).area );
		p4.add(new Label("Cx"));
		p4.add(((VolCalc) project).Cx );
		p4.add(new Label("Cy"));
		p4.add(((VolCalc) project).Cy );
		p4.add(new Label("Cz"));
		p4.add(((VolCalc) project).Cz );
		p4.add(new  Box.Filler(
				new Dimension((short) 0,(short) 0), 
				new Dimension((short) 0,(short) 500),
				new Dimension(Short.MAX_VALUE, Short.MAX_VALUE) ));

		return p4;
	}

}
