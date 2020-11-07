/* @author rich
 * Created on 30-Mar-2005
 *
 * See LICENSE.txt for license information.
 */
package org.singsurf.singsurf.clients;

import java.awt.Choice;
import java.awt.ItemSelectable;
import java.awt.event.ItemEvent;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.singsurf.singsurf.LParamList;
import org.singsurf.singsurf.PuParameter;
import org.singsurf.singsurf.calculators.Calculator;
import org.singsurf.singsurf.definitions.DefVariable;
import org.singsurf.singsurf.definitions.Definition;
import org.singsurf.singsurf.definitions.Option;
import org.singsurf.singsurf.geometries.GeomPair;
import org.singsurf.singsurf.geometries.GeomStore;
import org.singsurf.singsurf.jepwrapper.EvaluationException;
import org.singsurf.singsurf.operators.SimpleCalcIntersection;
import org.singsurf.singsurf.operators.UnSuportedGeometryException;

import jv.geom.PgElementSet;
import jv.geom.PgPointSet;
import jv.geom.PgPolygonSet;
import jv.number.PuDouble;
import jv.object.PsDebug;
import jv.project.PgGeometryIf;

/**
 * @author Rich Morris Created on 30-Mar-2005
 */
public class Intersection extends AbstractOperatorClient {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4659133739866394363L;

	/** Default name for geometries **/

//    protected String my_baseName = "intersect";

	// PuVariable displayVars[];
	protected PuDouble m_Clipping;
	int globalSteps = 40;
	DefVariable localX, localY, localZ;

	Choice chItts = new Choice();;

	public Intersection(GeomStore store, Definition def) {
		super(store, def == null ? "Intersection" : def.getName());
		if (getClass() == Intersection.class) {
//			if (def == null)
//				def = createDefaultDef();
			init(def);
		}
	}

	public void init(Definition def) {
		super.init();
		newParams = new LParamList(this);

		chItts.addItem("1");
		chItts.addItem("2");
		chItts.addItem("3");
		chItts.addItem("4");
		chItts.addItem("5");
		chItts.addItem("6");
		chItts.addItem("7");
		chItts.addItem("8");
		chItts.addItem("9");
		chItts.select(4);
		chItts.addItemListener(this);

		loadDefinition(def);
	}

	void checkDef(Definition def) {
	}

	@Override
	public void loadDefinition(Definition newdef) {
		Definition def = newdef.duplicate();
		checkDef(def);
		def.setName(this.getName());
		this.getInfoPanel().setTitle(this.getName());
		calc = new Calculator(def, 0);
		calc.build();
		// calc.requireDerivative(names)
		if (!calc.isGood())
			showStatus(calc.getMsg());
		// ch_inputSurf.setEnabled(calc.isGood());
		localX = calc.getDefVariable(0);
		localY = calc.getDefVariable(1);
		localZ = calc.getDefVariable(2);
		setDisplayEquation(def.getEquation());
		refreshParams();

		Option sfopt = def.getOption("showFace");
		if (sfopt != null)
			this.cbShowFace.setState(sfopt.getBoolVal());
		Option seopt = def.getOption("showEdge");
		if (seopt != null)
			this.cbShowEdge.setState(seopt.getBoolVal());

		Option svopt = def.getOption("showVert");
		if (svopt != null)
			this.cbShowVert.setState(svopt.getBoolVal());
		Option scopt = def.getOption("showCurve");
		if (scopt != null)
			this.cbShowCurves.setState(scopt.getBoolVal());
		Option colopt = def.getOption("colour");
		if (colopt != null)
			this.chCurveColours.select(colopt.getStringVal());

		Option ittopt = def.getOption("numItts");
		if (ittopt != null)
			this.chItts.select(ittopt.getStringVal());

		// calcSurf();
	}

