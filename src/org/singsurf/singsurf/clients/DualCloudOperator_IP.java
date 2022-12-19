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
public class DualCloudOperator_IP extends SingSurf_IP {
	private static final long serialVersionUID = -5968740970457076690L;

	public DualCloudOperator_IP() {
		super(false);
		if (getClass() == DualCloudOperator_IP.class)
			init();
	}

	@Override
	public void init() {
		super.init();
		addTitle("Dual");
	}

	@Override
	protected PsPanel getOptionPanel() {
		PsPanel p4 = new PsPanel();
		PsPanel p4a = new PsPanel();
		p4a.setLayout(new GridLayout(0,2));
		p4a.add(new Label("Num Lines"));
		p4a.add(((DualCloudOperator) project).numLines);
		p4a.add(new Label("Resolution"));
		p4a.add(((DualCloudOperator) project).resolution);

		p4.add(p4a);
		p4.add(new  Box.Filler(
				new Dimension((short) 0,(short) 0), 
				new Dimension((short) 0,(short) 500),
				new Dimension(Short.MAX_VALUE, Short.MAX_VALUE) ));

		return p4;
	}

	@Override
	protected PsPanel getSouthPanel() {
		PsPanel south = super.getSouthPanel();
		south.addSubTitle("New Input Geometry");
		south.add(((DualCloudOperator) project).ch_inputSurf);
		return south;
	}

}
