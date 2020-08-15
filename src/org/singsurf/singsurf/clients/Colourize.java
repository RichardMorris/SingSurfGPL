/* @author rich
 * Created on 30-Mar-2005
 *
 * See LICENSE.txt for license information.
 */
package org.singsurf.singsurf.clients;

import java.awt.Checkbox;
import java.awt.ItemSelectable;
import java.awt.event.ItemEvent;

import org.singsurf.singsurf.LParamList;
import org.singsurf.singsurf.PuParameter;
import org.singsurf.singsurf.calculators.Calculator;
import org.singsurf.singsurf.definitions.DefType;
import org.singsurf.singsurf.definitions.DefVariable;
import org.singsurf.singsurf.definitions.Definition;
import org.singsurf.singsurf.geometries.GeomPair;
import org.singsurf.singsurf.geometries.GeomStore;
import org.singsurf.singsurf.operators.ColourCalcMap;
import org.singsurf.singsurf.operators.UnSuportedGeometryException;

import org.singsurf.singsurf.jepwrapper.EvaluationException;

import jv.geom.PgElementSet;
import jv.geom.PgPointSet;
import jv.geom.PgPolygonSet;
import jv.project.PgGeometryIf;

/**
 * @author Rich Morris Created on 30-Mar-2005
 */
public class Colourize extends AbstractOperatorClient {
	/** 		
	 * 
	 */
	private static final long serialVersionUID = -4659133739866394363L;

	/** The operator which performs the mapping */

	Checkbox cbUseTextureCoords;
	Checkbox cbParamsFromTexture;

	/********** Constructor *********/

	public Colourize(GeomStore store, String name) {
		super(store, name);
		if (getClass() == Colourize.class) {
			init(this.createDefaultDef());
		}
	}

	public Colourize(GeomStore store, Definition def) {
		super(store, def.getName());
		if (getClass() == Colourize.class) {
			init(def);
		}
	}

	public void init(Definition def) {
		super.init();
		newParams = new LParamList(this);
		this.cbShowFace.setState(true);
		this.cbShowEdge.setState(false);
		this.cbShowVert.setState(false);
		cbUseTextureCoords = new Checkbox("Use texture coords",false);
		cbParamsFromTexture = new Checkbox("Parameters from texture",false);
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
		if (!calc.isGood())
			showStatus(calc.getMsg());
		ch_inputSurf.setEnabled(calc.isGood());
		
		setDisplayEquation(def.getEquation());
		refreshParams();
		// calcSurf();
		getDefinitionOptions(def);
	}
	
	public void getDefinitionOptions(Definition def) {
		
		setStandardControlsFromOptions();

		setCheckboxStateFromOption(cbUseTextureCoords,def,"useTextures");		
		setCheckboxStateFromOption(cbParamsFromTexture,def,"parametersFromTextures");		
	}

	@Override
	public void setDefinitionOptions(Definition def) {

		def.setOption("showFace", this.cbShowFace.getState());
		def.setOption("showEdge", this.cbShowEdge.getState());

		def.setOption("showVert", this.cbShowVert.getState());
		def.setOption("showCurve", this.cbShowCurves.getState());
		def.setOption("showPoint", this.cbShowPoints.getState());
		
		def.setOption("useTextures", cbUseTextureCoords.getState());		
		def.setOption("parametersFromTextures", cbParamsFromTexture.getState());		
	}

	@Override
	public PgGeometryIf calcGeomThread(GeomPair p) {
		if (calc != null && !calc.isGood()) {
			showStatus(calc.getMsg());
			return null;
		}
		ColourCalcMap map = new ColourCalcMap(calc.createEvaluator());

		PgGeometryIf input = p.getInput();
		PgGeometryIf out = p.getOutput();
		GeomStore.copySrcTgt(input, out);
		((PgPointSet) out).showVertexColors(true);

		try {
			map.setUseTextureCoordinates(cbUseTextureCoords.getState());
			map.setParamsFromTexture(cbParamsFromTexture.getState());
			map.operate(out);
		} catch (UnSuportedGeometryException e) {
			showStatus("Unsupported geometry type");
			return null;
		} catch (EvaluationException e) {
			System.out.println(e.toString());
			calc.setGood(false);
			return null;
		}


		return out;
	}
	
	public void displayGeom(GeomPair p, PgGeometryIf mappedGeom) {
	
		System.out.println("mapped outer");
		// debugCols((PgElementSet) mappedGeom);
		PgGeometryIf out = p.getOutput();
//		PgGeometryIf input = p.getInput();

		if (out instanceof PgElementSet) {
			((PgElementSet) out).assureElementColors();
			// ((PgElementSet) out).showElementFromVertexColors(true);
			((PgElementSet) out).showSmoothElementColors(true);
			((PgElementSet) out).makeElementFromVertexColors();
			((PgElementSet) out).showElementColors(true);
		}
		if (out instanceof PgPolygonSet) {
			((PgPolygonSet) out).makePolygonFromVertexColors();
		}
		setGeometryInfo(out,p.getInput());
		store.geomChanged(out);
	}

	@Override
	public void newActiveInput(String name) {
		if (activePairs.containsKey(name)) {
			showStatus(name + " is already active");
			return;
		}
		PgGeometryIf input = store.getGeom(name);
		PgGeometryIf output = store.aquireGeometry(getPreferredOutputName(name), input, this);
		GeomPair p = new GeomPair(input, output);
		activePairs.put(name, p);
		activeInputNames.add(name);
		this.setCheckboxesFromGeomety(p.getOutput());
		setDisplayProperties(p.getOutput());
		calcGeom(p);
		store.newPair(this, p);
	}

	@Override
	public boolean update(Object o) {
		if (o instanceof PuParameter) {
			return parameterChanged((PuParameter) o);
		} else
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
		} else
			super.itemStateChanged(e);
	}

	public String makeCGIstring() {
		return null;
	}

	@Override
	public void geometryDefHasChanged(AbstractClient client, Calculator inCalc) {
	}

	@Override
	public Definition createDefaultDef() {
		Definition def;
		def = new Definition("Colourize", DefType.colour, "");
		def.add(new DefVariable("x", -1, 1));
		def.add(new DefVariable("y", -1, 1));
		def.add(new DefVariable("z", -1, 1));
		return def;
	}

}
