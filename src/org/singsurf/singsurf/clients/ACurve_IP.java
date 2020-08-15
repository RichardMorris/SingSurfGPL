/* @author rich
 * Created on 31-Mar-2005
 *
 * See LICENSE.txt for license information.
 */
package org.singsurf.singsurf.clients;

import java.awt.FlowLayout;
import java.awt.Label;
import java.awt.Panel;

import jv.object.PsPanel;

/**
 * @author Rich Morris Created on 31-Mar-2005
 */
public class ACurve_IP extends SingSurf_IP {
	private static final long serialVersionUID = -8025333797850761023L;

	/**
	 * 
	 */
	public ACurve_IP() {
		super(false);
		if (getClass() == ACurve_IP.class)
			init();
	}

	@Override
	public void init() {
		super.init();
		addTitle("Algebraic Curve");
	}

	@Override
	protected PsPanel getOptionPanel() {
		PsPanel p4 = new PsPanel();

		Panel p4a = new Panel(new FlowLayout(FlowLayout.LEFT, 1, 0));
		p4a.add(new Label("Resolution:"));
		// p4a.add(((ACurve) project).cb_c_4);
		// p4a.add(parent.cb_c_8);
		p4a.add(((ACurve) project).cb_c_16);
		p4a.add(((ACurve) project).cb_c_32);
		p4a.add(((ACurve) project).cb_c_64);
		p4.add(p4a);
		Panel p4b = new Panel(new FlowLayout(FlowLayout.LEFT, 1, 0));
		p4b.add(new Label("           "));
		p4b.add(((ACurve) project).cb_c_128);
		p4b.add(((ACurve) project).cb_c_256);
		// p4b.add(new Label("Fine:"));
		// p4b.add(project.cb_fi_8);
		// p4b.add(project.cb_fi_16);
		// p4b.add(project.cb_fi_32);
		// p4b.add(project.cb_fi_64);
		// p4b.add(project.cb_fi_128);
		// p4b.add(project.cb_fi_256);
		// p4b.add(project.cb_fi_512);
		// p4b.add(project.cb_fi_1024);
		p4.add(p4b);

		// p4.add(project.cb_colour);
		PsPanel p5 = new PsPanel(new FlowLayout(FlowLayout.RIGHT));
		p5.add(new Label("colours: "));
		p5.add(project.chCurveColours);
		p4.add(p5);
		p4.add(project.cbCreateNew);
		// p4.add(project.cb_keepMat);
		p4.add(project.cbShowCurves);
		p4.add(project.cbShowVert);
		return p4;
	}
}
