/* @author rich
 * Created on 30-Mar-2005
 *
 * See LICENSE.txt for license information.
 */
package org.singsurf.singsurf.clients;

import java.awt.TextField;

import org.singsurf.singsurf.PuParameter;
import org.singsurf.singsurf.calculators.Calculator;
import org.singsurf.singsurf.definitions.Definition;
import org.singsurf.singsurf.geometries.GeomPair;
import org.singsurf.singsurf.geometries.GeomStore;
import org.singsurf.singsurf.jepwrapper.EvaluationException;
import org.singsurf.singsurf.operators.UnSuportedGeometryException;
import org.singsurf.singsurf.operators.VolCalcTerminal;

import jv.project.PgGeometryIf;

/**
 * @author Rich Morris Created on 30-Mar-2005
 */
public class VolCalc extends AbstractOperatorClient {
	/** 
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	TextField output;

	/********** Constructor *********/

	public VolCalc(GeomStore store, Definition def) {
		super(store, "VolCalc");
		if (getClass() == VolCalc.class) {
			init(def);
		}
	}

	public void calculate() {
//		equationChanged(taDef.getText());
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
		
		output = new TextField();
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
		VolCalcTerminal vct = new VolCalcTerminal();
		try {
			double volume = (Double) vct.operate(input);
			output.setText("Volume "+volume);
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
	public void geometryDefHasChanged(AbstractClient client, Calculator inCalc) {
	}

	@Override
	public void displayGeom(GeomPair p, PgGeometryIf result) {
		
	}


}
