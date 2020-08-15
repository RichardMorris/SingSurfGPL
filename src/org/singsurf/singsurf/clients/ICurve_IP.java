/* @author rich
 * Created on 31-Mar-2005
 *
 * See LICENSE.txt for license information.
 */
package org.singsurf.singsurf.clients;

import jv.object.PsPanel;
import jv.object.PsUpdateIf;

/**
 * @author Rich Morris Created on 31-Mar-2005
 */
public class ICurve_IP extends SingSurf_IP {
    private static final long serialVersionUID = -8025333797850761023L;

    /** Reference to main PjPsurfJepNew class */
    ICurve parent;

    /**
     * 
     */
    public ICurve_IP() {
    	super(false);
	// System.out.println("IP constructor");
    	if (getClass() == ICurve_IP.class)
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
		parent = (ICurve) par;
		// System.out.println("IP setParent");
		super.setParent(par);
	}

	@Override
	protected PsPanel getOptionPanel() {
		PsPanel p4 = new PsPanel();
		
		p4.add(parent.m_Clipping.assureInspector(PsPanel.INFO, PsPanel.INFO_EXT));

		p4.add(parent.cbCreateNew);
		
		p4.addLabelComponent("Scale",project.chScale);

		return p4;
	}
	
	@Override
	protected PsPanel getSouthPanel() {
		PsPanel south = new PsPanel();
		south.add(project.m_go);
		south.addSubTitle("New Input Geometry");
		south.add(parent.ch_inputSurf);
		
		south.addLabelComponent("Orientation",parent.chOrientation);
    	
    	south.addLabelComponent("Method", parent.chMethod);
		south.addLabelComponent("Length",
				parent.lengthControl.getControlPanel());
    	south.addLabelComponent("N steps",parent.numSteps);

		return south;
	}

}
