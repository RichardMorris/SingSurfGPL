/* @author rich
 * Created on 31-Mar-2005
 *
 * See LICENSE.txt for license information.
 */
package org.singsurf.singsurf.clients;

import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Label;

import jv.object.PsPanel;
import jv.object.PsUpdateIf;

/**
 * @author Rich Morris Created on 31-Mar-2005
 */
public class VecField_IP extends SingSurf_IP {
    private static final long serialVersionUID = -8025333797850761023L;

    /** Reference to main PjPsurfJepNew class */
    VecField parent;

    /**
     * 
     */
    public VecField_IP() {
    	super(false);
	// System.out.println("IP constructor");
    	if (getClass() == VecField_IP.class)
    		init();
	// System.out.println("IP constructor done");
    }

    // Initialization.
    @Override
    public void init() {
	 System.out.println("VField init");

	super.init();
	addTitle("Vector Field");
	// System.out.println("IP init done");
    }

	@Override
	public void setParent(PsUpdateIf par) {
		parent = (VecField) par;
		// System.out.println("IP setParent");
		super.setParent(par);
		
	}

	protected PsPanel buildOrientationSubPanel() {
    	PsPanel pan = new PsPanel(new GridLayout(2, 2));
    	pan.add(new Label("Orientation"));
    	pan.add(parent.chOrientation);
    	pan.add(parent.lengthControl.getLabel());
    	pan.add(parent.lengthControl.getControlPanel());
    	return pan;
    }

	public void superSetParent(PsUpdateIf par) {
		super.setParent(par);
		parent = (VecField) par;
	}

	@Override
	protected PsPanel getOptionPanel() {
		PsPanel p4 = new PsPanel();
		
		p4.add(parent.m_Clipping.assureInspector(PsPanel.INFO, PsPanel.INFO_EXT));
		PsPanel p6 = new PsPanel(new FlowLayout(FlowLayout.RIGHT));
		p6.add(new Label("colours: "));
		p6.add(parent.chCurveColours);
		p4.add(p6);

		PsPanel p6a = new PsPanel(new FlowLayout(FlowLayout.RIGHT));
		p6a.add(new Label("colours 2: "));
		p6a.add(parent.chColours2);
		p4.add(p6a);

		p4.add(parent.cbCreateNew);
		// p4.add(parent.cb_keepMat);
		p4.add(parent.cbShowVect);
		return p4;
	}
	
	@Override
	protected PsPanel getSouthPanel() {
		PsPanel south = super.getSouthPanel();
		south.addSubTitle("New Input Geometry");
		south.add(parent.ch_inputSurf);
		south.add( buildOrientationSubPanel());
		return south;
	}

}
