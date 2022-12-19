/* @author rich
 * Created on 30-Mar-2005
 *
 * See LICENSE.txt for license information.
 */
package org.singsurf.singsurf.clients;

import java.awt.TextField;
import java.text.DecimalFormat;

import org.singsurf.singsurf.PuParameter;
import org.singsurf.singsurf.calculators.Calculator;
import org.singsurf.singsurf.definitions.Definition;
import org.singsurf.singsurf.geometries.GeomPair;
import org.singsurf.singsurf.geometries.GeomStore;
import org.singsurf.singsurf.jepwrapper.EvaluationException;
import org.singsurf.singsurf.operators.UnSuportedGeometryException;
import org.singsurf.singsurf.operators.VolCalcTerminal;
import org.singsurf.singsurf.operators.VolCalcTerminal.VolInfo;

import jv.project.PgGeometryIf;

/**
 * @author Rich Morris Created on 30-Mar-2005
 */
public class VolCalc extends AbstractOperatorProject {
	/** 
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	TextField volume;
	TextField area;
	TextField Cx;
	TextField Cy;
	TextField Cz;

	/********** Constructor *********/

	public VolCalc(GeomStore store, Definition def) {
		super(store, "VolCalc");
		if (getClass() == VolCalc.class) {
			init(def);
		}
	}

	public void calculate() {
		String name = this.ch_inputSurf.getSelectedItem();
		PgGeometryIf input = store.getGeom(name);
		calculate(input);

	}

	public void init(Definition def) {
		super.init();
		this.cbShowFace.setState(true);
		this.cbShowEdge.setState(false);
		this.cbShowVert.setState(false);
		this.chCurveColours.addItemListener(this);
		loadDefinition(def);
	}

	@Override
	public void loadDefinition(Definition newdef) {
		
		volume = new TextField();
		area = new TextField();
		Cx = new TextField();
		Cy = new TextField();
		Cz = new TextField();
	}

	@Override
	public void setDefinitionOptions(Definition def) {
	}

	
	


	@Override
	public PgGeometryIf calcGeomThread(GeomPair p) {
		return null;
	}

	@Override
	public void newActiveInput(String name) {
		if (activePairs.containsKey(name)) {
			showStatus(name + " is already active");
			return;
		}
		PgGeometryIf input = store.getGeom(name);
		calculate(input);
	}

	/**
	 * @param input
	 */
	public void calculate(PgGeometryIf input) {
		DecimalFormat fmt = new DecimalFormat();
		fmt.setMaximumFractionDigits(6);
		VolCalcTerminal vct = new VolCalcTerminal();
		try {
			VolCalcTerminal.VolInfo volinfo =  (VolInfo) vct.operate(input);
			volume.setText(fmt.format(volinfo.volume));
			area.setText(fmt.format(volinfo.area));
			Cx.setText(fmt.format(volinfo.centroid.getEntry(0)));
			Cy.setText(fmt.format(volinfo.centroid.getEntry(1)));
			Cz.setText(fmt.format(volinfo.centroid.getEntry(2)));

			
		} catch (UnSuportedGeometryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (EvaluationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public boolean update(Object o) {
		if (o instanceof PuParameter) {
			return parameterChanged((PuParameter) o);
		} else
			return super.update(o);
	}



	@Override
	public void setDisplayProperties() {
	}

	@Override
	public void geometryDefHasChanged(AbstractProject client, Calculator inCalc) {
	}

	@Override
	public void displayGeom(GeomPair p, PgGeometryIf result) {
		
	}

	@Override
	public void calcGeom(GeomPair p) {
		calculate(p.getInput());
	}

	@Override
	public void calcGeoms() {
		// TODO Auto-generated method stub
		super.calcGeoms();
	}


}
