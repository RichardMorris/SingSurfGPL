/* @author rich
 * Created on 30-Mar-2005
 *
 * See LICENSE.txt for license information.
 */
package org.singsurf.singsurf.clients;

import java.awt.Checkbox;
import java.awt.Color;
import java.awt.ItemSelectable;
import java.awt.event.ItemEvent;
import java.util.Arrays;
import java.util.List;

import org.singsurf.singsurf.Fractometer;
import org.singsurf.singsurf.LParamList;
import org.singsurf.singsurf.PuParameter;
import org.singsurf.singsurf.PuVariable;
import org.singsurf.singsurf.calculators.Calculator;
import org.singsurf.singsurf.definitions.DefVariable;
import org.singsurf.singsurf.definitions.Definition;
import org.singsurf.singsurf.definitions.Option;
import org.singsurf.singsurf.geometries.GeomPair;
import org.singsurf.singsurf.geometries.GeomStore;
import org.singsurf.singsurf.jepwrapper.EvaluationException;
import org.singsurf.singsurf.operators.AbstractExtrude;
import org.singsurf.singsurf.operators.CalcExtrude;
import org.singsurf.singsurf.operators.ContinuityClip;
import org.singsurf.singsurf.operators.SphereClip;
import org.singsurf.singsurf.operators.UnSuportedGeometryException;

import jv.geom.PgElementSet;
import jv.geom.PgPointSet;
import jv.geom.PgPolygonSet;
import jv.project.PgGeometryIf;

/**
 * @author Rich Morris Created on 30-Mar-2005
 */
public class Extrude extends AbstractOperatorClient {
	/** 
	 * 
	 */
	private static final long serialVersionUID = -4659133739866394363L;

	protected Fractometer m_Clipping;
	protected Fractometer m_ContDist;
	DefVariable localY;


	Checkbox cbParamsFromTexture = new Checkbox("Parameters from texture",false);
	Checkbox cbAsLineBundle = new Checkbox("As Line Bundle",false);

	/********** Constructor *********/

	public Extrude(GeomStore store, Definition def) {
		super(store, def.getName());
		if (getClass() == Extrude.class) {
			init(def);
		}
	}

	public void init(Definition def) {
		super.init();
		localY = new DefVariable(def.getVar(0).getName(), "Normal");
		displayVars = new PuVariable[] { new PuVariable(this, localY) };

		newParams = new LParamList(this);
		m_Clipping = new Fractometer(100.0);
		m_Clipping.setMinVal(0.0);
		m_ContDist = new Fractometer(100.0);
		m_ContDist.setMinVal(0.0);
		this.cbShowFace.setState(true);
		this.cbShowEdge.setState(false);
		this.cbShowVert.setState(false);
		this.chCurveColours.addItemListener(this);
		loadDefinition(def);
	}

	@Override
	public void loadDefinition(Definition newdef) {
		Definition def = newdef.duplicate();
		def.setName(this.getName());
		localY = def.getVar(0);
		displayVars[0].set(localY);
		this.getInfoPanel().setTitle(this.getName());
		calc = new Calculator(def, 1);
		calc.build();

		if (!calc.isGood())
			showStatus(calc.getMsg());
		setDisplayEquation(def.getEquation());
		refreshParams();
		// calcSurf();

		Option colopt = def.getOption("colour");
		if (colopt != null)
			this.chCurveColours.select(colopt.getStringVal());
		loadFromDefOption(def,"surfColour",chSurfColours);
		loadFromDefOption(def,"curveColour",chCurveColours);

		Option clipopt = def.getOption("clipping");
		if (clipopt != null)
			this.m_Clipping.setValue(clipopt.getDoubleVal());

		Option contopt = def.getOption("continuity");
		if (contopt != null)
			this.m_ContDist.setValue(contopt.getDoubleVal());

		this.setStandardControlsFromOptions();

		setCheckboxStateFromOption(cbParamsFromTexture,def,"parametersFromTextures");		
		setCheckboxStateFromOption(cbAsLineBundle,def,"lineBundle");		

	}

	@Override
	public void setDefinitionOptions(Definition def) {

		def.setOption("showFace", this.cbShowFace.getState());
		def.setOption("showEdge", this.cbShowEdge.getState());

		def.setOption("showVert", this.cbShowVert.getState());
		def.setOption("showCurve", this.cbShowCurves.getState());
		def.setOption("surfColour", this.chSurfColours.getSelectedItem());
		def.setOption("curveColour", this.chCurveColours.getSelectedItem());

		def.setOption("clipping", this.m_Clipping.getValue());
		def.setOption("continuity", this.m_ContDist.getValue());

		def.setOption("lineBundle", this.cbAsLineBundle.getState());
		def.setOption("parametersFromTextures", this.cbParamsFromTexture.getState());

	}
	
	
	
