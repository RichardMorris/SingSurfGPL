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
public class GenICurve_IP extends SingSurf_IP {
    private static final long serialVersionUID = -8025333797850761023L;

    /** Reference to main PjPsurfJepNew class */
    GenICurve icparent;

    /**
     * 
     */
    public GenICurve_IP() {
    	super(false);
	// System.out.println("IP constructor");
    	if (getClass() == GenICurve_IP.class)
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
		icparent = (GenICurve) par;
		// System.out.println("IP setParent");
		super.setParent(par);
	}

	@Override
	protected PsPanel getOptionPanel() {
		PsPanel p4 = new PsPanel();

//		p4.add(parent.m_Clipping.assureInspector(PsPanel.INFO, PsPanel.INFO_EXT));

		p4.add(icparent.cbCreateNew);
		
		p4.addLabelComponent("Scale",project.chScale);

		return p4;
	}
	
	@Override
	protected PsPanel getSouthPanel() {
		PsPanel south = new PsPanel();
		south.add(project.m_go);
		
		south.addSubTitle("Ingredient:");
		south.add(icparent.ch_ingredient);

		south.addLabelComponent("Project", icparent.cbProject);
		south.addSubTitle("New Input Geometry");
		south.add(icparent.ch_inputSurf);
		
		south.addLabelComponent("Orientation",icparent.chOrientation);
    	
    	south.addLabelComponent("Method", icparent.chMethod);
		south.addLabelComponent("Length",
				icparent.lengthControl.getControlPanel());
    	south.addLabelComponent("N steps",icparent.numSteps);
    	south.addLabelComponent("Start dir",icparent.ch_start);
		return south;
	}

}
