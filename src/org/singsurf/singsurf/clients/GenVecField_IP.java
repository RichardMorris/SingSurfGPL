/* @author rich
 * Created on 31-Mar-2005
 *
 * See LICENSE.txt for license information.
 */
package org.singsurf.singsurf.clients;

import java.awt.Component;
import java.awt.GridLayout;
import java.awt.Label;

import jv.object.PsPanel;
import jv.object.PsUpdateIf;

/**
 * @author Rich Morris Created on 31-Mar-2005
 */
public class GenVecField_IP extends SingSurf_IP {
    private static final long serialVersionUID = -8025333797850761023L;

    /** Reference to main PjPsurfJepNew class */
    GenVecField vfparent;

    /**
     * 
     */
    public GenVecField_IP() {
	super(false);
	// System.out.println("IP constructor");
	if (getClass() == GenVecField_IP.class)
	    init();
	// System.out.println("IP constructor done");
    }

    // Initialization.
    @Override
    public void init() {
	System.out.println("GenVecFieldIP init");

	super.init();
	addTitle("Vector Field");
	// System.out.println("IP init done");
    }

	@Override
	public void setParent(PsUpdateIf par) {
		vfparent = (GenVecField) par;
		super.setParent(par);
	}


	@Override
	protected PsPanel getOptionPanel() {
		PsPanel p4 = new PsPanel();
		p4.add(vfparent.m_Clipping.assureInspector(PsPanel.INFO, PsPanel.INFO_EXT));

		p4.addLabelComponent("Field 1: ",project.chCurveColours);
		p4.addLabelComponent("Field 2", vfparent.chColours2);

//		p4.addLabelComponent("Show vectors",vfparent.cbShowVect);

		return p4;
	}

	
	protected PsPanel getInputsPanel() {
		PsPanel p1 = new PsPanel();
		Component comp = ((AbstractOperatorProject) project).activeInputNames;
		p1.add(comp);
		p1.add(new Label("With selected:"));
//		p1.add(project.cbShowFace);
//		p1.add(project.cbShowEdge);
//		p1.add(project.cbShowVert);
//		p1.add(project.cbShowCurves);
//		p1.add(project.cbShowPoints);
//		p1.add(project.cbShowBoundary);

//		p1.addLabelComponent("surf colours: ",project.chSurfColours);
		p1.addLabelComponent("Show vectors",vfparent.cbShowVect);

		
		p1.add(((AbstractOperatorProject) project).removeInputButton);
		p1.add(((AbstractOperatorProject) project).removeInputGeomButton);
		p1.add(((AbstractOperatorProject) project).removeInputDepButton);
		
		return p1;
	}
    protected PsPanel buildOrientationSubPanel() {
    	PsPanel pan = new PsPanel(new GridLayout(2, 2));
    	pan.add(new Label("Orientation"));
    	pan.add(vfparent.chOrientation);
    	pan.add(vfparent.lengthControl.getLabel());
    	pan.add(vfparent.lengthControl.getControlPanel());
    	return pan;
    }

	@Override
	protected PsPanel getSouthPanel() {
		PsPanel south = new PsPanel();
		south.add(project.m_go);
		south.add( buildOrientationSubPanel());
		south.add(vfparent.cbProject);
		
		south.addSubTitle("Ingredient:");
		south.add(vfparent.ch_ingredient);

		south.addSubTitle("New Input Geometry");
		south.add(vfparent.ch_inputSurf);

		return south;
	}
    
	
}