	@Override
	public void setDefinitionOptions(Definition def) {

		def.setOption("showFace", this.cbShowFace.getState());
		def.setOption("showEdge", this.cbShowEdge.getState());

		def.setOption("showVert", this.cbShowVert.getState());
		def.setOption("showCurve", this.cbShowCurves.getState());
		def.setOption("colour", this.chCurveColours.getSelectedItem());
		def.setOption("numItts", Integer.parseInt(this.chItts.getSelectedItem()));
	}

	@Override
	protected List<String> getPossibleCurveColours() {
		return Arrays.asList("Unchanged",
		"Colours from XYZ",
		"Red",
		"Green",
		"Blue",
		"Cyan",
		"Magenta",
		"Yellow",
		"Black",
		"Grey",
		"White");
	}

	@Override
	protected List<String> getPossibleSurfaceColours() {
		return Collections.emptyList();
	}

	
	@Override
	public PgGeometryIf calcGeomThread(GeomPair p) {
		if (!calc.isGood()) {
			showStatus(calc.getMsg());
			return null;
		}
		PgGeometryIf input = p.getInput();

		if (input == null) {
			showStatus("Null input");
			return null;
		}

		PgGeometryIf resultGeom = null;
		try {
			resultGeom = calcGeom(input);
		} catch (UnSuportedGeometryException e) {
			PsDebug.error("Intersection could not be calculated");
			return null;
		} catch (EvaluationException e) {
			System.out.println(e.toString());
			calc.setGood(false);
			return null;
		}

		if (resultGeom == null) {
			PsDebug.error("Intersection could not be calculated");
			return null;
		}

		return resultGeom;
	}
	
	public void displayGeom(GeomPair p, PgGeometryIf resultGeom) {
	
		System.out.println("mapped outer");
		// debugCols((PgElementSet) mappedGeom);
		PgGeometryIf out = p.getOutput();
//		PgGeometryIf input = p.getInput();

		GeomStore.copySrcTgt(resultGeom, out);
		setDisplayProperties(out);
		setGeometryInfo(out,p.getInput());
		store.geomChanged(out);
	}

	public PgGeometryIf calcGeom(PgGeometryIf input) throws UnSuportedGeometryException, EvaluationException {
		PgGeometryIf resultGeom;
		SimpleCalcIntersection intersectAlgorithm = null;
		intersectAlgorithm = new SimpleCalcIntersection(calc.createEvaluator(),Integer.parseInt(chItts.getSelectedItem()));
		resultGeom = intersectAlgorithm.operate(input);
		return resultGeom;
	}

	@Override
	public void newActiveInput(String name) {
		if (activePairs.containsKey(name)) {
			showStatus(name + " is already active");
			return;
		}
		PgGeometryIf input = store.getGeom(name);
		PgGeometryIf output = null;
		if (input instanceof PgElementSet)
			output = store.aquireCurve(getPreferredOutputName(name), this);
		else if (input instanceof PgPolygonSet)
			output = store.aquirePoints(getPreferredOutputName(name), this);
		else if (input instanceof PgPointSet)
			output = store.aquirePoints(getPreferredOutputName(name), this);
		GeomPair p = new GeomPair(input, output);
//		this.setCheckboxesFromGeomety(p.getOutput());
		setDisplayProperties(p.getOutput());
		activePairs.put(name, p);
		activeInputNames.add(name);
		calcGeom(p);
		store.newPair(this, p);
	}

	@Override
	public boolean update(Object o) {
		if (o instanceof PuParameter) {
			return parameterChanged((PuParameter) o);
		} else if (o == m_Clipping)
			return true;
		else
			return super.update(o);
	}

	/**
	 * Handles the selection of a new surface definition.
	 */
	@Override
	public void itemStateChanged(ItemEvent e) {
		ItemSelectable itSel = e.getItemSelectable();
		if (itSel == chDefs) {
			int i = chDefs.getSelectedIndex();
			loadDefinition(lsmpDefs[i]);
		} else if (itSel == chItts) {
		} else
			super.itemStateChanged(e);
	}

	@Override
	public void geometryDefHasChanged(AbstractClient client, Calculator inCalc) {
	}

}