	protected List<String> getPossibleSurfaceColours() {
		return Arrays.asList("None",
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


	@Override
	public PgGeometryIf calcGeomThread(GeomPair p) {
		if (calc != null && !calc.isGood()) {
			showStatus(calc.getMsg());
			return null;
		}
		PgGeometryIf input = p.getInput();

		AbstractExtrude map = new CalcExtrude(calc.createEvaluator(),localY);
		map.setParamsFromTexture(cbParamsFromTexture.getState());
		map.setAsLineBundle(this.cbAsLineBundle.getState());
		if (input == null) {
			showStatus("Null input");
			return null;
		}
		PgPointSet resultGeom = null;
		try {
			resultGeom = (PgPointSet) map.operate(input);
			System.out.println("extrude after map " + resultGeom.getNumVertices() + " pts");
			if(resultGeom instanceof PgElementSet) {
				System.out.println("\t " + ((PgElementSet)resultGeom).getNumElements() + " faces");
			}
//		for(int i=0;i<mappedGeom.getNumVertices();++i) {
//			PdVector vec = mappedGeom.getVertex(i);
//			System.out.println(Arrays.toString(vec.getEntries()));
//		}

			ContinuityClip cclip = new ContinuityClip(this.m_ContDist.getValue());
			cclip.operate(resultGeom);
			System.out.println("mapped after cont clip " + resultGeom.getNumVertices() + " pts");
			if(resultGeom instanceof PgElementSet) {
				System.out.println("\t " + ((PgElementSet)resultGeom).getNumElements() + " faces");
			}

			SphereClip clip = new SphereClip(this.m_Clipping.getValue());
			clip.operate(resultGeom);
			System.out.println("mapped after sphere clip " + resultGeom.getNumVertices() + " pts");
			if(resultGeom instanceof PgElementSet) {
				System.out.println("\t " + ((PgElementSet)resultGeom).getNumElements() + " faces");
			}
		} catch (UnSuportedGeometryException e) {
			showStatus("Unsupported geometry type");
			return null;
		} catch (EvaluationException e) {
			System.out.println(e.toString());
			calc.setGood(false);
			return null;
		}

		return resultGeom;
	}
	
	public void displayGeom(GeomPair p, PgGeometryIf mappedGeom) {
	
		System.out.println("mapped outer");
		// debugCols((PgElementSet) mappedGeom);
		PgGeometryIf out = p.getOutput();
		PgGeometryIf input = p.getInput();

		// debugCols((PgElementSet) out);

		GeomStore.copySrcTgt(mappedGeom, out);

		System.out.println("out " + ((PgPointSet) out).getNumVertices());
		// debugCols((PgElementSet) out);

		if (out instanceof PgElementSet) {
			PgElementSet outSurf = (PgElementSet) out;
//			PgElementSet in2 = (PgElementSet) input;

			switch(chSurfColours.getSelectedItem()) {
			case "None":
				break;
			case "Colours from XYZ":
				outSurf.makeElementColorsFromXYZ();
				outSurf.showElementColors(true);
				break;
			default:
				Color c = getColor(chSurfColours.getSelectedItem());
				outSurf.setGlobalElementColor(c);
				outSurf.showElementColors(false);
				outSurf.showVertexColors(false);
				break;
			}

		}
		setDisplayProperties(out);
		setGeometryInfo(out,input);
		store.geomChanged(out);
	}

	@Override
	public void newActiveInput(String name) {
		if (activePairs.containsKey(name)) {
			showStatus(name + " is already active");
			return;
		}
		PgGeometryIf input = store.getGeom(name);
		PgGeometryIf output = null;
		if(this.cbAsLineBundle.getState()) {
		if (input instanceof PgElementSet)
			output = store.aquireSurface(getPreferredOutputName(name), this);
		else if (input instanceof PgPolygonSet)
			output = store.aquireCurve(getPreferredOutputName(name), this);
		else if (input instanceof PgPointSet)
			output = store.aquireCurve(getPreferredOutputName(name), this);
		} else {
			if (input instanceof PgElementSet)
				output = store.aquireSurface(getPreferredOutputName(name), this);
			else if (input instanceof PgPolygonSet)
				output = store.aquireSurface(getPreferredOutputName(name), this);
			else if (input instanceof PgPointSet)
				output = store.aquireCurve(getPreferredOutputName(name), this);
			
		}
		GeomPair p = new GeomPair(input, output);
		this.setCheckboxesFromGeomety(p.getOutput());
		setDisplayProperties(p.getOutput());
		activePairs.put(name, p);
		activeInputNames.add(name);
		calcGeom(p);
		store.newPair(this, p);
	}

	public void variableRangeChanged(int n, PuVariable v) {
		calc.setVarBounds(n, v.getMin(), v.getMax(), v.getSteps());
		if (n == 0)
			localY.setBounds(v.getMin(), v.getMax(), v.getSteps());
		calcGeoms();
	}

	@Override
	public boolean update(Object o) {
		if (o == displayVars[0]) {
			variableRangeChanged(0, (PuVariable) o);
			return true;
		}
		else if (o instanceof PuParameter) {
			return parameterChanged((PuParameter) o);
		} else if (o == m_Clipping || o == this.m_ContDist) {
			calcGeoms();
			return true;
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
	public void setDP(int dp) {
		super.setDP(dp);
		this.m_Clipping.setDecimalPlaces(dp);
		this.m_ContDist.setDecimalPlaces(dp);
	}
}
