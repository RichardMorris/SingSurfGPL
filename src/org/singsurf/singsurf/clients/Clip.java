/* @author rich
 * Created on 30-Mar-2005
 *
 * See LICENSE.txt for license information.
 */
package org.singsurf.singsurf.clients;

import java.awt.Checkbox;
import java.awt.Choice;
import java.awt.ItemSelectable;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Arrays;
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
import org.singsurf.singsurf.operators.CalcClip;
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
public class Clip extends AbstractOperatorClient {
	private static final long serialVersionUID = 1L;

	/** The name for the program */
	protected static final String programName = "Clip";

	// PuVariable displayVars[];
	protected PuDouble m_Clipping;
	int globalSteps = 40;
	DefVariable localX, localY, localZ;


	Choice chItts = new Choice();
	Checkbox cbInvert;
	
	public Clip(GeomStore store, Definition def) {
		super(store, def == null ? "Clip" : def.getName());
		if (getClass() == Clip.class) {
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

		cbInvert = new Checkbox("invert",false);
		cbInvert.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {

			}});
		loadDefinition(def);
	}

	@SuppressWarnings("unused")
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

		setStandardControlsFromOptions();

		loadFromDefOption(def,"colour",chCurveColours);

		Option ittopt = def.getOption("numItts");
		if (ittopt != null)
			this.chItts.select(ittopt.getStringVal());

		setCheckboxStateFromOption(cbInvert,def,"invert");
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
		def.setOption("invert",cbInvert.getState());
	}
	
	protected List<String> getPossibleSurfaceColours() {
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


	
	public void displayGeom(GeomPair pair, PgGeometryIf resultGeom) {
			PgGeometryIf input = pair.getInput();
			PgGeometryIf out = pair.getOutput();
			GeomStore.copySrcTgt(resultGeom, out);
			if (input instanceof PgElementSet) {
				((PgElementSet) out).setGlobalElementColor(((PgElementSet) input).getGlobalElementColor());
				if (((PgElementSet) input).isShowingElementColors())
					((PgElementSet) out).showElementColors(true);
				if (((PgElementSet) input).hasVertexColors()) {
					((PgElementSet) out).makeElementFromVertexColors();
				} else if (((PgElementSet) input).hasElementColors()) {
					((PgElementSet) out).showElementColors(true);
				}
				((PgElementSet) out).removeVertexNormals();
			} else if (input instanceof PgPolygonSet) {
				((PgPolygonSet) out).setGlobalPolygonColor(((PgPolygonSet) input).getGlobalPolygonColor());
				if (((PgPolygonSet) input).isShowingPolygonColors())
					((PgPolygonSet) out).showPolygonColors(true);

			} else {
				if (((PgPointSet) input).isShowingVertexColors())
					((PgPointSet) out).showVertexColors(true);

			}
			setDisplayProperties(out);
			setGeometryInfo(out,input);
			store.geomChanged(out);
	}
	
	public PgGeometryIf calcGeomThread(GeomPair pair) {
			PgGeometryIf resultGeom = (PgPointSet) pair.getInput().clone();	

			CalcClip clipAlgorithm = new CalcClip(calc.createEvaluator(), chItts.getSelectedIndex(),cbInvert.getState());

			try {
				clipAlgorithm.operate(resultGeom);
			} catch (UnSuportedGeometryException e) {
				PsDebug.error("Clipping could not be calculated");
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


	@Override
	public void newActiveInput(String name) {
		if (activePairs.containsKey(name)) {
			showStatus(name + " is already active");
			return;
		}
		PgGeometryIf input = store.getGeom(name);
		PgGeometryIf output = null;
		if (input instanceof PgElementSet)
			output = store.aquireSurface(getPreferredOutputName(name), this);
		else if (input instanceof PgPolygonSet)
			output = store.aquireCurve(getPreferredOutputName(name), this);
		else if (input instanceof PgPointSet)
			output = store.aquirePoints(getPreferredOutputName(name), this);
		GeomPair p = new GeomPair(input, output);
		this.setCheckboxesFromGeomety(p.getOutput());
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
//			int i = chItts.getSelectedIndex();
		} else
			super.itemStateChanged(e);
	}

	public String makeCGIstring() {
		return null;
	}

	@Override
	public void geometryDefHasChanged(AbstractClient client, Calculator inCalc) {
		// TODO
	}

}
